package me.ash.reader.ui.page.home.reading

import me.ash.reader.ui.page.adaptive.ReadingUiState

val DEFAULT_AI_SUMMARIZATION_PROMPT =
    """
    Write a clear, structured summary of the article in English, broken into 3 to 4 concise paragraphs. Focus on what happened, key supporting facts, why it matters, and potential implications or controversies. Keep it clear, informative, and free of fluff.
    """.trimIndent()

fun resolveAiSummarizationPrompt(prompt: String): String =
    prompt.ifBlank { DEFAULT_AI_SUMMARIZATION_PROMPT }

fun shouldAutoSummarize(feedAutoSummary: Boolean, state: ReadingUiState): Boolean = false
