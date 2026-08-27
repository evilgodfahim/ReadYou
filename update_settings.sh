#!/bin/bash
sed -i '/val aiBackgroundSummaryBackfillOnSync: AiBackgroundSummaryBackfillOnSyncPreference,/a \
    val customAiProviders: CustomAiProvidersPreference = CustomAiProvidersPreference.default,' app/src/main/java/me/ash/reader/infrastructure/preference/Settings.kt

sed -i '/aiBackgroundSummaryBackfillOnSync = AiBackgroundSummaryBackfillOnSyncPreference.fromPreferences(this)/a \
        customAiProviders = CustomAiProvidersPreference.fromPreferences(this),' app/src/main/java/me/ash/reader/infrastructure/preference/Preference.kt

sed -i '/const val aiChatPrompt = "aiChatPrompt"/a \
        const val customAiProviders = "custom_ai_providers"' app/src/main/java/me/ash/reader/ui/ext/DataStoreExt.kt

sed -i '/StringKey(aiChatPrompt),/a \
                StringKey(customAiProviders),' app/src/main/java/me/ash/reader/ui/ext/DataStoreExt.kt

sed -i '/aiChatPrompt to DataStoreKey(stringPreferencesKey(aiChatPrompt), String::class.java),/a \
                customAiProviders to DataStoreKey(stringPreferencesKey(customAiProviders), String::class.java),' app/src/main/java/me/ash/reader/ui/ext/DataStoreExt.kt

sed -i '/PreferencesKey.aiChatPrompt to settings.aiChatPrompt.value,/a \
        PreferencesKey.customAiProviders to "[]",' app/src/main/java/me/ash/reader/ui/ext/DataStoreExt.kt

