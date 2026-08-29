package me.ash.reader.domain.service

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.ash.reader.infrastructure.di.ApplicationScope
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.rss.ReaderCacheHelper
import timber.log.Timber

private const val TAG = "SyncManager"

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rssService: RssService,
    private val workManager: WorkManager,
    private val accountService: AccountService,
    private val pendingAiSummaryEnqueuer: PendingAiSummaryEnqueuer,
    private val readerCacheHelper: ReaderCacheHelper,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val _isForegroundSyncing = MutableStateFlow(false)
    private val syncMutex = Mutex()

    private val backgroundSyncingFlow =
        workManager
            .getWorkInfosByTagFlow(SyncWorker.SYNC_TAG)
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING }
            }

    val isSyncing: StateFlow<Boolean> =
        combine(_isForegroundSyncing, backgroundSyncingFlow) { fg, bg -> fg || bg }
            .stateIn(applicationScope, SharingStarted.Eagerly, false)

    fun syncImmediately(
        accountId: Int? = null,
        feedId: String? = null,
        groupId: String? = null,
    ): Job = applicationScope.launch(ioDispatcher) {
        executeSync(
            accountId = accountId ?: accountService.getCurrentAccountId(),
            feedId = feedId,
            groupId = groupId,
            isForeground = true,
        )
    }

    suspend fun executeSync(
        accountId: Int,
        feedId: String? = null,
        groupId: String? = null,
        isForeground: Boolean = false,
    ): ListenableWorker.Result = withContext(ioDispatcher) {
        if (syncMutex.isLocked && isForeground) {
            Timber.tag(TAG).d("Sync already in progress, waiting for lock...")
        }
        syncMutex.withLock {
            if (isForeground) {
                _isForegroundSyncing.value = true
            }
            try {
                val account = accountService.getAccountById(accountId)
                    ?: return@withLock ListenableWorker.Result.failure()
                val rssRepository = rssService.get(account.type.id)
                Timber.tag(TAG).i("Starting sync for account: $accountId, feed: $feedId, group: $groupId")
                val result = rssRepository.sync(
                    accountId = accountId,
                    feedId = feedId,
                    groupId = groupId,
                )

                if (result is ListenableWorker.Result.Success) {
                    pendingAiSummaryEnqueuer.enqueueUnreadBackfill(
                        accountId = accountId,
                        requireBackfillOnSync = true,
                    )
                    rssRepository.clearKeepArchivedArticles(accountId).forEach {
                        readerCacheHelper.deleteCacheFor(articleId = it.id, accountId = it.accountId)
                    }
                    val workerInputData = workDataOf(
                        SyncWorker.INPUT_ACCOUNT_ID to accountId,
                        "feedId" to feedId,
                        "groupId" to groupId,
                    )
                    val aiSummaryWork =
                        OneTimeWorkRequestBuilder<AiSummaryPrecomputeWorker>()
                            .addTag(SyncWorker.ONETIME_WORK_TAG)
                            .setInputData(workerInputData)
                            .setBackoffCriteria(
                                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                                backoffDelay = 30,
                                timeUnit = TimeUnit.SECONDS,
                            )
                            .build()
                    val widgetWork = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()

                    workManager
                        .beginUniqueWork(
                            uniqueWorkName = SyncWorker.postSyncWorkName(accountId),
                            existingWorkPolicy = ExistingWorkPolicy.KEEP,
                            listOf(aiSummaryWork, widgetWork),
                        )
                        .enqueue()
                }
                result
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Sync failed with exception")
                ListenableWorker.Result.failure()
            } finally {
                if (isForeground) {
                    _isForegroundSyncing.value = false
                }
                SyncProgressTracker.finishSync()
            }
        }
    }
}
