package me.ash.reader.domain.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.withTimeout
import me.ash.reader.infrastructure.net.ApiResult
import me.ash.reader.infrastructure.net.openai.ChatThinkingConfig
import me.ash.reader.infrastructure.net.openai.OpenAiApiService
import me.ash.reader.infrastructure.net.openai.ChatCompletionRequest
import me.ash.reader.infrastructure.net.openai.ChatMessage
import javax.inject.Inject
import javax.inject.Singleton

import me.ash.reader.infrastructure.preference.AiPromptFormatter
import me.ash.reader.ui.page.home.reading.DEFAULT_AI_SUMMARIZATION_PROMPT
import me.ash.reader.ui.page.home.reading.resolveAiSummarizationPrompt

@Singleton
class AiSummaryRepository @Inject constructor() {

    suspend fun recommendCommuteBriefArticles(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        targetDurationMinutes: Int,
        candidates: List<CommuteBriefRecommendationCandidate>,
    ): ApiResult<List<String>> {
        return try {
            val service = OpenAiApiService.getInstance(
                baseUrl = baseUrl,
                apiKey = apiKey,
                timeoutSeconds = COMMUTE_BRIEF_RECOMMENDATION_TIMEOUT_SECONDS,
                callTimeoutSeconds = COMMUTE_BRIEF_RECOMMENDATION_TIMEOUT_SECONDS,
            )
            val request = ChatCompletionRequest(
                model = model,
                messages = buildCommuteBriefRecommendationMessages(
                    prompt = prompt,
                    targetDurationMinutes = targetDurationMinutes,
                    candidates = candidates,
                ),
                temperature = 0.2,
                maxTokens = 1200,
            )
            val response =
                withTimeout(COMMUTE_BRIEF_RECOMMENDATION_TIMEOUT_SECONDS * 1000L) {
                    service.createChatCompletion(request)
                }
            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content.orEmpty()
                val articleIds = parseRecommendedArticleIds(content)
                if (articleIds.isNotEmpty()) {
                    ApiResult.Success(articleIds)
                } else {
                    ApiResult.BizError(Exception("No recommended article ids returned from API"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                ApiResult.BizError(Exception(errorMsg))
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    suspend fun fetchAvailableModels(
        baseUrl: String,
        apiKey: String
    ): ApiResult<List<String>> {
        return try {
            val service = OpenAiApiService.getInstance(baseUrl, apiKey)
            val response = service.getModels()

            if (response.isSuccessful && response.body() != null) {
                val modelIds = response.body()!!.data.map { it.id }
                ApiResult.Success(modelIds)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                ApiResult.BizError(Exception(errorMsg))
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    suspend fun testAiServiceConnection(
        baseUrl: String,
        apiKey: String,
        model: String,
    ): ApiResult<Unit> {
        return try {
            val service = OpenAiApiService.getInstance(
                baseUrl = baseUrl,
                apiKey = apiKey,
                timeoutSeconds = AI_CONNECTION_TEST_TIMEOUT_SECONDS,
                callTimeoutSeconds = AI_CONNECTION_TEST_TIMEOUT_SECONDS,
            )
            val request = ChatCompletionRequest(
                model = model,
                messages = listOf(ChatMessage(role = "user", content = "Reply with OK only.")),
                temperature = 0.0,
                maxTokens = 8,
                thinking = buildConnectionTestThinkingConfig(baseUrl),
            )
            val response =
                withTimeout(AI_CONNECTION_TEST_TIMEOUT_SECONDS * 1000L) {
                    service.createChatCompletion(request)
                }
            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.choices.firstOrNull()?.message?.content.orEmpty()
                if (content.isNotBlank()) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.BizError(Exception("No content returned from API"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                ApiResult.BizError(Exception(errorMsg))
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    suspend fun summarizeArticle(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        articleTitle: String = "",
        feedName: String = "",
        language: String = "",
        articleContent: String
    ): ApiResult<String> {
        return try {
            val service = OpenAiApiService.getInstance(baseUrl, apiKey)
            val messages = buildSummaryMessages(
                prompt = prompt,
                articleTitle = articleTitle,
                feedName = feedName,
                language = language,
                articleContent = articleContent,
            )

            val request = ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = 0.7,
                maxTokens = 2500
            )

            val response = service.createChatCompletion(request)

            if (response.isSuccessful && response.body() != null) {
                val choices = response.body()!!.choices
                if (choices.isNotEmpty()) {
                    val summary = choices[0].message.content
                    ApiResult.Success(summary)
                } else {
                    ApiResult.BizError(Exception("No choices returned from API"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                ApiResult.BizError(Exception(errorMsg))
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    internal fun buildSummaryMessages(
        prompt: String,
        articleTitle: String = "",
        feedName: String = "",
        language: String = "",
        articleContent: String,
    ): List<ChatMessage> {
        val resolvedPrompt = resolveAiSummarizationPrompt(prompt)
        val hasEmbeddedContent = AiPromptFormatter.hasContentPlaceholder(resolvedPrompt)

        return if (hasEmbeddedContent) {
            val fullPrompt = AiPromptFormatter.formatPrompt(
                template = resolvedPrompt,
                title = articleTitle,
                content = articleContent,
                language = language,
                feedName = feedName,
            )
            listOf(
                ChatMessage(role = "user", content = fullPrompt),
            )
        } else {
            val formattedPrompt = AiPromptFormatter.formatPrompt(
                template = resolvedPrompt,
                title = articleTitle,
                content = "",
                language = language,
                feedName = feedName,
            )

            val systemContent = if (resolvedPrompt == DEFAULT_AI_SUMMARIZATION_PROMPT) {
                buildString {
                    appendLine(formattedPrompt)
                    appendLine()
                    appendLine("补充要求：")
                    appendLine("- 只输出摘要正文")
                    appendLine("- 不要添加标题、前言、结尾或额外说明")
                    appendLine("- 按结构化短段落输出，最多 4 段")
                    appendLine("- 每段只写 1 到 2 句，没有对应信息就省略")
                    appendLine("- 优先覆盖：发生了什么、关键事实、为什么值得关注")
                    appendLine("- 如果原文信息不足，只基于已有内容总结，不要编造")
                }.trim()
            } else {
                formattedPrompt.trim()
            }

            val userContent = buildString {
                if (articleTitle.isNotBlank()) {
                    appendLine("Article Title: $articleTitle")
                    appendLine()
                }
                append(articleContent)
            }.trim()

            listOf(
                ChatMessage(role = "system", content = systemContent),
                ChatMessage(role = "user", content = userContent),
            )
        }
    }

    internal fun buildConnectionTestThinkingConfig(baseUrl: String): ChatThinkingConfig? =
        if (baseUrl.trim().contains("api.deepseek.com", ignoreCase = true)) {
            ChatThinkingConfig(type = "disabled")
        } else {
            null
        }

    internal fun buildCommuteBriefRecommendationMessages(
        prompt: String,
        targetDurationMinutes: Int,
        candidates: List<CommuteBriefRecommendationCandidate>,
    ): List<ChatMessage> =
        listOf(
            ChatMessage(
                role = "system",
                content =
                    buildString {
                        appendLine(prompt)
                        appendLine()
                        appendLine("补充要求：")
                        appendLine("- 目标总时长约 ${targetDurationMinutes} 分钟")
                        appendLine("- 只输出 JSON，不要 Markdown，不要解释")
                        appendLine("- JSON 格式必须是：{\"articleIds\":[\"id1\",\"id2\"]}")
                        appendLine("- articleIds 按推荐收听顺序排列，只能使用候选中已有 id")
                    }.trim(),
            ),
            ChatMessage(
                role = "user",
                content = candidates.joinToString(separator = "\n\n") { it.toPromptBlock() },
            ),
        )

    internal fun parseRecommendedArticleIds(content: String): List<String> {
        val cleaned = content.trim().removeSurrounding("```json", "```").removeSurrounding("```", "```").trim()
        return runCatching {
            val root = JsonParser.parseString(cleaned).asJsonObject
            root.getAsJsonArray("articleIds")
                ?.mapNotNull { it.asString.takeIf(String::isNotBlank) }
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun CommuteBriefRecommendationCandidate.toPromptBlock(): String =
        buildString {
            appendLine("id: $articleId")
            appendLine("title: $title")
            appendLine("source: $feedName")
            appendLine("publishedAt: $publishedAt")
            appendLine("estimatedDurationMinutes: $estimatedDurationMinutes")
            appendLine("summary: ${summary.take(MAX_RECOMMENDATION_SUMMARY_CHARS)}")
        }.trim()
}

data class CommuteBriefRecommendationCandidate(
    val articleId: String,
    val title: String,
    val feedName: String,
    val publishedAt: String,
    val summary: String,
    val estimatedDurationMinutes: Int,
)

private const val MAX_RECOMMENDATION_SUMMARY_CHARS = 800
private const val COMMUTE_BRIEF_RECOMMENDATION_TIMEOUT_SECONDS = 60L
private const val AI_CONNECTION_TEST_TIMEOUT_SECONDS = 15L
