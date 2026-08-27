import re

with open("app/src/main/java/me/ash/reader/ui/page/settings/ai/AiSettingsPage.kt", "r") as f:
    content = f.read()

# 1. Update ActionItemCard signature and usage
content = content.replace("fun ActionItemCard(title: String, subtitle: String, isDefault: Boolean)", "fun ActionItemCard(title: String, subtitle: String, isDefault: Boolean, onEdit: () -> Unit)")
content = content.replace("IconButton(onClick = {}) {", "IconButton(onClick = onEdit) {")

# 2. Add imports
if "LocalAiSummarizationPrompt" not in content:
    content = content.replace("import me.ash.reader.infrastructure.preference.*", "import me.ash.reader.infrastructure.preference.*\nimport me.ash.reader.infrastructure.preference.LocalAiSummarizationPrompt\nimport me.ash.reader.infrastructure.preference.LocalAiChatPrompt\nimport me.ash.reader.infrastructure.preference.AiSummarizationPromptPreference\nimport me.ash.reader.infrastructure.preference.AiChatPromptPreference")

# 3. Add dialog state variables
dialog_state = """    var apiKeyVisible by remember { mutableStateOf(false) }

    var editPromptType by remember { mutableStateOf<String?>(null) }
    var editPromptValue by remember { mutableStateOf("") }
"""
content = content.replace("    var apiKeyVisible by remember { mutableStateOf(false) }\n", dialog_state)

# 4. Add dialog UI right after if (showAddCustomProviderDialog) { ... }
dialog_ui = """
    if (editPromptType != null) {
        AlertDialog(
            onDismissRequest = { editPromptType = null },
            title = { Text(if (editPromptType == "summary") "Edit AI Summary Prompt" else "Edit Article Prompt") },
            text = {
                OutlinedTextField(
                    value = editPromptValue,
                    onValueChange = { editPromptValue = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (editPromptType == "summary") {
                            context.dataStore.put("aiSummarizationPrompt", editPromptValue)
                        } else {
                            context.dataStore.put("aiChatPrompt", editPromptValue)
                        }
                        editPromptType = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editPromptType = null }) { Text("Cancel") }
            }
        )
    }
"""
content = content.replace("    if (showAddCustomProviderDialog) {", dialog_ui + "\n    if (showAddCustomProviderDialog) {")

# 5. Bring Locals into scope
locals_code = """
    val aiSummarizationPrompt = LocalAiSummarizationPrompt.current
    val aiChatPrompt = LocalAiChatPrompt.current
"""
content = content.replace("    val aiBaseUrl = LocalAiBaseUrl.current", locals_code + "\n    val aiBaseUrl = LocalAiBaseUrl.current")

# 6. Update the ActionItemCard calls
action_item_1 = """                ActionItemCard(
                    title = "AI Summary",
                    subtitle = "Classify the article titled \\\"[ti...",
                    isDefault = true
                )"""

new_action_item_1 = """                ActionItemCard(
                    title = "AI Summary",
                    subtitle = aiSummarizationPrompt.value.ifBlank { "Classify the article titled \\\"[ti..." },
                    isDefault = aiSummarizationPrompt.value == AiSummarizationPromptPreference.default.value,
                    onEdit = {
                        editPromptValue = aiSummarizationPrompt.value
                        editPromptType = "summary"
                    }
                )"""

action_item_2 = """                ActionItemCard(
                    title = "Article",
                    subtitle = "Please generate a comprehe...",
                    isDefault = true
                )"""

new_action_item_2 = """                ActionItemCard(
                    title = "Article",
                    subtitle = aiChatPrompt.value.ifBlank { "Please generate a comprehe..." },
                    isDefault = aiChatPrompt.value == AiChatPromptPreference.default.value,
                    onEdit = {
                        editPromptValue = aiChatPrompt.value
                        editPromptType = "chat"
                    }
                )"""

content = content.replace(action_item_1, new_action_item_1)
content = content.replace(action_item_2, new_action_item_2)

with open("app/src/main/java/me/ash/reader/ui/page/settings/ai/AiSettingsPage.kt", "w") as f:
    f.write(content)

