package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.ash.reader.ui.ext.dataStore

data class AiSummaryPromptPreset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val prompt: String,
)

data class AiSummaryPromptState(
    val prompts: List<AiSummaryPromptPreset>,
    val activePromptId: String,
) {
    val currentPrompt: AiSummaryPromptPreset
        get() = prompts.find { it.id == activePromptId } ?: prompts.firstOrNull() ?: DefaultAiSummaryPrompts.first()
}

fun Context.readAiSummaryPromptState(): AiSummaryPromptState = runBlocking {
    dataStore.data.first().readAiSummaryPromptState()
}

private val gson = Gson()
private val promptListType = object : TypeToken<List<AiSummaryPromptPreset>>() {}.type

private val aiSummaryPromptsKey = stringPreferencesKey("ai_summary_prompts_json")
private val aiActiveSummaryPromptIdKey = stringPreferencesKey("ai_active_summary_prompt_id")

val DefaultAiSummaryPrompts = listOf(
    AiSummaryPromptPreset(
        id = "default_standard",
        name = "Standard Structured Summary",
        prompt = "Write a clear, structured summary of the article in English, broken into 3 to 4 concise paragraphs. Focus on what happened, key supporting facts, why it matters, and potential implications or controversies. Keep it clear, informative, and free of fluff.",
    ),
    AiSummaryPromptPreset(
        id = "concise_bullets",
        name = "Concise Bullet Points",
        prompt = "Summarize the article in 3 to 5 clear, impactful bullet points highlighting the main takeaways, context, and key details.",
    ),
    AiSummaryPromptPreset(
        id = "executive_brief",
        name = "Executive Brief",
        prompt = "Provide a high-level executive brief of the article including core thesis, strategic insights, key findings, and actionable implications.",
    ),
    AiSummaryPromptPreset(
        id = "key_takeaways",
        name = "Key Takeaways & Impact",
        prompt = "Write a 2-paragraph summary focusing on key findings, original context, and long-term impact.",
    )
)

fun Preferences.readAiSummaryPromptState(): AiSummaryPromptState {
    val json = this[aiSummaryPromptsKey].orEmpty()
    val activeId = this[aiActiveSummaryPromptIdKey].orEmpty()

    val parsedPrompts = if (json.isNotBlank()) {
        runCatching { gson.fromJson<List<AiSummaryPromptPreset>>(json, promptListType) }.getOrDefault(emptyList())
    } else emptyList()

    val prompts = if (parsedPrompts.isNotEmpty()) parsedPrompts else DefaultAiSummaryPrompts
    val resolvedActiveId = activeId.takeIf { id -> prompts.any { it.id == id } } ?: prompts.first().id

    return AiSummaryPromptState(prompts = prompts, activePromptId = resolvedActiveId)
}

suspend fun Context.saveAiSummaryPromptState(state: AiSummaryPromptState) {
    dataStore.edit { preferences ->
        preferences[aiSummaryPromptsKey] = gson.toJson(state.prompts)
        preferences[aiActiveSummaryPromptIdKey] = state.activePromptId
    }
}
