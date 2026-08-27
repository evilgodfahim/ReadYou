package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val customAiProvidersKey = stringPreferencesKey("custom_ai_providers")

data class CustomAiProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val defaultModel: String
)

val LocalCustomAiProviders =
    compositionLocalOf<CustomAiProvidersPreference> { CustomAiProvidersPreference.default }

sealed class CustomAiProvidersPreference(val value: List<CustomAiProvider>) : Preference() {
    class State(value: List<CustomAiProvider>) : CustomAiProvidersPreference(value)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            val json = Gson().toJson(value)
            context.dataStore.put("custom_ai_providers", json)
        }
    }

    companion object {
        val default = State(emptyList())

        fun fromPreferences(preferences: Preferences): CustomAiProvidersPreference {
            val json = preferences[customAiProvidersKey] ?: return default
            val type = object : TypeToken<List<CustomAiProvider>>() {}.type
            return try {
                State(Gson().fromJson(json, type) ?: emptyList())
            } catch (e: Exception) {
                default
            }
        }
    }
}
