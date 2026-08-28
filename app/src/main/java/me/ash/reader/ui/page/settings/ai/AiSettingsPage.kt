package me.ash.reader.ui.page.settings.ai
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.util.UUID
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.AiBaseUrlPreference
import me.ash.reader.infrastructure.preference.AiChatPromptPreference
import me.ash.reader.infrastructure.preference.AiSummarizationPromptPreference
import me.ash.reader.infrastructure.preference.CustomAiProvider
import me.ash.reader.infrastructure.preference.CustomAiProvidersPreference
import me.ash.reader.infrastructure.preference.LocalAiApiKey
import me.ash.reader.infrastructure.preference.LocalAiBaseUrl
import me.ash.reader.infrastructure.preference.LocalAiChatPrompt
import me.ash.reader.infrastructure.preference.LocalAiModel
import me.ash.reader.infrastructure.preference.LocalAiSummarizationPrompt
import me.ash.reader.infrastructure.preference.LocalCustomAiProviders
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.theme.palette.onLight

data class ProviderOption(
    val id: String,
    val title: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val isCustom: Boolean = false,
)

val BuiltInProviders =
    listOf(
        ProviderOption(
            "gemini",
            "Gemini",
            "https://generativelanguage.googleapis.com/v1beta/openai/",
            "gemini-2.5-flash",
        ),
        ProviderOption(
            "mistral",
            "Mistral",
            "https://api.mistral.ai/v1/",
            "mistral-large-latest",
        ),
        ProviderOption(
            "deepseek",
            "DeepSeek",
            "https://api.deepseek.com/v1/",
            "deepseek-chat",
        ),
        ProviderOption(
            "openai",
            "OpenAI",
            "https://api.openai.com/v1/",
            "gpt-4o-mini",
        ),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsPage(
    aiSettingsViewModel: AiSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    navigateToPresetManager: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val aiSummarizationPrompt = LocalAiSummarizationPrompt.current
    val aiChatPrompt = LocalAiChatPrompt.current

    val aiBaseUrl = LocalAiBaseUrl.current
    val aiApiKey = LocalAiApiKey.current
    val aiModel = LocalAiModel.current
    val customAiProvidersPref = LocalCustomAiProviders.current

    val allProviders =
        remember(customAiProvidersPref.value) {
            BuiltInProviders +
                customAiProvidersPref.value.map {
                    ProviderOption(it.id, it.name, it.baseUrl, it.defaultModel, true)
                }
        }

    var selectedProvider by
        remember(aiBaseUrl.value, allProviders) {
            mutableStateOf(
                allProviders.firstOrNull {
                    AiBaseUrlPreference.normalize(it.defaultBaseUrl) ==
                        AiBaseUrlPreference.normalize(aiBaseUrl.value)
                } ?: allProviders.first()
            )
        }

    var providerDropdownExpanded by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(aiApiKey.value) }
    var modelInput by remember { mutableStateOf(aiModel.value) }
    var apiKeyVisible by remember { mutableStateOf(false) }

    LaunchedEffect(aiApiKey.value) {
        if (apiKeyInput.isEmpty() && aiApiKey.value.isNotEmpty()) {
            apiKeyInput = aiApiKey.value
        }
    }

    LaunchedEffect(aiModel.value) {
        if (modelInput.isEmpty() && aiModel.value.isNotEmpty()) {
            modelInput = aiModel.value
        }
    }

    fun saveCurrentConfiguration(
        key: String = apiKeyInput,
        model: String = modelInput,
        provider: ProviderOption = selectedProvider,
    ) {
        val trimmedKey = key.trim()
        val trimmedModel = model.trim().ifEmpty { provider.defaultModel }
        val normalizedUrl = AiBaseUrlPreference.normalize(provider.defaultBaseUrl)

        aiSettingsViewModel.persistAiConfiguration(
            context = context.applicationContext,
            apiKey = trimmedKey,
            model = trimmedModel,
            baseUrl = normalizedUrl,
            providerTitle = provider.title,
        )
    }

    BackHandler {
        saveCurrentConfiguration()
        onBack()
    }

    DisposableEffect(Unit) {
        onDispose {
            saveCurrentConfiguration()
        }
    }

    var editPromptType by remember { mutableStateOf<String?>(null) }
    var editPromptValue by remember { mutableStateOf("") }

    // Dialog state
    var showAddCustomProviderDialog by remember { mutableStateOf(false) }
    var customProviderName by remember { mutableStateOf("") }
    var customProviderUrl by remember { mutableStateOf("") }
    var customProviderModel by remember { mutableStateOf("") }

    if (editPromptType != null) {
        AlertDialog(
            onDismissRequest = { editPromptType = null },
            title = {
                Text(
                    if (editPromptType == "summary") "Edit AI Summary Prompt"
                    else "Edit Article Prompt",
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                OutlinedTextField(
                    value = editPromptValue,
                    onValueChange = { editPromptValue = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    minLines = 3,
                    maxLines = 8,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (editPromptType == "summary") {
                                context.dataStore.put(
                                    DataStoreKey.aiSummarizationPrompt,
                                    editPromptValue,
                                )
                            } else {
                                context.dataStore.put(
                                    DataStoreKey.aiChatPrompt,
                                    editPromptValue,
                                )
                            }
                            editPromptType = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editPromptType = null }) { Text("Cancel") }
            },
        )
    }

    if (showAddCustomProviderDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomProviderDialog = false },
            title = {
                Text(
                    "Add OpenAI Compatible Provider",
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = customProviderName,
                        onValueChange = { customProviderName = it },
                        label = { Text("Provider Name") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = customProviderUrl,
                        onValueChange = { customProviderUrl = it },
                        label = { Text("Base URL (e.g. https://api.openai.com/v1/)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = customProviderModel,
                        onValueChange = { customProviderModel = it },
                        label = { Text("Default Model") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customProviderName.isNotBlank() && customProviderUrl.isNotBlank()) {
                            val newCustom =
                                CustomAiProvider(
                                    id = UUID.randomUUID().toString(),
                                    name = customProviderName.trim(),
                                    baseUrl = customProviderUrl.trim(),
                                    defaultModel = customProviderModel.trim(),
                                )
                            val newList = customAiProvidersPref.value + newCustom
                            CustomAiProvidersPreference.State(newList).put(context, scope)

                            val newOption =
                                ProviderOption(
                                    newCustom.id,
                                    newCustom.name,
                                    newCustom.baseUrl,
                                    newCustom.defaultModel,
                                    true,
                                )
                            selectedProvider = newOption
                            if (modelInput.isBlank()) {
                                modelInput = newOption.defaultModel
                            }
                            saveCurrentConfiguration(provider = newOption)
                            showAddCustomProviderDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomProviderDialog = false }) { Text("Cancel") }
            },
        )
    }

    RYScaffold(
        containerColor =
            MaterialTheme.colorScheme.surface onLight
                MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    saveCurrentConfiguration()
                    onBack()
                },
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 64.dp),
            ) {
                item {
                    DisplayText(
                        text = "AI Features",
                        desc = "Configure actions such as smart summaries, translations, keyword extraction, and models.",
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Subtitle(
                            modifier = Modifier.weight(1f),
                            text = "AI Provider",
                        )
                        TextButton(
                            onClick = {
                                customProviderName = ""
                                customProviderUrl = ""
                                customProviderModel = ""
                                showAddCustomProviderDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Custom")
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = providerDropdownExpanded,
                        onExpandedChange = {
                            providerDropdownExpanded = !providerDropdownExpanded
                        },
                        modifier =
                            Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = selectedProvider.title,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = providerDropdownExpanded
                                )
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        ExposedDropdownMenu(
                            expanded = providerDropdownExpanded,
                            onDismissRequest = { providerDropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        ) {
                            allProviders.forEach { provider ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            provider.title,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    },
                                    onClick = {
                                        selectedProvider = provider
                                        providerDropdownExpanded = false
                                        if (modelInput.isBlank() || allProviders.any { it.defaultModel == modelInput }) {
                                            modelInput = provider.defaultModel
                                        }
                                        saveCurrentConfiguration(model = modelInput, provider = provider)
                                    },
                                    trailingIcon =
                                        if (provider.isCustom) {
                                            {
                                                IconButton(
                                                    onClick = {
                                                        val newList =
                                                            customAiProvidersPref.value.filter {
                                                                it.id != provider.id
                                                            }
                                                        CustomAiProvidersPreference.State(newList)
                                                            .put(context, scope)
                                                        if (selectedProvider.id == provider.id) {
                                                            selectedProvider =
                                                                BuiltInProviders.first()
                                                            saveCurrentConfiguration(
                                                                provider = selectedProvider
                                                            )
                                                        }
                                                        providerDropdownExpanded = false
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Rounded.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                    )
                                                }
                                            }
                                        } else null,
                                )
                            }
                        }
                    }
                }

                item {
                    Subtitle(text = "${selectedProvider.title} Configuration")

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            saveCurrentConfiguration(key = it)
                        },
                        label = { Text("API Key(s)") },
                        visualTransformation =
                            if (apiKeyVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                    Icon(
                                        if (apiKeyVisible) Icons.Rounded.Visibility
                                        else Icons.Rounded.VisibilityOff,
                                        contentDescription = "Toggle visibility",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        saveCurrentConfiguration()
                                        context.showToast("API Key(s) saved")
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Save,
                                        contentDescription = "Save",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier =
                            Modifier.fillMaxWidth()
                                .animateContentSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                    )

                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = {
                            modelInput = it
                            saveCurrentConfiguration(model = it)
                        },
                        label = { Text("Model Name") },
                        placeholder = { Text(selectedProvider.defaultModel) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        modelInput = selectedProvider.defaultModel
                                        saveCurrentConfiguration(model = modelInput)
                                        context.showToast("Model reset to default")
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Refresh,
                                        contentDescription = "Reset to default",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        saveCurrentConfiguration()
                                        context.showToast("Model saved")
                                    }
                                ) {
                                    Icon(
                                        Icons.Rounded.Save,
                                        contentDescription = "Save",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                    )

                    if (apiKeyInput.isNotBlank()) {
                        Row(
                            modifier =
                                Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${selectedProvider.title} configuration active & saved",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                item {
                    Subtitle(text = "AI Prompts Management")

                    ActionItemCard(
                        title = "AI Summary",
                        subtitle =
                            aiSummarizationPrompt.value.ifBlank {
                                "Default structured summary prompt"
                            },
                        isDefault =
                            aiSummarizationPrompt.value ==
                                AiSummarizationPromptPreference.default.value,
                        onEdit = {
                            editPromptValue = aiSummarizationPrompt.value
                            editPromptType = "summary"
                        },
                    )

                    ActionItemCard(
                        title = "Article Chat / Aggregation",
                        subtitle =
                            aiChatPrompt.value.ifBlank {
                                "Default reading assistant prompt"
                            },
                        isDefault =
                            aiChatPrompt.value == AiChatPromptPreference.default.value,
                        onEdit = {
                            editPromptValue = aiChatPrompt.value
                            editPromptType = "chat"
                        },
                    )
                }
            }
        },
    )
}

@Composable
fun ActionItemCard(
    title: String,
    subtitle: String,
    isDefault: Boolean,
    onEdit: () -> Unit,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                "Default",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                softWrap = false,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
