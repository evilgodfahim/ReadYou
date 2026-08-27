package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.RYExtensibleVisibility
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionViewModel

@OptIn(
    ExperimentalFoundationApi::class,
)
@Composable
private fun FeedItemImpl(
    feed: Feed,
    isLastItem: () -> Boolean = { false },
    onLongClickCallback: (String) -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = {
                    onLongClick()
                    scope.launch { onLongClickCallback(feed.id) }
                }
            )
            .padding(start = 44.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedIcon(
                feedName = feed.name,
                iconUrl = feed.icon,
                modifier = Modifier.size(18.dp)
            )
            Text(
                modifier = Modifier.padding(start = 10.dp, end = 8.dp),
                text = feed.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    lineHeightStyle = LineHeightStyle(
                        trim = LineHeightStyle.Trim.Both,
                        alignment = LineHeightStyle.Alignment.Center
                    )
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (feed.important != 0) {
            Text(
                text = if (feed.important > 999) "1000+" else feed.important.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
)
@Composable
fun FeedItem(
    feed: Feed,
    isLastItem: () -> Boolean = { false },
    isExpanded: () -> Boolean,
    feedOptionViewModel: FeedOptionViewModel = hiltViewModel(),
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    RYExtensibleVisibility(visible = isExpanded()) {
        FeedItemImpl(
            feed = feed,
            isLastItem = isLastItem,
            onClick = onClick,
            onLongClick = onLongClick,
            onLongClickCallback = { feedId ->
                scope.launch {
                    feedOptionViewModel.fetchFeed(feedId = feedId)
                }
            })
    }
}

@Preview
@Composable
private fun Preview() {
    MaterialTheme {
        Surface {
            FeedItemImpl(
                feed = Feed(
                    id = "1",
                    name = "Awesome Feed",
                    icon = null,
                    important = 5,
                    url = "https://example.com",
                    groupId = "1",
                    accountId = 1,
                    isNotification = false,
                    isFullContent = false,
                ),
            )
        }
    }
}
