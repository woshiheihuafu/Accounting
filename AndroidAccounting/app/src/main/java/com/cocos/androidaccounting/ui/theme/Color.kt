package com.cocos.androidaccounting.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 墨与纸 Design Token
val PaperWhite = Color(0xFFFAFAF7)
val InkBlack = Color(0xFF1A1A1A)
val SageGreen = Color(0xFF7D9B76)
val SurfaceGray = Color(0xFFF0F0EC)
val DividerGray = Color(0xFFE0E0DB)
val ExpenseRed = Color(0xFFC75C5C)
val IncomeGreen = Color(0xFF7D9B76)

// Dark 变体
val PaperWhiteDark = Color(0xFF1A1A1A)
val InkBlackDark = Color(0xFFFAFAF7)
val SurfaceGrayDark = Color(0xFF262626) // 建议值，待设计确认
val DividerGrayDark = Color(0xFF333330) // 建议值，待设计确认

// 业务语义色扩展（不挤占 Material 语义槽位）
@Immutable
data class AccountingColors(
    val expenseRed: Color = ExpenseRed,
    val incomeGreen: Color = IncomeGreen,
)

val LocalAccountingColors = staticCompositionLocalOf { AccountingColors() }
