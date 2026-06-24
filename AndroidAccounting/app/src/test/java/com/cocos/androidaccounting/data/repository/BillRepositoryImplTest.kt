package com.cocos.androidaccounting.data.repository

import app.cash.turbine.test
import com.cocos.androidaccounting.data.local.BillDao
import com.cocos.androidaccounting.data.local.BillEntity
import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.MonthlySummary
import com.cocos.androidaccounting.util.DateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class BillRepositoryImplTest {

    private lateinit var fakeBillDao: FakeBillDao
    private lateinit var repository: BillRepositoryImpl

    @Before
    fun setup() {
        fakeBillDao = FakeBillDao()
        repository = BillRepositoryImpl(fakeBillDao)
    }

    private fun bill(
        id: Long = 0L,
        type: BillType = BillType.EXPENSE,
        amount: Long = 1000L,
        date: LocalDate = LocalDate.of(2026, 6, 15),
    ) = Bill(
        id = id, type = type, category = "测试", amount = amount,
        date = date, remark = "", createdAt = Instant.EPOCH,
    )

    private val june = YearMonth.of(2026, 6)

    @Test
    fun observeBillsByMonth_mapsEntityToDomain() = runTest {
        val entity = BillEntity(
            id = 1L, type = BillType.EXPENSE, category = "测试", amount = 1000L,
            date = LocalDate.of(2026, 6, 15), remark = "", createdAt = Instant.EPOCH,
        )
        fakeBillDao.setEntities(listOf(entity))
        repository.observeBillsByMonth(june).test {
            val bills = awaitItem()
            assertEquals(1, bills.size)
            assertEquals(entity.id, bills[0].id)
            assertEquals(entity.amount, bills[0].amount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeBillsByMonth_usesDateUtilRange() = runTest {
        repository.observeBillsByMonth(june).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        val expectedStart = DateUtil.monthStartEpochDay(june)
        val expectedEnd = DateUtil.monthEndExclusiveEpochDay(june)
        assertEquals(expectedStart, fakeBillDao.lastQueryStart)
        assertEquals(expectedEnd, fakeBillDao.lastQueryEnd)
    }

    @Test
    fun observeMonthlySummary_combinesIncomeExpense() = runTest {
        fakeBillDao.setExpenseSum(3000L)
        fakeBillDao.setIncomeSum(10000L)
        repository.observeMonthlySummary(june).test {
            val summary = awaitItem()
            assertEquals(10000L, summary.income)
            assertEquals(3000L, summary.expense)
            assertEquals(7000L, summary.balance)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeMonthlySummary_nullSumDefaultsToZero() = runTest {
        fakeBillDao.setExpenseSum(null)
        fakeBillDao.setIncomeSum(null)
        repository.observeMonthlySummary(june).test {
            val summary = awaitItem()
            assertEquals(MonthlySummary(), summary)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertBill_mapsFieldsAndReturnsDaoId() = runTest {
        val testDate = LocalDate.of(2026, 6, 15)
        val testInstant = Instant.EPOCH
        val input = Bill(
            id = 0L,
            type = BillType.INCOME,
            category = "工资",
            amount = 50000L,
            date = testDate,
            remark = "六月薪水",
            createdAt = testInstant,
        )
        val returnedId = repository.insertBill(input)
        assertEquals(1L, returnedId)
        val insertedEntity = fakeBillDao.lastInsertedEntity
        assertNotNull(insertedEntity)
        assertEquals(0L, insertedEntity!!.id)
        assertEquals(BillType.INCOME, insertedEntity.type)
        assertEquals("工资", insertedEntity.category)
        assertEquals(50000L, insertedEntity.amount)
        assertEquals(testDate, insertedEntity.date)
        assertEquals("六月薪水", insertedEntity.remark)
        assertEquals(testInstant, insertedEntity.createdAt)
    }

    @Test
    fun deleteBill_delegatesToDao() = runTest {
        repository.deleteBill(42L)
        assertEquals(42L, fakeBillDao.lastDeletedId)
    }
}

// ─── Fake DAO ────────────────────────────────────────────────────────────────

class FakeBillDao : BillDao {

    private val entitiesFlow = MutableStateFlow<List<BillEntity>>(emptyList())
    private val expenseSumFlow = MutableStateFlow<Long?>(null)
    private val incomeSumFlow = MutableStateFlow<Long?>(null)

    var lastQueryStart: Long = 0L
    var lastQueryEnd: Long = 0L
    var lastDeletedId: Long = -1L
    var lastInsertedEntity: BillEntity? = null
    private var nextId: Long = 1L

    fun setEntities(entities: List<BillEntity>) { entitiesFlow.value = entities }
    fun setExpenseSum(sum: Long?) { expenseSumFlow.value = sum }
    fun setIncomeSum(sum: Long?) { incomeSumFlow.value = sum }

    override fun observeBillsByMonth(
        startEpochDay: Long,
        endEpochDayExclusive: Long,
    ): Flow<List<BillEntity>> {
        lastQueryStart = startEpochDay
        lastQueryEnd = endEpochDayExclusive
        return entitiesFlow
    }

    override fun observeSumByType(
        type: BillType,
        startEpochDay: Long,
        endEpochDayExclusive: Long,
    ): Flow<Long?> = if (type == BillType.EXPENSE) expenseSumFlow else incomeSumFlow

    override suspend fun insert(entity: BillEntity): Long {
        lastInsertedEntity = entity
        return nextId++
    }

    override suspend fun deleteById(id: Long) { lastDeletedId = id }
}
