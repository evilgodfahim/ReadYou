package me.ash.reader.ui.page.home.reading

private val DEFAULT_AI_CHAT_PROMPT =
    "Based on the provided article content, answer questions directly and clearly in English. If uncertain, state so clearly."

const val AI_CHAT_ROLE_USER = "user"
const val AI_CHAT_ROLE_ASSISTANT = "assistant"

const val AI_CHAT_CONTEXT_MANUAL = "manual"
const val AI_CHAT_CONTEXT_EXPLAIN_ARTICLE = "article"
const val AI_CHAT_CONTEXT_EXPLAIN_SELECTION = "selection"
const val AI_CHAT_CONTEXT_BACKGROUND = "background"
const val AI_CHAT_CONTEXT_INTRO = "intro"

enum class AiChatQuickAction {
    ExplainArticle,
    ExplainSelection,
    GiveBackground,
    Introduce,
}

fun resolveAiChatPrompt(prompt: String): String =
    prompt.ifBlank { DEFAULT_AI_CHAT_PROMPT }

fun buildAiChatQuickQuestion(
    action: AiChatQuickAction,
    hasSelection: Boolean,
): String =
    when (action) {
        AiChatQuickAction.ExplainArticle -> "Please explain what this article is about and summarize its key points."
        AiChatQuickAction.ExplainSelection -> "Please explain what the currently selected text means."
        AiChatQuickAction.GiveBackground ->
            if (hasSelection) {
                "Please provide relevant background context needed to understand this selection."
            } else {
                "Please provide relevant background context needed to understand this article."
            }

        AiChatQuickAction.Introduce ->
            if (hasSelection) {
                "Please introduce the subject of this selection in simple terms."
            } else {
                "Please introduce the core subject of this article in simple terms."
            }
    }

fun contextTypeForQuickAction(action: AiChatQuickAction): String =
    when (action) {
        AiChatQuickAction.ExplainArticle -> AI_CHAT_CONTEXT_EXPLAIN_ARTICLE
        AiChatQuickAction.ExplainSelection -> AI_CHAT_CONTEXT_EXPLAIN_SELECTION
        AiChatQuickAction.GiveBackground -> AI_CHAT_CONTEXT_BACKGROUND
        AiChatQuickAction.Introduce -> AI_CHAT_CONTEXT_INTRO
    }
