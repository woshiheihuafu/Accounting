package com.cocos.androidaccounting.data.repository

import com.cocos.androidaccounting.data.local.BillDao
import com.cocos.androidaccounting.data.mapper.toDomainList
import com.cocos.androidaccounting.data.mapper.toEntity
import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.MonthlySummary
import com.cocos.androidaccounting.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class BillRepositoryImpl @Inject constructor(
    private val billDao: BillDao,
) : BillRepository {

    override fun observeBillsByMonth(yearMonth: YearMonth): Flow<List<Bill>> {
        val start = DateUtil.monthStartEpochDay(yearMonth)
        val end = DateUtil.monthEndExclusiveEpochDay(yearMonth)
        return billDao.observeBillsByMonth(start, end).map { it.toDomainList() }
    }

    override fun observeMonthlySummary(yearMonth: YearMonth): Flow<MonthlySummary> {
        val start = DateUtil.monthStartEpochDay(yearMonth)
        val end = DateUtil.monthEndExclusiveEpochDay(yearMonth)
        val expenseFlow = billDao.observeSumByType(BillType.EXPENSE, start, end)
        val incomeFlow = billDao.observeSumByType(BillType.INCOME, start, end)
        return combine(expenseFlow, incomeFlow) { expense, income ->
            MonthlySummary(
                income = income ?: 0L,
                expense = expense ?: 0L,
            )
        }
    }

    override suspend fun insertBill(bill: Bill): Long =
        billDao.insert(bill.toEntity())

    override suspend fun deleteBill(id: Long) {
        billDao.deleteById(id)
    }
}
