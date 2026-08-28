package me.ash.reader

import me.ash.reader.infrastructure.preference.BasicFontsPreference
import me.ash.reader.infrastructure.preference.ReadingFontsPreference
import me.ash.reader.ui.theme.GlobalFontFamily
import me.ash.reader.ui.theme.GoogleSansFontFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun globalFontFamily_isConfigured() {
        assertNotNull(GlobalFontFamily)
        assertEquals(GlobalFontFamily, GoogleSansFontFamily)
    }

    @Test
    fun basicFontsPreference_defaultIsSerif() {
        assertEquals(BasicFontsPreference.Serif, BasicFontsPreference.default)
    }

    @Test
    fun readingFontsPreference_defaultIsSerif() {
        assertEquals(ReadingFontsPreference.Serif, ReadingFontsPreference.default)
    }
}
