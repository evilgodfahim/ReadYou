package me.ash.reader.domain.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import me.ash.reader.domain.data.SyncLogger
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.FeedWithArticle
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.di.DefaultDispatcher
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.rss.RssHelper
import timber.log.Timber

private const val TAG = "LocalRssService"

class LocalRssService
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val articleDao: ArticleDao,
    private val feedDao: FeedDao,
    private val rssHelper: RssHelper,
    private val notificationHelper: NotificationHelper,
    private val groupDao: GroupDao,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val workManager: WorkManager,
    private val accountService: AccountService,
    private val pendingAiSummaryEnqueuer: PendingAiSummaryEnqueuer,
    private val syncLogger: SyncLogger,
) :
    AbstractRssRepository(
        articleDao,
        groupDao,
        feedDao,
        workManager,
        rssHelper,
        notificationHelper,
        ioDispatcher,
        defaultDispatcher,
        accountService,
    ) {

    override suspend fun sync(
        accountId: Int,
        feedId: String?,
        groupId: String?
    ): ListenableWorker.Result = supervisorScope {
        return@supervisorScope runCatching {
            val preTime = System.currentTimeMillis()
            val preDate = Date(preTime)
            val currentAccount = accountService.getAccountById(accountId)!!
            require(currentAccount.type.id == AccountType.Local.id) {
                "Account type is invalid"
            }
            val semaphore = Semaphore(256)

            val feedsToSync =
                when {
                    feedId != null -> listOfNotNull(feedDao.queryById(feedId))
                    groupId != null -> feedDao.queryByGroupId(accountId, groupId)
                    else -> feedDao.queryAll(accountId)
                }

            // Phase 1: Pure parallel HTTP fetching & XML parsing (no DB contention)
            val fetchedResults =
                feedsToSync
                    .map { currentFeed ->
                        async(Dispatchers.IO) {
                            semaphore.withPermit {
                                currentFeed to syncFeed(currentFeed, preDate)
                            }
                        }
                    }
                    .awaitAll()

            // Phase 2: Save to Database
            for ((currentFeed, fetchedFeed) in fetchedResults) {
                if (fetchedFeed.articles.isEmpty()) continue
                val archivedArticles =
                    feedDao
                        .queryArchivedArticles(currentFeed.id)
                        .map { it.link }
                        .toSet()

                val fetchedArticles =
                    fetchedFeed.articles.filterNot {
                        archivedArticles.contains(it.link)
                    }

                if (fetchedArticles.isEmpty()) continue

                val newArticles =
                    articleDao.insertListIfNotExist(
                        articles = fetchedArticles,
                        feed = currentFeed,
                    )
                if (newArticles.isNotEmpty()) {
                    pendingAiSummaryEnqueuer.enqueue(accountId, newArticles)
                    if (currentFeed.isNotification) {
                        notificationHelper.notify(
                            fetchedFeed.copy(articles = newArticles, feed = currentFeed)
                        )
                    }
                }
            }

            Timber.tag("RlOG").i("onCompletion: ${System.currentTimeMillis() - preTime}")
            accountService.update(currentAccount.copy(updateAt = Date()))
            ListenableWorker.Result.success()
        }
            .onFailure { syncLogger.log(it) }
            .getOrNull() ?: ListenableWorker.Result.retry()
    }

    private suspend fun syncFeed(feed: Feed, preDate: Date = Date()): FeedWithArticle {
        val articles = rssHelper.queryRssXml(feed, "", preDate)
        if (feed.icon == null) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    val iconLink = rssHelper.queryRssIconLink(feed.url)
                    if (iconLink != null) {
                        rssHelper.saveRssIcon(feedDao, feed, iconLink)
                    }
                }
            }
        }
        return FeedWithArticle(
            feed = feed.copy(isNotification = feed.isNotification && articles.isNotEmpty()),
            articles = articles,
        )
    }
}
