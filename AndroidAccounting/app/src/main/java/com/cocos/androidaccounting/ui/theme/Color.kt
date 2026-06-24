package com.cocos.androidaccounting.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 墨与纸 Design Token（对齐 code-accounting-v2.pen 设计变量）
val PaperWhite = Color(0xFFFAFAF8)
val InkBlack = Color(0xFF1A1A1A)
val SageGreen = Color(0xFF7D9B76)
val SageGreenLight = Color(0xFFEDF3EB) // brand-light，选中类目底色
val SurfaceGray = Color(0xFFF0F0EC)
val DividerGray = Color(0xFFE8E8E4)
val TextSecondary = Color(0xFF8C8C88) // 次要文字/未选中图标
val ExpenseRed = Color(0xFFC75C5C)
val IncomeGreen = Color(0xFF7D9B76)

// Dark 变体
val PaperWhiteDark = Color(0xFF1A1A1A)
val InkBlackDark = Color(0xFFFAFAF8)
val SurfaceGrayDark = Color(0xFF262626) // 建议值，待设计确认
val DividerGrayDark = Color(0xFF333330) // 建议值，待设计确认
val TextSecondaryDark = Color(0xFF9A9A96) // 建议值，待设计确认

// 业务语义色扩展（不挤占 Material 语义槽位）
@Immutable
data class AccountingColors(
    val expenseRed: Color = ExpenseRed,
    val incomeGreen: Color = IncomeGreen,
)

val LocalAccountingColors = staticCompositionLocalOf { AccountingColors() }
