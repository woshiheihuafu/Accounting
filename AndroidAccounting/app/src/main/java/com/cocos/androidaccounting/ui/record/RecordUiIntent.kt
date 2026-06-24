package com.cocos.androidaccounting.ui.record

import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.Category
import java.time.LocalDate

sealed interface AmountKey {
    data class Digit(val n: Int) : AmountKey
    data object Dot : AmountKey
    data object Delete : AmountKey
    data object PlusMinus : AmountKey
    data object Done : AmountKey
}

sealed interface RecordUiIntent {
    data class SelectType(val type: BillType) : RecordUiIntent
    data class SelectCategory(val category: Category) : RecordUiIntent
    data class InputAmount(val key: AmountKey) : RecordUiIntent
    data class ChangeRemark(val text: String) : RecordUiIntent
    data object OpenDatePicker : RecordUiIntent
    data object DismissDatePicker : RecordUiIntent
    data class ConfirmDate(val date: LocalDate) : RecordUiIntent
    data object Save : RecordUiIntent
}
