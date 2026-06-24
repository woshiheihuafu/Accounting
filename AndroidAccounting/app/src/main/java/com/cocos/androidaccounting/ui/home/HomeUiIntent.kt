package com.cocos.androidaccounting.ui.home

sealed interface HomeUiIntent {
    data object OpenYearMonthPicker : HomeUiIntent
    data object DismissYearMonthPicker : HomeUiIntent
    data class ConfirmYearMonthSelection(val year: Int, val month: Int) : HomeUiIntent
    data object NavigateToRecord : HomeUiIntent
    data object OnBottomBarComingSoon : HomeUiIntent
}
