package me.ash.reader.infrastructure.net.openai

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface OpenAiApiService {
    @GET("models")
    suspend fun getModels(): Response<ModelsResponse>

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @POST("responses")
    suspend fun createRawResponse(
        @Body request: OpenAiResponsesRequest
    ): Response<ResponseBody>

    companion object {
        fun getInstance(
            baseUrl: String,
            apiKey: String,
            timeoutSeconds: Long = 30L,
            callTimeoutSeconds: Long? = null,
        ): OpenAiApiService {
            val authInterceptor = Interceptor { chain ->
                val originalRequest: Request = chain.request()
                val requestBuilder: Request.Builder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")

                val request: Request = requestBuilder.build()
                chain.proceed(request)
            }

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            if (callTimeoutSeconds != null) {
                clientBuilder.callTimeout(callTimeoutSeconds, TimeUnit.SECONDS)
            }
            val client = clientBuilder.build()

            return Retrofit.Builder()
                .baseUrl(normalizeBaseUrl(baseUrl))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenAiApiService::class.java)
        }

        internal fun normalizeBaseUrl(baseUrl: String): String {
            val trimmed = baseUrl.trim().trimEnd('/')
            if (trimmed.isBlank()) return "https://api.openai.com/v1/"

            if (trimmed.contains("generativelanguage.googleapis.com", ignoreCase = true)) {
                if (!trimmed.endsWith("/openai", ignoreCase = true)) {
                    val base = if (trimmed.endsWith("/v1beta", ignoreCase = true) || trimmed.endsWith("/v1", ignoreCase = true)) {
                        trimmed
                    } else if (trimmed.contains("/v1beta", ignoreCase = true) || trimmed.contains("/v1", ignoreCase = true)) {
                        trimmed
                    } else {
                        "$trimmed/v1beta"
                    }
                    val withOpenAi = if (base.endsWith("/openai", ignoreCase = true)) base else "$base/openai"
                    return "$withOpenAi/"
                }
            }
            return "$trimmed/"
        }

        fun normalizeModel(model: String, baseUrl: String = ""): String {
            val selected = model.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .randomOrNull() ?: model.trim()

            var result = selected
            val isGemini = baseUrl.contains("generativelanguage.googleapis.com", ignoreCase = true) ||
                    result.contains("gemini", ignoreCase = true)

            if (isGemini && result.startsWith("models/", ignoreCase = true)) {
                result = result.substring("models/".length).trim()
            }
            return result
        }
    }
}
