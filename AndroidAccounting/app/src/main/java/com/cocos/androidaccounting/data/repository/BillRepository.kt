package com.cocos.androidaccounting.data.repository

import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.MonthlySummary
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BillRepository {
    fun observeBillsByMonth(yearMonth: YearMonth): Flow<List<Bill>>
    fun observeMonthlySummary(yearMonth: YearMonth): Flow<MonthlySummary>
    suspend fun insertBill(bill: Bill): Long
    suspend fun deleteBill(id: Long)
}
