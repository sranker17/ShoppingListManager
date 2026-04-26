package com.sranker.shoppinglistmanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.sranker.shoppinglistmanager.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Outfit")

val outfitFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold)
)

fun getTypographyWithMultiplier(multiplier: Float): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (57 * multiplier).sp,
            lineHeight = (64 * multiplier).sp,
            letterSpacing = -0.25.sp
        ),
        displayMedium = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (45 * multiplier).sp,
            lineHeight = (52 * multiplier).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (36 * multiplier).sp,
            lineHeight = (44 * multiplier).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (32 * multiplier).sp,
            lineHeight = (40 * multiplier).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (28 * multiplier).sp,
            lineHeight = (36 * multiplier).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (24 * multiplier).sp,
            lineHeight = (32 * multiplier).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (22 * multiplier).sp,
            lineHeight = (28 * multiplier).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * multiplier).sp,
            lineHeight = (24 * multiplier).sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * multiplier).sp,
            lineHeight = (20 * multiplier).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * multiplier).sp,
            lineHeight = (24 * multiplier).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * multiplier).sp,
            lineHeight = (20 * multiplier).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * multiplier).sp,
            lineHeight = (16 * multiplier).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * multiplier).sp,
            lineHeight = (20 * multiplier).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * multiplier).sp,
            lineHeight = (16 * multiplier).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = outfitFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * multiplier).sp,
            lineHeight = (16 * multiplier).sp,
            letterSpacing = 0.5.sp
        )
    )
}
