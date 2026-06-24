package com.cocos.androidaccounting.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = PaperWhite,
    background = PaperWhite,
    onBackground = InkBlack,
    surface = PaperWhite,
    onSurface = InkBlack,
    surfaceVariant = SurfaceGray,
    onSurfaceVariant = InkBlack,
    outline = DividerGray,
    outlineVariant = DividerGray,
)

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    onPrimary = PaperWhiteDark,
    background = PaperWhiteDark,
    onBackground = InkBlackDark,
    surface = PaperWhiteDark,
    onSurface = InkBlackDark,
    surfaceVariant = SurfaceGrayDark,
    onSurfaceVariant = InkBlackDark,
    outline = DividerGrayDark,
    outlineVariant = DividerGrayDark,
)

@Composable
fun AccountingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalAccountingColors provides AccountingColors(),
        LocalAccountingTypography provides AccountingTypography(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AccountingTypographyM3,
            shapes = AccountingShapes,
            content = content,
        )
    }
}

// 便捷访问扩展
object AccountingThemeAccessor {
    val colors: AccountingColors
        @Composable get() = LocalAccountingColors.current
    val typography: AccountingTypography
        @Composable get() = LocalAccountingTypography.current
}
