package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

data class AiProviderCredentials(
    val providerId: String,
    val apiKey: String,
    val model: String,
    val baseUrl: String = "",
)

private val gson = Gson()
private val providerMapType = object : TypeToken<Map<String, AiProviderCredentials>>() {}.type
private val providerCredentialsKey = stringPreferencesKey("ai_provider_credentials_map")
private val activeProviderIdKey = stringPreferencesKey("ai_active_provider_id")

fun Preferences.readAiProviderCredentialsMap(): Map<String, AiProviderCredentials> {
    val json = this[providerCredentialsKey].orEmpty()
    if (json.isBlank()) return emptyMap()
    return runCatching {
        gson.fromJson<Map<String, AiProviderCredentials>>(json, providerMapType)
    }.getOrDefault(emptyMap())
}

fun Preferences.readActiveProviderId(): String {
    return this[activeProviderIdKey].orEmpty()
}

suspend fun Context.saveAiProviderCredentialsMap(
    map: Map<String, AiProviderCredentials>,
    activeProviderId: String
) {
    dataStore.put("ai_provider_credentials_map", gson.toJson(map))
    dataStore.put("ai_active_provider_id", activeProviderId)
}
