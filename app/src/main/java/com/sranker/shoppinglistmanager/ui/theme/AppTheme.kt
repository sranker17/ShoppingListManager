package com.sranker.shoppinglistmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.sranker.shoppinglistmanager.data.repository.AppTheme as ThemeEnum
import com.sranker.shoppinglistmanager.data.repository.TextSizePreference

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanTeal,
    background = MinimalistBackground,
    surface = MinimalistBackground,
    onPrimary = MinimalistBackground,
    onBackground = OceanText,
    onSurface = OceanText,
    surfaceVariant = MinimalistBackground,
    onSurfaceVariant = OceanText,
    surfaceContainer = MinimalistBackground
)

private val ForestLightColorScheme = darkColorScheme(
    primary = ForestGreen,
    background = MinimalistBackground,
    surface = MinimalistBackground,
    onPrimary = MinimalistBackground,
    onBackground = ForestText,
    onSurface = ForestText,
    surfaceVariant = MinimalistBackground,
    onSurfaceVariant = ForestText,
    surfaceContainer = MinimalistBackground
)

private val SunsetDarkColorScheme = darkColorScheme(
    primary = SunsetPink,
    background = MinimalistBackground,
    surface = MinimalistBackground,
    onPrimary = MinimalistBackground,
    onBackground = SunsetText,
    onSurface = SunsetText,
    surfaceVariant = MinimalistBackground,
    onSurfaceVariant = SunsetText,
    surfaceContainer = MinimalistBackground
)

private val SnowLightColorScheme = darkColorScheme(
    primary = SnowSlate,
    background = MinimalistBackground,
    surface = MinimalistBackground,
    onPrimary = MinimalistBackground,
    onBackground = SnowText,
    onSurface = SnowText,
    surfaceVariant = MinimalistBackground,
    onSurfaceVariant = SnowText,
    surfaceContainer = MinimalistBackground
)

@Composable
fun AppTheme(
    theme: ThemeEnum = ThemeEnum.OCEAN_DARK,
    textSize: TextSizePreference = TextSizePreference.MEDIUM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        ThemeEnum.OCEAN_DARK -> OceanDarkColorScheme
        ThemeEnum.FOREST_LIGHT -> ForestLightColorScheme
        ThemeEnum.SUNSET_DARK -> SunsetDarkColorScheme
        ThemeEnum.SNOW_LIGHT -> SnowLightColorScheme
    }

    val multiplier = when (textSize) {
        TextSizePreference.SMALL -> 0.85f
        TextSizePreference.MEDIUM -> 1.0f
        TextSizePreference.LARGE -> 1.15f
    }
    
    val typography = getTypographyWithMultiplier(multiplier)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Ocean Dark")
@Composable
fun OceanDarkPreview() {
    AppTheme(theme = ThemeEnum.OCEAN_DARK) {
        androidx.compose.material3.Surface {
            androidx.compose.material3.Text("Ocean Dark Theme")
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Forest Light")
@Composable
fun ForestLightPreview() {
    AppTheme(theme = ThemeEnum.FOREST_LIGHT) {
        androidx.compose.material3.Surface {
            androidx.compose.material3.Text("Forest Light Theme")
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Sunset Dark")
@Composable
fun SunsetDarkPreview() {
    AppTheme(theme = ThemeEnum.SUNSET_DARK) {
        androidx.compose.material3.Surface {
            androidx.compose.material3.Text("Sunset Dark Theme")
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Snow Light")
@Composable
fun SnowLightPreview() {
    AppTheme(theme = ThemeEnum.SNOW_LIGHT) {
        androidx.compose.material3.Surface {
            androidx.compose.material3.Text("Snow Light Theme")
        }
    }
}
