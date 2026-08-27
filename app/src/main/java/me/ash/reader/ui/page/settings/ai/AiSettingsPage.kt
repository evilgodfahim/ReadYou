package me.ash.reader.ui.page.settings.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.*
import me.ash.reader.ui.component.base.*
import me.ash.reader.ui.ext.showToast
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

data class ProviderOption(
    val id: String,
    val title: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val isCustom: Boolean = false
)

val BuiltInProviders = listOf(
    ProviderOption("gemini", "Gemini", "https://generativelanguage.googleapis.com/v1beta/openai/", "gemini-3.5-flash-lite"),
    ProviderOption("mistral", "Mistral", "https://api.mistral.ai/v1/", "mistral-large-latest")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsPage(
    aiSettingsViewModel: AiSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    navigateToPresetManager: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val aiBaseUrl = LocalAiBaseUrl.current
    val aiApiKey = LocalAiApiKey.current
    val aiModel = LocalAiModel.current
    val customAiProvidersPref = LocalCustomAiProviders.current
    
    val allProviders = BuiltInProviders + customAiProvidersPref.value.map { 
        ProviderOption(it.id, it.name, it.baseUrl, it.defaultModel, true) 
    }

    var selectedProvider by remember {
        mutableStateOf(
            allProviders.firstOrNull { it.defaultBaseUrl == aiBaseUrl.value } ?: allProviders.first()
        )
    }
    
    var providerDropdownExpanded by remember { mutableStateOf(false) }
    var apiKeyInput by remember(aiApiKey.value) { mutableStateOf(aiApiKey.value) }
    var modelInput by remember(aiModel.value) { mutableStateOf(aiModel.value) }
    var apiKeyVisible by remember { mutableStateOf(false) }

    // Dialog state
    var showAddCustomProviderDialog by remember { mutableStateOf(false) }
    var customProviderName by remember { mutableStateOf("") }
    var customProviderUrl by remember { mutableStateOf("") }
    var customProviderModel by remember { mutableStateOf("") }

    if (showAddCustomProviderDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomProviderDialog = false },
            title = { Text("Add OpenAI Compatible Provider") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customProviderName,
                        onValueChange = { customProviderName = it },
                        label = { Text("Provider Name") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = customProviderUrl,
                        onValueChange = { customProviderUrl = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = customProviderModel,
                        onValueChange = { customProviderModel = it },
                        label = { Text("Default Model") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customProviderName.isNotBlank() && customProviderUrl.isNotBlank()) {
                        val newCustom = CustomAiProvider(
                            id = UUID.randomUUID().toString(),
                            name = customProviderName,
                            baseUrl = customProviderUrl,
                            defaultModel = customProviderModel
                        )
                        val newList = customAiProvidersPref.value + newCustom
                        CustomAiProvidersPreference.State(newList).put(context, scope)
                        
                        val newOption = ProviderOption(newCustom.id, newCustom.name, newCustom.baseUrl, newCustom.defaultModel, true)
                        selectedProvider = newOption
                        
                        scope.launch {
                            context.dataStore.put("aiBaseUrl", AiBaseUrlPreference.normalize(newOption.defaultBaseUrl))
                            context.dataStore.put("aiModel", newOption.defaultModel)
                        }
                        
                        showAddCustomProviderDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomProviderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Features") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Configure actions such as smart summaries, translations, keyword extraction, etc.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Provider",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { 
                        customProviderName = ""
                        customProviderUrl = ""
                        customProviderModel = ""
                        showAddCustomProviderDialog = true 
                    }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom")
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = providerDropdownExpanded,
                    onExpandedChange = { providerDropdownExpanded = !providerDropdownExpanded },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedProvider.title,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = providerDropdownExpanded,
                        onDismissRequest = { providerDropdownExpanded = false }
                    ) {
                        allProviders.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.title) },
                                onClick = {
                                    selectedProvider = provider
                                    providerDropdownExpanded = false
                                    scope.launch {
                                        context.dataStore.put("aiBaseUrl", AiBaseUrlPreference.normalize(provider.defaultBaseUrl))
                                        context.dataStore.put("aiModel", provider.defaultModel)
                                    }
                                },
                                trailingIcon = if (provider.isCustom) {
                                    {
                                        IconButton(onClick = {
                                            val newList = customAiProvidersPref.value.filter { it.id != provider.id }
                                            CustomAiProvidersPreference.State(newList).put(context, scope)
                                            if (selectedProvider.id == provider.id) {
                                                selectedProvider = BuiltInProviders.first()
                                                scope.launch {
                                                    context.dataStore.put("aiBaseUrl", AiBaseUrlPreference.normalize(selectedProvider.defaultBaseUrl))
                                                    context.dataStore.put("aiModel", selectedProvider.defaultModel)
                                                }
                                            }
                                            providerDropdownExpanded = false
                                        }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("${selectedProvider.title} Configuration")
                
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key(s) - Comma separated for random selection") },
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                Icon(
                                    if (apiKeyVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                            IconButton(onClick = { 
                                scope.launch {
                                    context.dataStore.put("aiApiKey", apiKeyInput.trim())
                                }
                                context.showToast("API Key(s) saved")
                            }) {
                                Icon(Icons.Rounded.Save, contentDescription = "Save")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )

                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("Model (Optional)") },
                    trailingIcon = {
                        IconButton(onClick = { 
                            modelInput = selectedProvider.defaultModel
                            scope.launch {
                                context.dataStore.put("aiModel", modelInput)
                            }
                        }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (apiKeyInput.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckBox, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${selectedProvider.title} API configuration saved", color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Actions Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                
                ActionItemCard(
                    title = "AI Summary",
                    subtitle = "Classify the article titled \"[ti...",
                    isDefault = true
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Article Aggregation Prompts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                
                ActionItemCard(
                    title = "Article",
                    subtitle = "Please generate a comprehe...",
                    isDefault = true
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ActionItemCard(title: String, subtitle: String, isDefault: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.DragIndicator, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    if (isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Default", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Rounded.MoreVert, contentDescription = null)
            }
        }
    }
}
