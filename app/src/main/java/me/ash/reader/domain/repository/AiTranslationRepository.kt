package me.ash.reader.domain.repository

import javax.inject.Inject
import javax.inject.Singleton
import me.ash.reader.infrastructure.net.ApiResult
import me.ash.reader.infrastructure.net.openai.ChatCompletionRequest
import me.ash.reader.infrastructure.net.openai.ChatMessage
import me.ash.reader.infrastructure.net.openai.OpenAiApiService
import me.ash.reader.ui.page.home.reading.TranslationSourceBlock

@Singleton
class AiTranslationRepository @Inject constructor() {

    suspend fun translateBlocks(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        sourceBlocks: List<TranslationSourceBlock>,
    ): ApiResult<List<TranslatedArticleBlock>> {
        return try {
            val effectiveBaseUrl = OpenAiApiService.normalizeBaseUrl(baseUrl)
            val effectiveModel = OpenAiApiService.normalizeModel(model, effectiveBaseUrl)
            val effectiveApiKey = apiKey.split(",").map { it.trim() }.filter { it.isNotBlank() }.randomOrNull() ?: apiKey.trim()
            val service = OpenAiApiService.getInstance(
                baseUrl = effectiveBaseUrl,
                apiKey = effectiveApiKey,
                timeoutSeconds = 60L,
                callTimeoutSeconds = 60L,
            )
            val translatedBlocks = mutableListOf<TranslatedArticleBlock>()
            val chunks = TranslationRequestChunker.chunk(sourceBlocks)

            for (chunk in chunks) {
                val payloadJson = ArticleTranslationPayloadCodec.encodeSourceBlocks(chunk)
                val request = ChatCompletionRequest(
                    model = effectiveModel,
                    messages = buildTranslationMessages(prompt = prompt, payloadJson = payloadJson),
                    temperature = 0.2,
                    maxTokens = 1200,
                )
                val response = service.createChatCompletion(request)
                if (!response.isSuccessful || response.body() == null) {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    return ApiResult.BizError(Exception(errorMsg))
                }
                val choices = response.body()!!.choices
                if (choices.isEmpty()) {
                    return ApiResult.BizError(Exception("No choices returned from API"))
                }
                when (
                    val parsedResult =
                        ArticleTranslationPayloadCodec.decodeTranslatedBlocks(
                            rawContent = choices.first().message.content,
                            expectedIds = chunk.map { it.id }.toSet(),
                        )
                ) {
                    is ApiResult.Success -> translatedBlocks += parsedResult.data
                    is ApiResult.BizError -> return parsedResult
                    is ApiResult.NetworkError -> return parsedResult
                    is ApiResult.UnknownError -> return parsedResult
                }
            }

            ApiResult.Success(translatedBlocks)
        } catch (error: Exception) {
            ApiResult.NetworkError(error)
        }
    }

    internal fun buildTranslationMessages(
        prompt: String,
        payloadJson: String,
    ): List<ChatMessage> =
        listOf(
            ChatMessage(
                role = "system",
                content =
                    """
                    你是一个翻译结果生成器。

                    输出要求：
                    - Return ONLY valid JSON, with no code blocks, explanations, or extra text
                    - Input is a JSON array
                    - Preserve the id of each item
                    - Maintain original array order
                    - Do not omit any items
                    - Do not summarize or alter formatting
                    - Fill the translatedText field for each item
                    - translatedText must be accurate and natural English
                    """.trimIndent(),
            ),
            ChatMessage(
                role = "user",
                content =
                    """
                    翻译要求：
                    $prompt

                    待翻译内容（JSON）：
                    $payloadJson
                    """.trimIndent(),
            ),
        )
}
