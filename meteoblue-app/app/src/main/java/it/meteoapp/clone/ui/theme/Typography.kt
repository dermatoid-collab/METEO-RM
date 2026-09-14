package it.meteoapp.clone.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Usa il font di sistema (Roboto su Android) — sostituibile con Inter via Google Fonts
val MeteoTypography = Typography(
    // Temperatura hero grande
    displayLarge = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize   = 80.sp,
        lineHeight = 84.sp,
        letterSpacing = (-2).sp
    ),
    // Temperatura giornaliera
    displayMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    // Titoli schermata
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 24.sp
    ),
    // Label ore / giorni
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 20.sp
    ),
    // Dati meteo secondari
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    // Label piccole (percentuali, unità)
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp,
        lineHeight = 16.sp
    )
)
