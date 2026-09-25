package me.ash.reader.ui.theme.palette

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import me.ash.reader.infrastructure.preference.LocalAmoledDarkTheme
import me.ash.reader.infrastructure.preference.LocalDarkTheme

@Composable
fun dynamicLightColorScheme(): ColorScheme {
    val palettes = LocalTonalPalettes.current

    // Soothing warm paper palette to eliminate harsh contrast and eye strain
    val soothingLightOnSurface = Color(0xFF2C2A26)
    val soothingLightOnSurfaceVariant = Color(0xFF5A5750)
    val soothingLightBackground = Color(0xFFF8F6F1)
    val soothingLightSurface = Color(0xFFFAF8F3)

    return lightColorScheme(
        primary = palettes primary 40,
        onPrimary = palettes primary 100,
        primaryContainer = palettes primary 90,
        onPrimaryContainer = palettes primary 30,
        inversePrimary = palettes primary 80,
        secondary = palettes secondary 40,
        onSecondary = palettes secondary 100,
        secondaryContainer = palettes secondary 90,
        onSecondaryContainer = palettes secondary 30,
        tertiary = palettes tertiary 40,
        onTertiary = palettes tertiary 100,
        tertiaryContainer = palettes tertiary 90,
        onTertiaryContainer = palettes tertiary 30,
        background = soothingLightBackground,
        onBackground = soothingLightOnSurface,
        surface = soothingLightSurface,
        onSurface = soothingLightOnSurface,
        surfaceVariant = palettes neutralVariant 90,
        onSurfaceVariant = soothingLightOnSurfaceVariant,
        surfaceTint = palettes primary 40,
        inverseSurface = palettes neutral 20,
        inverseOnSurface = palettes neutral 95,
        outline = palettes neutralVariant 50,
        outlineVariant = palettes neutralVariant 80,
        surfaceBright = Color(0xFFFCFAF6),
        surfaceDim = Color(0xFFEBE8E1),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F3ED),
        surfaceContainer = Color(0xFFF0EDE6),
        surfaceContainerHigh = Color(0xFFEAE7DF),
        surfaceContainerHighest = Color(0xFFE4E1D8),
        primaryFixed = palettes primary 90,
        onPrimaryFixed = palettes primary 10,
        primaryFixedDim = palettes primary 90,
        onPrimaryFixedVariant = palettes primary 30,
        secondaryFixed = palettes secondary 90,
        onSecondaryFixed = palettes secondary 10,
        secondaryFixedDim = palettes secondary 90,
        onSecondaryFixedVariant = palettes secondary 30,
        tertiaryFixed = palettes tertiary 90,
        onTertiaryFixed = palettes tertiary 10,
        tertiaryFixedDim = palettes tertiary 90,
        onTertiaryFixedVariant = palettes tertiary 30
    )
}

@Composable
fun dynamicDarkColorScheme(): ColorScheme {
    val palettes = LocalTonalPalettes.current
    val useAmoledDarkTheme = LocalAmoledDarkTheme.current.value

    // Kindle-like soothing darkness:
    // Pure deep pitch-black canvas with warm, gentle bone/cream white typography
    // (zero blue glare, high ocular comfort for night and extended reading)
    val soothingOnSurface = Color(0xFFC8C2B6)
    val soothingOnSurfaceVariant = Color(0xFF7E7A73)
    val soothingBackground = Color.Black
    val soothingSurface = Color.Black
    val soothingSurfaceContainerLowest = Color.Black
    val soothingSurfaceContainerLow = if (useAmoledDarkTheme) Color.Black else Color(0xFF0C0C0D)
    val soothingSurfaceContainer = if (useAmoledDarkTheme) Color(0xFF0F0F11) else Color(0xFF131315)
    val soothingSurfaceContainerHigh = if (useAmoledDarkTheme) Color(0xFF161618) else Color(0xFF19191C)
    val soothingSurfaceContainerHighest = if (useAmoledDarkTheme) Color(0xFF1B1B1E) else Color(0xFF202024)

    return darkColorScheme(
        primary = palettes primary 75,
        onPrimary = palettes primary 20,
        primaryContainer = palettes primary 30,
        onPrimaryContainer = palettes primary 90,
        inversePrimary = palettes primary 40,
        secondary = palettes secondary 75,
        onSecondary = palettes secondary 20,
        secondaryContainer = palettes secondary 30,
        onSecondaryContainer = palettes secondary 90,
        tertiary = palettes tertiary 75,
        onTertiary = palettes tertiary 20,
        tertiaryContainer = palettes tertiary 30,
        onTertiaryContainer = palettes tertiary 90,
        background = soothingBackground,
        onBackground = soothingOnSurface,
        surface = soothingSurface,
        onSurface = soothingOnSurface,
        surfaceVariant = Color(0xFF171719),
        onSurfaceVariant = soothingOnSurfaceVariant,
        surfaceTint = palettes primary 75,
        inverseSurface = Color(0xFFD2CEC7),
        inverseOnSurface = Color(0xFF141414),
        outline = Color(0xFF2C2B29),
        outlineVariant = Color(0xFF1E1D1B),
        surfaceBright = Color(0xFF2B2B30),
        surfaceDim = soothingSurface,
        surfaceContainerLowest = soothingSurfaceContainerLowest,
        surfaceContainerLow = soothingSurfaceContainerLow,
        surfaceContainer = soothingSurfaceContainer,
        surfaceContainerHigh = soothingSurfaceContainerHigh,
        surfaceContainerHighest = soothingSurfaceContainerHighest,
        primaryFixed = palettes primary 90,
        onPrimaryFixed = palettes primary 10,
        primaryFixedDim = palettes primary 90,
        onPrimaryFixedVariant = palettes primary 30,
        secondaryFixed = palettes secondary 90,
        onSecondaryFixed = palettes secondary 10,
        secondaryFixedDim = palettes secondary 90,
        onSecondaryFixedVariant = palettes secondary 30,
        tertiaryFixed = palettes tertiary 90,
        onTertiaryFixed = palettes tertiary 10,
        tertiaryFixedDim = palettes tertiary 90,
        onTertiaryFixedVariant = palettes tertiary 30
    )
}

@Composable
infix fun Color.onLight(lightColor: Color): Color =
    if (!LocalDarkTheme.current.isDarkTheme()) lightColor else this

@Composable
infix fun Color.onDark(darkColor: Color): Color =
    if (LocalDarkTheme.current.isDarkTheme()) darkColor else this

@Stable
@Composable
@ReadOnlyComposable
infix fun Color.alwaysLight(isAlways: Boolean): Color {
    val colorScheme = MaterialTheme.colorScheme
    return if (isAlways && LocalDarkTheme.current.isDarkTheme()) {
        when (this) {
            colorScheme.primary -> colorScheme.onPrimary
            colorScheme.secondary -> colorScheme.onSecondary
            colorScheme.tertiary -> colorScheme.onTertiary
            colorScheme.background -> colorScheme.onBackground
            colorScheme.error -> colorScheme.onError
            colorScheme.surface -> colorScheme.onSurface
            colorScheme.surfaceVariant -> colorScheme.onSurfaceVariant
            colorScheme.primaryContainer -> colorScheme.onPrimaryContainer
            colorScheme.secondaryContainer -> colorScheme.onSecondaryContainer
            colorScheme.tertiaryContainer -> colorScheme.onTertiaryContainer
            colorScheme.errorContainer -> colorScheme.onErrorContainer
            colorScheme.inverseSurface -> colorScheme.inverseOnSurface

            colorScheme.onPrimary -> colorScheme.primary
            colorScheme.onSecondary -> colorScheme.secondary
            colorScheme.onTertiary -> colorScheme.tertiary
            colorScheme.onBackground -> colorScheme.background
            colorScheme.onError -> colorScheme.error
            colorScheme.onSurface -> colorScheme.surface
            colorScheme.onSurfaceVariant -> colorScheme.surfaceVariant
            colorScheme.onPrimaryContainer -> colorScheme.primaryContainer
            colorScheme.onSecondaryContainer -> colorScheme.secondaryContainer
            colorScheme.onTertiaryContainer -> colorScheme.tertiaryContainer
            colorScheme.onErrorContainer -> colorScheme.errorContainer
            colorScheme.inverseOnSurface -> colorScheme.inverseSurface

            else -> Color.Unspecified
        }
    } else {
        this
    }
}

@Stable
@Composable
@ReadOnlyComposable
infix fun Color.alwaysDark(isAlways: Boolean): Color {
    val colorScheme = MaterialTheme.colorScheme
    return if (isAlways && !LocalDarkTheme.current.isDarkTheme()) {
        when (this) {
            colorScheme.primary -> colorScheme.onPrimary
            colorScheme.secondary -> colorScheme.onSecondary
            colorScheme.tertiary -> colorScheme.onTertiary
            colorScheme.background -> colorScheme.onBackground
            colorScheme.error -> colorScheme.onError
            colorScheme.surface -> colorScheme.onSurface
            colorScheme.surfaceVariant -> colorScheme.onSurfaceVariant
            colorScheme.primaryContainer -> colorScheme.onPrimaryContainer
            colorScheme.secondaryContainer -> colorScheme.onSecondaryContainer
            colorScheme.tertiaryContainer -> colorScheme.onTertiaryContainer
            colorScheme.errorContainer -> colorScheme.onErrorContainer
            colorScheme.inverseSurface -> colorScheme.inverseOnSurface

            colorScheme.onPrimary -> colorScheme.primary
            colorScheme.onSecondary -> colorScheme.secondary
            colorScheme.onTertiary -> colorScheme.tertiary
            colorScheme.onBackground -> colorScheme.background
            colorScheme.onError -> colorScheme.error
            colorScheme.onSurface -> colorScheme.surface
            colorScheme.onSurfaceVariant -> colorScheme.surfaceVariant
            colorScheme.onPrimaryContainer -> colorScheme.primaryContainer
            colorScheme.onSecondaryContainer -> colorScheme.secondaryContainer
            colorScheme.onTertiaryContainer -> colorScheme.tertiaryContainer
            colorScheme.onErrorContainer -> colorScheme.errorContainer
            colorScheme.inverseOnSurface -> colorScheme.inverseSurface

            else -> Color.Unspecified
        }
    } else {
        this
    }
}

fun String.checkColorHex(): String? {
    var s = this.trim()
    if (s.length > 6) {
        s = s.substring(s.length - 6)
    }
    return "[0-9a-fA-F]{6}".toRegex().find(s)?.value
}

@Stable
fun String.safeHexToColor(): Color =
    try {
        Color(java.lang.Long.parseLong(this, 16))
    } catch (e: Exception) {
        Color.Transparent
    }
