package me.ash.reader.ui.page.home.reading

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.LocalReadingTextFontSize
import me.ash.reader.ui.component.reader.bodyStyle
import me.ash.reader.ui.component.reader.h3Style

import androidx.compose.foundation.background
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import me.ash.reader.infrastructure.preference.readAiProviderCredentialsMap
import me.ash.reader.infrastructure.preference.readActiveProviderId
import me.ash.reader.infrastructure.preference.saveAiProviderCredentialsMap
import me.ash.reader.infrastructure.preference.readAiSummaryPromptState
import me.ash.reader.infrastructure.preference.saveAiSummaryPromptState
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.page.settings.ai.BuiltInProviders

@Composable
fun AiSummaryCard(
    summary: String,
    isLoading: Boolean,
    error: String?,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit = {},
    onVisibilityChanged: (Boolean) -> Unit = {},
    onRegenerate: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val density = LocalDensity.current
    val minVisibleHeight = with(density) { 24.dp.toPx() }
    var lastVisibility by remember { mutableStateOf<Boolean?>(null) }

    var activeProviderId by remember { mutableStateOf(context.readActiveProviderId()) }
    var promptState by remember { mutableStateOf(context.readAiSummaryPromptState()) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var promptMenuExpanded by remember { mutableStateOf(false) }

    val activeProviderTitle = remember(activeProviderId) {
        BuiltInProviders.find { it.id == activeProviderId }?.title ?: activeProviderId
    }

    val activePromptName = remember(promptState) {
        promptState.currentPrompt?.name ?: "Default Prompt"
    }

    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = 8.dp)
                .onGloballyPositioned { coordinates ->
                    val bounds: Rect = coordinates.boundsInWindow()
                    val viewportWidth = view.width.toFloat()
                    val viewportHeight = view.height.toFloat()
                    val visibleWidth =
                        max(0f, min(bounds.right, viewportWidth) - max(bounds.left, 0f))
                    val visibleHeight =
                        max(0f, min(bounds.bottom, viewportHeight) - max(bounds.top, 0f))
                    val isVisible =
                        visibleWidth > 0f &&
                            visibleHeight >= min(minVisibleHeight, bounds.height)
                    if (lastVisibility != isVisible) {
                        lastVisibility = isVisible
                        onVisibilityChanged(isVisible)
                    }
                },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpanded),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = stringResource(id = R.string.ai_summary),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isLoading) {
                        Spacer(modifier = Modifier.size(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Icon(
                    imageVector =
                        if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription =
                        stringResource(
                            id = if (isExpanded) R.string.expand_less else R.string.expand_more,
                        ),
                )
            }

            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Provider & Prompt Controls Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Provider Dropdown Chip
                        Box {
                            AssistChip(
                                onClick = { providerMenuExpanded = true },
                                label = { Text(activeProviderTitle, fontSize = 12.sp) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Rounded.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ),
                            )
                            DropdownMenu(
                                expanded = providerMenuExpanded,
                                onDismissRequest = { providerMenuExpanded = false },
                            ) {
                                BuiltInProviders.forEach { provider ->
                                    DropdownMenuItem(
                                        text = { Text(provider.title) },
                                        onClick = {
                                            providerMenuExpanded = false
                                            activeProviderId = provider.id
                                            val currentMap = context.readAiProviderCredentialsMap()
                                            val creds = currentMap[provider.id]
                                            scope.launch {
                                                context.saveAiProviderCredentialsMap(
                                                    map = currentMap,
                                                    activeProviderId = provider.id,
                                                )
                                                if (creds != null && creds.apiKey.isNotBlank()) {
                                                    context.dataStore.put(DataStoreKey.aiApiKey, creds.apiKey)
                                                    context.dataStore.put(DataStoreKey.aiModel, creds.model)
                                                    context.dataStore.put(DataStoreKey.aiBaseUrl, creds.baseUrl)
                                                }
                                            }
                                            onRegenerate()
                                        },
                                    )
                                }
                            }
                        }

                        // Prompt Dropdown Chip
                        Box {
                            AssistChip(
                                onClick = { promptMenuExpanded = true },
                                label = { Text(activePromptName, fontSize = 12.sp) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Rounded.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ),
                            )
                            DropdownMenu(
                                expanded = promptMenuExpanded,
                                onDismissRequest = { promptMenuExpanded = false },
                            ) {
                                promptState.prompts.forEach { preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset.name) },
                                        onClick = {
                                            promptMenuExpanded = false
                                            val newState = promptState.copy(activePromptId = preset.id)
                                            promptState = newState
                                            scope.launch {
                                                context.saveAiSummaryPromptState(newState)
                                            }
                                            onRegenerate()
                                        },
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onRegenerate,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Regenerate Summary",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Column {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.ai_summary_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (summary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (error != null) {
                    Text(
                        text = stringResource(id = R.string.ai_summary_error, error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    if (summary.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (summary.isNotEmpty()) {
                    AiSummaryMarkdownContent(markdown = summary)
                }
            }
        }
    }
}

@Composable
fun AiSummaryMarkdownContent(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(markdown) { parseAiChatMarkdownBlocks(markdown) }
    val baseBodyStyle = bodyStyle()
    val baseH3Style = h3Style()
    val currentFontSize = LocalReadingTextFontSize.current.sp

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(14.dp))
            }
            when (block) {
                is AiChatMarkdownBlock.Heading -> {
                    Text(
                        text = buildMarkdownAnnotatedString(
                            text = block.content,
                            boldColor = MaterialTheme.colorScheme.primary,
                        ),
                        style = baseH3Style.copy(
                            fontSize = (LocalReadingTextFontSize.current * 1.18f).sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                is AiChatMarkdownBlock.Paragraph -> {
                    val subParagraphs = block.content.split("\n\n", "\n").map { it.trim() }.filter { it.isNotBlank() }
                    if (subParagraphs.size > 1) {
                        Column {
                            subParagraphs.forEachIndexed { subIdx, subText ->
                                if (subIdx > 0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                Text(
                                    text = buildMarkdownAnnotatedString(subText),
                                    style = baseBodyStyle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = buildMarkdownAnnotatedString(block.content),
                            style = baseBodyStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                is AiChatMarkdownBlock.ListBlock -> {
                    Column {
                        block.items.forEach { item ->
                            Row(modifier = Modifier.padding(start = (item.depth * 14).dp, bottom = 4.dp)) {
                                Text(
                                    text = "${item.marker} ",
                                    style = baseBodyStyle,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = buildMarkdownAnnotatedString(item.content),
                                    style = baseBodyStyle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                is AiChatMarkdownBlock.Quote -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = buildMarkdownAnnotatedString(block.lines.joinToString("\n")),
                            style = baseBodyStyle.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                is AiChatMarkdownBlock.CodeBlock -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = block.code,
                            style = baseBodyStyle.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = (LocalReadingTextFontSize.current * 0.9f).sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                is AiChatMarkdownBlock.Divider -> {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                is AiChatMarkdownBlock.Table -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        if (block.headers.isNotEmpty()) {
                            Text(
                                text = block.headers.joinToString(" | "),
                                style = baseBodyStyle.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                        block.rows.forEach { row ->
                            Text(
                                text = row.joinToString(" | "),
                                style = baseBodyStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildMarkdownAnnotatedString(
    text: String,
    highlightBg: Color = Color.Unspecified,
    boldColor: Color = Color.Unspecified,
): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        while (index < text.length) {
            when {
                text.startsWith("***", index) || text.startsWith("___", index) -> {
                    val delim = text.substring(index, index + 3)
                    val end = text.indexOf(delim, index + 3)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = boldColor))
                        append(text.substring(index + 3, end))
                        pop()
                        index = end + 3
                        continue
                    }
                }

                text.startsWith("**", index) || text.startsWith("__", index) -> {
                    val delim = text.substring(index, index + 2)
                    val end = text.indexOf(delim, index + 2)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor))
                        append(text.substring(index + 2, end))
                        pop()
                        index = end + 2
                        continue
                    }
                }

                text.startsWith("*", index) || text.startsWith("_", index) -> {
                    val delim = text[index]
                    val end = text.indexOf(delim, index + 1)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(index + 1, end))
                        pop()
                        index = end + 1
                        continue
                    }
                }

                text.startsWith("`", index) -> {
                    val end = text.indexOf('`', index + 1)
                    if (end != -1) {
                        pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                        append(text.substring(index + 1, end))
                        pop()
                        index = end + 1
                        continue
                    }
                }
            }
            append(text[index])
            index++
        }
    }
}

