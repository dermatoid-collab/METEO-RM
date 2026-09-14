package it.meteoapp.clone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MeteoColorScheme = darkColorScheme(
    primary          = AccentBlue,
    onPrimary        = TextPrimary,
    primaryContainer = TabSelected,
    background       = BackgroundDeep,
    onBackground     = TextPrimary,
    surface          = BackgroundCard,
    onSurface        = TextPrimary,
    surfaceVariant   = BackgroundCardAlt,
    onSurfaceVariant = TextSecondary,
    secondary        = PrecipBlue,
    onSecondary      = BackgroundDeep,
    error            = Color(0xFFCF6679),
    outline          = TextMuted,
)

@Composable
fun MeteoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MeteoColorScheme,
        typography  = MeteoTypography,
        content     = content
    )
}
