package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.ash.reader.R
import me.ash.reader.domain.model.group.Group
import me.ash.reader.ui.page.home.feeds.drawer.group.GroupOptionViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupItem(
    group: Group,
    articleCount: Int,
    isExpanded: () -> Boolean,
    groupOptionViewModel: GroupOptionViewModel = hiltViewModel(),
    onExpanded: () -> Unit = {},
    onLongClick: () -> Unit = {},
    groupOnClick: () -> Unit = {},
) {
    val expanded = isExpanded()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { groupOnClick() },
                onLongClick = {
                    groupOptionViewModel.fetchGroup(groupId = group.id)
                    onLongClick()
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(if (expanded) R.string.expand_less else R.string.expand_more),
            tint = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .combinedClickable(
                    onClick = { onExpanded() },
                    onLongClick = {}
                )
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = group.name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (expanded) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 15.sp,
            ),
            color = if (expanded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        if (articleCount > 0) {
            Text(
                text = if (articleCount > 999) "1000+" else articleCount.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
inline fun GroupWithFeedsContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        content = { content() }
    )
}

