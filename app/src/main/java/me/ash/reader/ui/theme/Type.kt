@file:OptIn(ExperimentalTextApi::class)

package me.ash.reader.ui.theme

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import java.text.Bidi
import me.ash.reader.R

// TODO: Rename file to Typography.kt and add @Stable

private val LabelSmallEmphasizedFont: FontFamily get() = GlobalFontFamily
private val LabelSmallEmphasizedLineHeight = 16.0.sp
private val LabelSmallEmphasizedSize = 11.sp
private val LabelSmallEmphasizedTracking = 0.5.sp
private val LabelSmallEmphasizedWeight = FontWeight.Bold

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val SystemTypography
    get() =
        Typography(
            bodySmallEmphasized =
                TextStyle.Default.copy(
                    fontFamily = LabelSmallEmphasizedFont,
                    fontSize = LabelSmallEmphasizedSize,
                    fontWeight = LabelSmallEmphasizedWeight,
                    letterSpacing = LabelSmallEmphasizedTracking,
                    lineHeight = LabelSmallEmphasizedLineHeight,
                )
        )

internal fun TextStyle.applyTextDirection(textDirection: TextDirection = TextDirection.Content) =
    this.copy(textDirection = textDirection)

private val PlayfairRegular =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W400,
    )

private val PlayfairRegularItalic =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W400,
        style = FontStyle.Italic,
    )

private val PlayfairMedium =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W500,
    )

private val PlayfairMediumItalic =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W500,
        style = FontStyle.Italic,
    )

private val PlayfairSemiBold =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W600,
    )

private val PlayfairSemiBoldItalic =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W600,
        style = FontStyle.Italic,
    )

private val PlayfairBold =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W700,
    )

private val PlayfairBoldItalic =
    Font(
        R.font.playfair_display,
        weight = FontWeight.W700,
        style = FontStyle.Italic,
    )

private val NikoshRegular =
    Font(
        R.font.nikosh,
        weight = FontWeight.W400,
    )

private val NikoshRegularItalic =
    Font(
        R.font.nikosh,
        weight = FontWeight.W400,
        style = FontStyle.Italic,
    )

private val NikoshBold =
    Font(
        R.font.nikosh,
        weight = FontWeight.W700,
    )

private val NikoshBoldItalic =
    Font(
        R.font.nikosh,
        weight = FontWeight.W700,
        style = FontStyle.Italic,
    )

val GlobalFallbackFontFamily =
    FontFamily(
        PlayfairRegular,
        PlayfairRegularItalic,
        PlayfairMedium,
        PlayfairMediumItalic,
        PlayfairSemiBold,
        PlayfairSemiBoldItalic,
        PlayfairBold,
        PlayfairBoldItalic,
        NikoshRegular,
        NikoshRegularItalic,
        NikoshBold,
        NikoshBoldItalic,
    )

@Volatile
private var cachedGlobalFontFamily: FontFamily? = null

fun getGlobalFontFamily(context: Context): FontFamily {
    cachedGlobalFontFamily?.let { return it }
    val family =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val playfairFont =
                    android.graphics.fonts.Font.Builder(context.resources, R.font.playfair_display).build()
                val playfairFamily =
                    android.graphics.fonts.FontFamily.Builder(playfairFont).build()

                val nikoshFont =
                    android.graphics.fonts.Font.Builder(context.resources, R.font.nikosh).build()
                val nikoshFamily =
                    android.graphics.fonts.FontFamily.Builder(nikoshFont).build()

                val customTypeface =
                    Typeface.CustomFallbackBuilder(playfairFamily)
                        .addCustomFallback(nikoshFamily)
                        .build()
                FontFamily(customTypeface)
            } else {
                GlobalFallbackFontFamily
            }
        } catch (e: Throwable) {
            GlobalFallbackFontFamily
        }
    cachedGlobalFontFamily = family
    return family
}

val GlobalFontFamily: FontFamily
    get() = cachedGlobalFontFamily ?: GlobalFallbackFontFamily

val GoogleSansFontFamily: FontFamily
    get() = GlobalFontFamily

/**
 * Resolve the text to Rtl if the text requires BiDirectional
 *
 * @see [android.view.View.TEXT_DIRECTION_ANY_RTL]
 * @see [Bidi.requiresBidi]
 */
fun TextStyle.applyTextDirection(requiresBidi: Boolean) =
    this.applyTextDirection(
        textDirection = if (requiresBidi) TextDirection.Rtl else TextDirection.Ltr
    )

fun TextStyle.applyFontFamily(fontFamily: FontFamily) = this.merge(fontFamily = fontFamily)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun Typography.applyTextDirection() =
    this.copy(
        displayLarge = displayLarge.applyTextDirection(),
        displayMedium = displayMedium.applyTextDirection(),
        displaySmall = displaySmall.applyTextDirection(),
        headlineLarge = headlineLarge.applyTextDirection(),
        headlineMedium = headlineMedium.applyTextDirection(),
        headlineSmall = headlineSmall.applyTextDirection(),
        titleLarge = titleLarge.applyTextDirection(),
        titleMedium = titleMedium.applyTextDirection(),
        titleSmall = titleSmall.applyTextDirection(),
        bodyLarge = bodyLarge.applyTextDirection(),
        bodyMedium = bodyMedium.applyTextDirection(),
        bodySmall = bodySmall.applyTextDirection(),
        labelLarge = labelLarge.applyTextDirection(),
        labelMedium = labelMedium.applyTextDirection(),
        labelSmall = labelSmall.applyTextDirection(),
        bodyLargeEmphasized = bodyLargeEmphasized.applyTextDirection(),
        bodyMediumEmphasized = bodyMediumEmphasized.applyTextDirection(),
        bodySmallEmphasized = bodySmallEmphasized.applyTextDirection(),
        displayLargeEmphasized = displayLargeEmphasized.applyTextDirection(),
        displayMediumEmphasized = displayMediumEmphasized.applyTextDirection(),
        displaySmallEmphasized = displaySmallEmphasized.applyTextDirection(),
        headlineLargeEmphasized = headlineLargeEmphasized.applyTextDirection(),
        headlineMediumEmphasized = headlineMediumEmphasized.applyTextDirection(),
        headlineSmallEmphasized = headlineSmallEmphasized.applyTextDirection(),
        titleLargeEmphasized = titleLargeEmphasized.applyTextDirection(),
        titleMediumEmphasized = titleMediumEmphasized.applyTextDirection(),
        titleSmallEmphasized = titleSmallEmphasized.applyTextDirection(),
        labelLargeEmphasized = labelLargeEmphasized.applyTextDirection(),
        labelMediumEmphasized = labelMediumEmphasized.applyTextDirection(),
        labelSmallEmphasized = labelSmallEmphasized.applyTextDirection(),
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun Typography.applyFontFamily(fontFamily: FontFamily) =
    this.copy(
        displayLarge = displayLarge.applyFontFamily(fontFamily),
        displayMedium = displayMedium.applyFontFamily(fontFamily),
        displaySmall = displaySmall.applyFontFamily(fontFamily),
        headlineLarge = headlineLarge.applyFontFamily(fontFamily),
        headlineMedium = headlineMedium.applyFontFamily(fontFamily),
        headlineSmall = headlineSmall.applyFontFamily(fontFamily),
        titleLarge = titleLarge.applyFontFamily(fontFamily),
        titleMedium = titleMedium.applyFontFamily(fontFamily),
        titleSmall = titleSmall.applyFontFamily(fontFamily),
        bodyLarge = bodyLarge.applyFontFamily(fontFamily),
        bodyMedium = bodyMedium.applyFontFamily(fontFamily),
        bodySmall = bodySmall.applyFontFamily(fontFamily),
        labelLarge = labelLarge.applyFontFamily(fontFamily),
        labelMedium = labelMedium.applyFontFamily(fontFamily),
        labelSmall = labelSmall.applyFontFamily(fontFamily),
        bodyLargeEmphasized = bodyLargeEmphasized.applyFontFamily(fontFamily),
        bodyMediumEmphasized = bodyMediumEmphasized.applyFontFamily(fontFamily),
        bodySmallEmphasized = bodySmallEmphasized.applyFontFamily(fontFamily),
        displayLargeEmphasized = displayLargeEmphasized.applyFontFamily(fontFamily),
        displayMediumEmphasized = displayMediumEmphasized.applyFontFamily(fontFamily),
        displaySmallEmphasized = displaySmallEmphasized.applyFontFamily(fontFamily),
        headlineLargeEmphasized = headlineLargeEmphasized.applyFontFamily(fontFamily),
        headlineMediumEmphasized = headlineMediumEmphasized.applyFontFamily(fontFamily),
        headlineSmallEmphasized = headlineSmallEmphasized.applyFontFamily(fontFamily),
        titleLargeEmphasized = titleLargeEmphasized.applyFontFamily(fontFamily),
        titleMediumEmphasized = titleMediumEmphasized.applyFontFamily(fontFamily),
        titleSmallEmphasized = titleSmallEmphasized.applyFontFamily(fontFamily),
        labelLargeEmphasized = labelLargeEmphasized.applyFontFamily(fontFamily),
        labelMediumEmphasized = labelMediumEmphasized.applyFontFamily(fontFamily),
        labelSmallEmphasized = labelSmallEmphasized.applyFontFamily(fontFamily),
    )
