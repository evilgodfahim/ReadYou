package me.ash.reader.ui.page.settings.ai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.ash.reader.domain.repository.AiChatRepository
import me.ash.reader.domain.repository.AiSummaryRepository
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.AiSummaryPrecomputeWorker
import me.ash.reader.domain.service.PendingAiSummaryEnqueuer
import me.ash.reader.infrastructure.preference.AiConfigPreset
import me.ash.reader.infrastructure.preference.AiConfigPresetState
import me.ash.reader.infrastructure.preference.buildAiConfigPreset
import me.ash.reader.infrastructure.preference.readAiConfigPresetState
import me.ash.reader.infrastructure.preference.readAiProviderCredentialsMap
import me.ash.reader.infrastructure.preference.readLegacyAiConfigPresetState
import me.ash.reader.infrastructure.preference.saveAiProviderCredentialsMap
import me.ash.reader.infrastructure.preference.updateAiConfigPresetState
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.infrastructure.net.ApiResult

private const val DEFAULT_PRESET_COPY_SUFFIX = "Copy"

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val aiSummaryRepository: AiSummaryRepository,
    private val aiChatRepository: AiChatRepository,
    private val accountService: AccountService,
    private val pendingAiSummaryEnqueuer: PendingAiSummaryEnqueuer,
    private val workManager: WorkManager,
) : ViewModel() {

    suspend fun readPresetState(context: Context): AiConfigPresetState {
        val preferences = context.dataStore.data.first()
        return preferences.readAiConfigPresetState()
            ?: preferences.readLegacyAiConfigPresetState()
            ?: AiConfigPresetState(emptyList(), "")
    }

    fun setCurrentPreset(
        context: Context,
        presetId: String,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            context.updateAiConfigPresetState { state ->
                state.copy(currentPresetId = presetId)
            }
            me.ash.reader.infrastructure.preference.AiProviderCredentialsCache.saveAndMerge(
                context = context,
                incomingMap = emptyMap(),
                activeProviderId = presetId,
            )
            onComplete()
        }
    }

    fun savePreset(
        context: Context,
        preset: AiConfigPreset,
        setAsCurrent: Boolean,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            context.updateAiConfigPresetState(
                fallback = AiConfigPresetState(presets = listOf(preset), currentPresetId = preset.id),
            ) { state ->
                val existingIndex = state.presets.indexOfFirst { it.id == preset.id }
                val updatedPresets =
                    if (existingIndex >= 0) {
                        state.presets.toMutableList().apply { this[existingIndex] = preset }
                    } else {
                        state.presets + preset
                    }
                state.copy(
                    presets = updatedPresets,
                    currentPresetId = if (setAsCurrent || state.currentPresetId.isBlank()) preset.id else state.currentPresetId,
                )
            }
            me.ash.reader.infrastructure.preference.AiProviderCredentialsCache.saveAndMerge(
                context = context,
                incomingMap = mapOf(
                    preset.id to me.ash.reader.infrastructure.preference.AiProviderCredentials(
                        providerId = preset.id,
                        apiKey = preset.apiKey,
                        model = preset.model,
                        baseUrl = preset.baseUrl,
                    )
                ),
                activeProviderId = if (setAsCurrent) preset.id else "",
            )
            if (setAsCurrent) {
                context.dataStore.put(me.ash.reader.ui.ext.DataStoreKey.aiApiKey, preset.apiKey)
                context.dataStore.put(me.ash.reader.ui.ext.DataStoreKey.aiModel, preset.model)
                context.dataStore.put(me.ash.reader.ui.ext.DataStoreKey.aiBaseUrl, preset.baseUrl)
            }
            onComplete()
        }
    }

    fun deletePreset(
        context: Context,
        presetId: String,
        onComplete: () -> Unit = {},
    ) {
        viewModelScope.launch {
            context.updateAiConfigPresetState { state ->
                val remaining = state.presets.filterNot { it.id == presetId }
                state.copy(
                    presets = remaining,
                    currentPresetId = remaining.firstOrNull()?.id.orEmpty(),
                )
            }
            onComplete()
        }
    }

    fun persistAiConfiguration(
        context: Context,
        providerId: String = "default_preset",
        apiKey: String,
        model: String,
        baseUrl: String,
        providerTitle: String,
        existingCredentialsMap: Map<String, me.ash.reader.infrastructure.preference.AiProviderCredentials> = emptyMap(),
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
            me.ash.reader.infrastructure.preference.AiProviderCredentialsCache.ensureLoaded(context)

            val incoming = existingCredentialsMap.toMutableMap()
            val existingTarget = me.ash.reader.infrastructure.preference.AiProviderCredentialsCache.getCredential(providerId)
            val finalApiKey = if (apiKey.isNotBlank()) apiKey else existingTarget?.apiKey.orEmpty()
            val finalModel = if (model.isNotBlank()) model else existingTarget?.model.orEmpty()
            val finalBaseUrl = if (baseUrl.isNotBlank()) baseUrl else existingTarget?.baseUrl.orEmpty()

            incoming[providerId] = me.ash.reader.infrastructure.preference.AiProviderCredentials(
                providerId = providerId,
                apiKey = finalApiKey,
                model = finalModel,
                baseUrl = finalBaseUrl,
            )

            val updatedMap = me.ash.reader.infrastructure.preference.AiProviderCredentialsCache.saveAndMerge(
                context = context,
                incomingMap = incoming,
                activeProviderId = providerId,
            )

            val currentCred = updatedMap[providerId]
            val effectiveApiKey = currentCred?.apiKey ?: finalApiKey
            val effectiveModel = currentCred?.model ?: finalModel
            val effectiveBaseUrl = currentCred?.baseUrl ?: finalBaseUrl

            context.dataStore.put(me.ash.reader.ui.ext.DataStoreKey.aiApiKey, effectiveApiKey)
            context.dataStore.put(me.ash.reader.ui.ext.DataStoreKey.aiModel, effectiveModel)
            context.dataStore.put(me.ash.reader.ui.ext.DataStoreKey.aiBaseUrl, effectiveBaseUrl)

            context.updateAiConfigPresetState { currentState ->
                val currentPresetId = providerId.ifBlank { currentState.currentPresetId.ifBlank { "default_preset" } }
                val exists = currentState.presets.any { it.id == currentPresetId }
                val updatedPresets = if (exists) {
                    currentState.presets.map { preset ->
                        if (preset.id == currentPresetId) {
                            preset.copy(
                                baseUrl = effectiveBaseUrl,
                                apiKey = effectiveApiKey,
                                model = effectiveModel,
                                provider = providerTitle,
                            )
                        } else preset
                    }
                } else {
                    currentState.presets + AiConfigPreset(
                        id = currentPresetId,
                        name = providerTitle,
                        baseUrl = effectiveBaseUrl,
                        apiKey = effectiveApiKey,
                        model = effectiveModel,
                        provider = providerTitle,
                    )
                }
                currentState.copy(
                    presets = updatedPresets,
                    currentPresetId = currentPresetId,
                )
            }
        }
    }

    fun duplicatePreset(source: AiConfigPreset): AiConfigPreset =
        buildAiConfigPreset(
            name = "${source.name} ${DEFAULT_PRESET_COPY_SUFFIX}",
            baseUrl = source.baseUrl,
            apiKey = source.apiKey,
            model = source.model,
        )

    fun fetchModels(
        baseUrl: String,
        apiKey: String,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            when (val result = aiSummaryRepository.fetchAvailableModels(baseUrl, apiKey)) {
                is ApiResult.Success -> onSuccess(result.data)
                is ApiResult.BizError -> onError(result.exception.message ?: "Business error")
                is ApiResult.NetworkError -> onError(result.exception.message ?: "Network error")
                is ApiResult.UnknownError -> onError(result.throwable.message ?: "Unknown error")
            }
        }
    }

    fun testConnection(
        baseUrl: String,
        apiKey: String,
        model: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            when (val result = aiSummaryRepository.testAiServiceConnection(baseUrl, apiKey, model)) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.BizError -> onError(result.exception.message ?: "Business error")
                is ApiResult.NetworkError -> onError(result.exception.message ?: "Network error")
                is ApiResult.UnknownError -> onError(result.throwable.message ?: "Unknown error")
            }
        }
    }

    fun testWebSearch(
        baseUrl: String,
        apiKey: String,
        model: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            when (val result = aiChatRepository.testWebSearch(baseUrl, apiKey, model)) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.BizError -> onError(result.exception.message ?: "Business error")
                is ApiResult.NetworkError -> onError(result.exception.message ?: "Network error")
                is ApiResult.UnknownError -> onError(result.throwable.message ?: "Unknown error")
            }
        }
    }

    fun enqueueUnreadSummaryBackfill(onResult: (PendingAiSummaryEnqueuer.BackfillResult) -> Unit) {
        viewModelScope.launch {
            val accountId = accountService.getCurrentAccountId()
            val result =
                pendingAiSummaryEnqueuer.enqueueUnreadBackfill(
                    accountId = accountId,
                    requireBackfillOnSync = false,
                )
            if (result is PendingAiSummaryEnqueuer.BackfillResult.Enqueued && result.count > 0) {
                AiSummaryPrecomputeWorker.enqueueOneTimeWork(workManager, accountId)
            }
            onResult(result)
        }
    }
}
