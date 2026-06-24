package com.cocos.androidaccounting.ui.record

import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.Categories
import com.cocos.androidaccounting.data.model.Category
import java.time.LocalDate

data class RecordUiState(
    val type: BillType = BillType.EXPENSE,
    val selectedCategory: Category? = null,
    val categories: List<Category> = Categories.byType(BillType.EXPENSE),
    val amountInput: String = "",
    val remark: String = "",
    val date: LocalDate = LocalDate.now(),
    val isDatePickerVisible: Boolean = false,
    val isSaving: Boolean = false,
)
