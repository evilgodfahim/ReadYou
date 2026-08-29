package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.ash.reader.ui.ext.dataStore

data class AiProviderCredentials(
    val providerId: String,
    val apiKey: String,
    val model: String,
    val baseUrl: String = "",
) {
    val randomApiKey: String
        get() = apiKey.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .randomOrNull() ?: apiKey.trim()

    val randomModel: String
        get() = model.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .randomOrNull() ?: model.trim()
}

private val gson = Gson()
private val providerMapType = object : TypeToken<Map<String, AiProviderCredentials>>() {}.type
internal val providerCredentialsKey = stringPreferencesKey("ai_provider_credentials_map")
internal val activeProviderIdKey = stringPreferencesKey("ai_active_provider_id")

object AiProviderCredentialsCache {
    private val mutex = Mutex()
    private val cache = ConcurrentHashMap<String, AiProviderCredentials>()
    @Volatile
    private var activeIdCache: String = ""
    @Volatile
    private var isLoaded: Boolean = false

    fun isLoaded(): Boolean = isLoaded

    fun getCachedMap(): Map<String, AiProviderCredentials> = cache.toMap()

    fun getCachedActiveProviderId(): String = activeIdCache

    fun getCredential(providerId: String): AiProviderCredentials? = cache[providerId]

    fun updateMemoryCacheOnly(
        map: Map<String, AiProviderCredentials>,
        activeProviderId: String? = null,
    ) {
        cache.putAll(map)
        if (!activeProviderId.isNullOrBlank()) {
            activeIdCache = activeProviderId
        }
        isLoaded = true
    }

    suspend fun ensureLoaded(context: Context) {
        if (isLoaded) return
        mutex.withLock {
            if (isLoaded) return
            try {
                val prefs = context.dataStore.data.first()
                val diskMap = prefs.parseDiskMap()
                val diskActiveId = prefs[activeProviderIdKey].orEmpty()
                cache.putAll(diskMap)
                if (diskActiveId.isNotBlank()) {
                    activeIdCache = diskActiveId
                }
                isLoaded = true
            } catch (_: Exception) {
                // If read fails, keep going
            }
        }
    }

    suspend fun saveAndMerge(
        context: Context,
        incomingMap: Map<String, AiProviderCredentials>,
        activeProviderId: String,
    ): Map<String, AiProviderCredentials> {
        return mutex.withLock {
            if (!isLoaded) {
                try {
                    val prefs = context.dataStore.data.first()
                    val diskMap = prefs.parseDiskMap()
                    val diskActiveId = prefs[activeProviderIdKey].orEmpty()
                    cache.putAll(diskMap)
                    if (diskActiveId.isNotBlank() && activeIdCache.isBlank()) {
                        activeIdCache = diskActiveId
                    }
                    isLoaded = true
                } catch (_: Exception) {}
            }

            // Merge incoming into cache - preserve existing providers
            incomingMap.forEach { (id, creds) ->
                val existing = cache[id]
                if (existing == null) {
                    cache[id] = creds
                } else {
                    cache[id] = existing.copy(
                        apiKey = if (creds.apiKey.isNotBlank()) creds.apiKey else existing.apiKey,
                        model = if (creds.model.isNotBlank()) creds.model else existing.model,
                        baseUrl = if (creds.baseUrl.isNotBlank()) creds.baseUrl else existing.baseUrl,
                    )
                }
            }

            if (activeProviderId.isNotBlank()) {
                activeIdCache = activeProviderId
            }

            val finalSnapshot = cache.toMap()
            val targetActiveId = activeIdCache

            try {
                context.dataStore.edit { preferences ->
                    preferences[providerCredentialsKey] = gson.toJson(finalSnapshot)
                    if (targetActiveId.isNotBlank()) {
                        preferences[activeProviderIdKey] = targetActiveId
                    }
                }
            } catch (_: Exception) {}

            finalSnapshot
        }
    }

    suspend fun updateSingleProvider(
        context: Context,
        providerId: String,
        apiKey: String,
        model: String,
        baseUrl: String,
        setAsActive: Boolean = true,
    ): Map<String, AiProviderCredentials> {
        return mutex.withLock {
            if (!isLoaded) {
                try {
                    val prefs = context.dataStore.data.first()
                    val diskMap = prefs.parseDiskMap()
                    val diskActiveId = prefs[activeProviderIdKey].orEmpty()
                    cache.putAll(diskMap)
                    if (diskActiveId.isNotBlank() && activeIdCache.isBlank()) {
                        activeIdCache = diskActiveId
                    }
                    isLoaded = true
                } catch (_: Exception) {}
            }

            val existing = cache[providerId]
            val finalKey = if (apiKey.isNotBlank()) apiKey else existing?.apiKey.orEmpty()
            val finalModel = if (model.isNotBlank()) model else existing?.model.orEmpty()
            val finalBaseUrl = if (baseUrl.isNotBlank()) baseUrl else existing?.baseUrl.orEmpty()

            cache[providerId] = AiProviderCredentials(
                providerId = providerId,
                apiKey = finalKey,
                model = finalModel,
                baseUrl = finalBaseUrl,
            )

            if (setAsActive && providerId.isNotBlank()) {
                activeIdCache = providerId
            }

            val finalSnapshot = cache.toMap()
            val targetActiveId = activeIdCache

            try {
                context.dataStore.edit { preferences ->
                    preferences[providerCredentialsKey] = gson.toJson(finalSnapshot)
                    if (targetActiveId.isNotBlank()) {
                        preferences[activeProviderIdKey] = targetActiveId
                    }
                }
            } catch (_: Exception) {}

            finalSnapshot
        }
    }
}

private fun Preferences.parseDiskMap(): Map<String, AiProviderCredentials> {
    val json = this[providerCredentialsKey].orEmpty()
    if (json.isBlank()) return emptyMap()
    return runCatching {
        gson.fromJson<Map<String, AiProviderCredentials>>(json, providerMapType)
    }.getOrDefault(emptyMap())
}

fun Preferences.readAiProviderCredentialsMap(): Map<String, AiProviderCredentials> {
    val cached = AiProviderCredentialsCache.getCachedMap()
    val disk = parseDiskMap()
    if (disk.isNotEmpty()) {
        AiProviderCredentialsCache.updateMemoryCacheOnly(disk)
    }
    return if (cached.isEmpty()) disk else (disk + cached)
}

fun Preferences.readActiveProviderId(): String {
    val cached = AiProviderCredentialsCache.getCachedActiveProviderId()
    if (cached.isNotBlank()) return cached
    val disk = this[activeProviderIdKey].orEmpty()
    if (disk.isNotBlank()) {
        AiProviderCredentialsCache.updateMemoryCacheOnly(emptyMap(), disk)
    }
    return disk
}

fun Context.readAiProviderCredentialsMap(): Map<String, AiProviderCredentials> {
    val cached = AiProviderCredentialsCache.getCachedMap()
    if (cached.isNotEmpty()) return cached
    return runBlocking {
        dataStore.data.first().readAiProviderCredentialsMap()
    }
}

fun Context.readActiveProviderId(): String {
    val cached = AiProviderCredentialsCache.getCachedActiveProviderId()
    if (cached.isNotBlank()) return cached
    return runBlocking {
        dataStore.data.first().readActiveProviderId()
    }
}

suspend fun Context.saveAiProviderCredentialsMap(
    map: Map<String, AiProviderCredentials>,
    activeProviderId: String
) {
    AiProviderCredentialsCache.saveAndMerge(this, map, activeProviderId)
}
