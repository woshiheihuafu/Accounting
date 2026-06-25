package com.cocos.androidaccounting.ui.home

import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.MonthlySummary
import java.time.LocalDate
import java.time.YearMonth

data class BillGroup(
    val date: LocalDate,
    val bills: List<Bill>,
    val dayNetAmount: Long,
)

data class HomeUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val summary: MonthlySummary = MonthlySummary(),
    val groups: List<BillGroup> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isYearMonthPickerVisible: Boolean = false,
)
