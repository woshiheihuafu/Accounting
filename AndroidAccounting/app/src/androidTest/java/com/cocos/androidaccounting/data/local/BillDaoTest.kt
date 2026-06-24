package com.cocos.androidaccounting.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cocos.androidaccounting.data.model.BillType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class BillDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: BillDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.billDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun entity(
        id: Long = 0L,
        type: BillType = BillType.EXPENSE,
        category: String = "餐饮",
        amount: Long = 1000L,
        date: LocalDate = LocalDate.of(2026, 6, 15),
        remark: String = "",
        createdAt: Instant = Instant.now(),
    ) = BillEntity(id, type, category, amount, date, remark, createdAt)

    private val june = LocalDate.of(2026, 6, 1).toEpochDay()
    private val july = LocalDate.of(2026, 7, 1).toEpochDay()

    @Test
    fun insert_then_observeBillsByMonth_returnsInserted() = runTest {
        val e = entity()
        dao.insert(e)
        val result = dao.observeBillsByMonth(june, july).first()
        assertEquals(1, result.size)
        assertEquals(e.type, result[0].type)
        assertEquals(e.amount, result[0].amount)
        assertEquals(e.date, result[0].date)
        assertEquals(e.category, result[0].category)
    }

    @Test
    fun observeBillsByMonth_emptyMonth_returnsEmptyList() = runTest {
        val result = dao.observeBillsByMonth(june, july).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun observeBillsByMonth_monthBoundary() = runTest {
        val may31 = LocalDate.of(2026, 5, 31)
        val jun1 = LocalDate.of(2026, 6, 1)
        val jun30 = LocalDate.of(2026, 6, 30)
        val jul1 = LocalDate.of(2026, 7, 1)
        dao.insert(entity(date = may31))
        dao.insert(entity(date = jun1))
        dao.insert(entity(date = jun30))
        dao.insert(entity(date = jul1))
        val result = dao.observeBillsByMonth(june, july).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.date >= jun1 && it.date < jul1 })
    }

    @Test
    fun observeSumByType_expense() = runTest {
        dao.insert(entity(type = BillType.EXPENSE, amount = 1000L))
        dao.insert(entity(type = BillType.EXPENSE, amount = 2000L))
        dao.insert(entity(type = BillType.INCOME, amount = 5000L))
        val sum = dao.observeSumByType(BillType.EXPENSE, june, july).first()
        assertEquals(3000L, sum)
    }

    @Test
    fun observeSumByType_income() = runTest {
        dao.insert(entity(type = BillType.INCOME, amount = 10000L))
        val sum = dao.observeSumByType(BillType.INCOME, june, july).first()
        assertEquals(10000L, sum)
    }

    @Test
    fun observeSumByType_emptyMonth_returnsNull() = runTest {
        val sum = dao.observeSumByType(BillType.EXPENSE, june, july).first()
        assertNull(sum)
    }

    @Test
    fun deleteById_removesBill() = runTest {
        val id = dao.insert(entity())
        dao.deleteById(id)
        val result = dao.observeBillsByMonth(june, july).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteById_nonExistingId_noop() = runTest {
        dao.insert(entity())
        dao.deleteById(9999L)
        val result = dao.observeBillsByMonth(june, july).first()
        assertEquals(1, result.size)
    }

    @Test
    fun observeBillsByMonth_ordering() = runTest {
        val baseTime = Instant.parse("2026-06-15T10:00:00Z")
        // 插入三笔：两笔 6/10，一笔 6/20（更晚日期）
        dao.insert(entity(date = LocalDate.of(2026, 6, 20), createdAt = baseTime))
        dao.insert(entity(date = LocalDate.of(2026, 6, 10), createdAt = baseTime.plusSeconds(10)))
        dao.insert(entity(date = LocalDate.of(2026, 6, 10), createdAt = baseTime))

        val result = dao.observeBillsByMonth(june, july).first()

        assertEquals(3, result.size)
        // 第一条：date 最大（6/20）
        assertEquals(LocalDate.of(2026, 6, 20), result[0].date)
        // 第二、三条：同日（6/10），按 createdAt 倒序：baseTime+10s 先，baseTime 后
        assertEquals(LocalDate.of(2026, 6, 10), result[1].date)
        assertEquals(baseTime.plusSeconds(10), result[1].createdAt)
        assertEquals(LocalDate.of(2026, 6, 10), result[2].date)
        assertEquals(baseTime, result[2].createdAt)
    }
}
