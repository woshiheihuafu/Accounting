package com.cocos.androidaccounting.ui.record

sealed interface RecordUiEffect {
    data object NavigateBack : RecordUiEffect
    data class ShowToast(val messageRes: Int) : RecordUiEffect
}
