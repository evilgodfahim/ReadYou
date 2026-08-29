package me.ash.reader.infrastructure.preference

import me.ash.reader.infrastructure.net.openai.OpenAiApiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiProviderCredentialsTest {

    @Test
    fun testGeminiBaseUrlAndModelNormalization() {
        val geminiBase = "https://generativelanguage.googleapis.com/v1beta"
        val normalizedBase = OpenAiApiService.normalizeBaseUrl(geminiBase)
        assertEquals("https://generativelanguage.googleapis.com/v1beta/openai/", normalizedBase)

        val modelWithPrefix = "models/gemini-2.0-flash"
        val normalizedModel = OpenAiApiService.normalizeModel(modelWithPrefix, normalizedBase)
        assertEquals("gemini-2.0-flash", normalizedModel)

        // User-provided model should NOT be overridden with hardcoded model
        val customModel = "gemini-2.5-flash"
        val normalizedCustom = OpenAiApiService.normalizeModel(customModel, normalizedBase)
        assertEquals("gemini-2.5-flash", normalizedCustom)

        // Comma-separated models should pick one of the user-provided models
        val commaModels = "gemini-2.5-flash, gemini-2.0-flash, gemini-1.5-pro"
        val pickedModel = OpenAiApiService.normalizeModel(commaModels, normalizedBase)
        assertTrue(listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-pro").contains(pickedModel))
    }

    @Test
    fun addingOneCredentialPreservesExistingOtherCredentialsInMemory() {
        val initialMap = mapOf(
            "openai" to AiProviderCredentials("openai", "sk-openai-123", "gpt-4o", "https://api.openai.com/v1/"),
            "gemini" to AiProviderCredentials("gemini", "ai-gemini-456", "gemini-1.5-pro", "https://generativelanguage.googleapis.com/")
        )

        AiProviderCredentialsCache.updateMemoryCacheOnly(initialMap, "openai")

        // Check both exist
        val cachedBefore = AiProviderCredentialsCache.getCachedMap()
        assertEquals(2, cachedBefore.size)
        assertEquals("sk-openai-123", cachedBefore["openai"]?.apiKey)
        assertEquals("ai-gemini-456", cachedBefore["gemini"]?.apiKey)

        // Now simulate adding a 3rd credential for Claude
        val newMap = mapOf(
            "claude" to AiProviderCredentials("claude", "sk-ant-789", "claude-3-5-sonnet", "https://api.anthropic.com/v1/")
        )
        AiProviderCredentialsCache.updateMemoryCacheOnly(cachedBefore + newMap, "claude")

        val cachedAfter = AiProviderCredentialsCache.getCachedMap()
        // Must contain all 3 providers without losing any!
        assertEquals(3, cachedAfter.size)
        assertEquals("sk-openai-123", cachedAfter["openai"]?.apiKey)
        assertEquals("ai-gemini-456", cachedAfter["gemini"]?.apiKey)
        assertEquals("sk-ant-789", cachedAfter["claude"]?.apiKey)
        assertEquals("claude", AiProviderCredentialsCache.getCachedActiveProviderId())
    }

    @Test
    fun commaSeparatedApiKeysRandomRotationWorks() {
        val creds = AiProviderCredentials(
            providerId = "openai",
            apiKey = "key-1, key-2, key-3",
            model = "gpt-4o",
            baseUrl = ""
        )
        val selected = creds.randomApiKey
        assertTrue(listOf("key-1", "key-2", "key-3").contains(selected))
    }

    @Test
    fun commaSeparatedModelsRandomRotationWorks() {
        val creds = AiProviderCredentials(
            providerId = "gemini",
            apiKey = "key-1",
            model = "gemini-2.5-flash, gemini-2.0-flash, gemini-1.5-flash",
            baseUrl = ""
        )
        val selected = creds.randomModel
        assertTrue(listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash").contains(selected))
    }
}
