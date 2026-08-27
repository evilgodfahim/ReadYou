package me.ash.reader.ui.page.home.reading

const val DEFAULT_AI_TRANSLATION_PROMPT =
    "Translate the provided content into clear, accurate, and natural English, preserving tone and terminology."

fun resolveAiTranslationPrompt(prompt: String): String =
    prompt.ifBlank { DEFAULT_AI_TRANSLATION_PROMPT }
