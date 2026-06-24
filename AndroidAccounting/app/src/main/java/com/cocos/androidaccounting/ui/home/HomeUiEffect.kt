package com.cocos.androidaccounting.ui.home

sealed interface HomeUiEffect {
    data object NavigateToRecord : HomeUiEffect
    data class ShowToast(val messageRes: Int) : HomeUiEffect
}
