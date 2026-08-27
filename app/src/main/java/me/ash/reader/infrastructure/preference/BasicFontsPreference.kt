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
            System -> context.getString(R.string.system_default)
            GoogleSans -> context.getString(R.string.google_sans)
            External -> context.getString(R.string.external_fonts)
            Serif -> "Times New Roman / Serif"
        }

    fun asFontFamily(context: Context): FontFamily =
        when (this) {
            System -> FontFamily.Default
            GoogleSans -> GoogleSansFontFamily
            External -> ExternalFonts.loadBasicTypography(context).bodyMedium.fontFamily ?: FontFamily.Default
            Serif -> FontFamily.Serif
        }

    fun asTypography(context: Context): Typography =
        when (this) {
            System -> SystemTypography
            GoogleSans -> SystemTypography.applyFontFamily(GoogleSansFontFamily)
            External -> ExternalFonts.loadBasicTypography(context)
            Serif -> SystemTypography.applyFontFamily(FontFamily.Serif)
        }

    companion object {

        val default = Serif
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
