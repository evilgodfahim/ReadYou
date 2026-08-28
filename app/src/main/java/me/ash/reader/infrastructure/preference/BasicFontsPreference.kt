package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.basicFonts
import me.ash.reader.ui.ext.ExternalFonts
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.ext.restart
import me.ash.reader.ui.theme.GoogleSansFontFamily
import me.ash.reader.ui.theme.SystemTypography
import me.ash.reader.ui.theme.applyFontFamily
import me.ash.reader.ui.theme.getGlobalFontFamily

val LocalBasicFonts = compositionLocalOf<BasicFontsPreference> { BasicFontsPreference.default }

sealed class BasicFontsPreference(val value: Int) : Preference() {
    object System : BasicFontsPreference(0)

    object GoogleSans : BasicFontsPreference(1)

    object External : BasicFontsPreference(5)
    
    object Serif : BasicFontsPreference(6)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(basicFonts, value)
            context.restart()
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            System -> "Playfair Display & Nikosh (Default)"
            GoogleSans -> "Playfair & Nikosh"
            External -> context.getString(R.string.external_fonts)
            Serif -> "Playfair Display & Nikosh"
        }

    fun asFontFamily(context: Context): FontFamily =
        when (this) {
            System -> getGlobalFontFamily(context)
            GoogleSans -> getGlobalFontFamily(context)
            External -> ExternalFonts.loadBasicTypography(context).bodyMedium.fontFamily ?: FontFamily.Default
            Serif -> getGlobalFontFamily(context)
        }

    fun asTypography(context: Context): Typography =
        when (this) {
            System -> SystemTypography.applyFontFamily(getGlobalFontFamily(context))
            GoogleSans -> SystemTypography.applyFontFamily(getGlobalFontFamily(context))
            External -> ExternalFonts.loadBasicTypography(context)
            Serif -> SystemTypography.applyFontFamily(getGlobalFontFamily(context))
        }

    companion object {

        val default: BasicFontsPreference get() = Serif
        val values = listOf(Serif, GoogleSans, System, External)

        fun fromPreferences(preferences: Preferences): BasicFontsPreference =
            when (preferences[DataStoreKey.keys[basicFonts]?.key as Preferences.Key<Int>]) {
                0 -> System
                1 -> GoogleSans
                5 -> External
                6 -> Serif
                else -> default
            }
    }
}
