package me.ash.reader.infrastructure.preference

import java.util.Locale

object AiPromptFormatter {

    fun resolveLanguage(customLanguage: String? = null): String {
        if (!customLanguage.isNullOrBlank()) return customLanguage
        val currentLocale = Locale.getDefault()
        val display = currentLocale.getDisplayLanguage(Locale.ENGLISH)
        return display.ifBlank { "English" }
    }

    /**
     * Formats a prompt template by replacing placeholders such as:
     * - {title}, {TITLE}, {article_title}, {Title}
     * - {content}, {CONTENT}, {article_content}, {article}, {text}, {body}
     * - {language}, {LANGUAGE}, {target_language}, {lang}
     * - {feed}, {feed_name}, {source}
     * - {url}, {link}
     */
    fun formatPrompt(
        template: String,
        title: String = "",
        content: String = "",
        language: String = "",
        feedName: String = "",
        url: String = "",
    ): String {
        if (template.isBlank()) return template
        val resolvedLang = if (language.isNotBlank()) language else resolveLanguage()

        var formatted = template

        // Replace language placeholders
        val languagePlaceholders = listOf(
            "{language}", "{LANGUAGE}", "{Language}",
            "{target_language}", "{TARGET_LANGUAGE}", "{Target_Language}",
            "{lang}", "{LANG}"
        )
        for (placeholder in languagePlaceholders) {
            formatted = formatted.replace(placeholder, resolvedLang, ignoreCase = true)
        }

        // Replace title placeholders
        val titlePlaceholders = listOf(
            "{title}", "{TITLE}", "{Title}",
            "{article_title}", "{ARTICLE_TITLE}", "{Article_Title}",
            "{articleTitle}", "{ArticleTitle}"
        )
        for (placeholder in titlePlaceholders) {
            formatted = formatted.replace(placeholder, title, ignoreCase = true)
        }

        // Replace content placeholders
        val contentPlaceholders = listOf(
            "{content}", "{CONTENT}", "{Content}",
            "{article_content}", "{ARTICLE_CONTENT}", "{Article_Content}",
            "{articleContent}", "{ArticleContent}",
            "{article}", "{ARTICLE}", "{Article}",
            "{text}", "{TEXT}", "{Text}",
            "{body}", "{BODY}", "{Body}"
        )
        for (placeholder in contentPlaceholders) {
            formatted = formatted.replace(placeholder, content, ignoreCase = true)
        }

        // Replace feed/source placeholders
        val feedPlaceholders = listOf(
            "{feed}", "{FEED}", "{Feed}",
            "{feed_name}", "{FEED_NAME}", "{Feed_Name}",
            "{feedName}", "{FeedName}",
            "{source}", "{SOURCE}", "{Source}"
        )
        for (placeholder in feedPlaceholders) {
            formatted = formatted.replace(placeholder, feedName, ignoreCase = true)
        }

        // Replace URL placeholders
        val urlPlaceholders = listOf(
            "{url}", "{URL}", "{Url}",
            "{link}", "{LINK}", "{Link}"
        )
        for (placeholder in urlPlaceholders) {
            formatted = formatted.replace(placeholder, url, ignoreCase = true)
        }

        return formatted
    }

    /**
     * Checks if the template already has a content placeholder embedded.
     */
    fun hasContentPlaceholder(template: String): Boolean {
        val contentPlaceholders = listOf(
            "{content}", "{article_content}", "{articlecontent}",
            "{article}", "{text}", "{body}"
        )
        val lower = template.lowercase()
        return contentPlaceholders.any { lower.contains(it) }
    }
}
