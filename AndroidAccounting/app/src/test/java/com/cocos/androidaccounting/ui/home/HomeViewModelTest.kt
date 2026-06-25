package com.cocos.androidaccounting.ui.home

import app.cash.turbine.test
import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.MonthlySummary
import com.cocos.androidaccounting.data.repository.BillRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepo: FakeBillRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBillRepository()
        viewModel = HomeViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeBill(
        id: Long,
        type: BillType = BillType.EXPENSE,
        amount: Long = 1000L,
        date: LocalDate = LocalDate.of(2026, 6, 1),
        createdAt: Instant = Instant.now(),
    ) = Bill(
        id = id,
        type = type,
        category = "餐饮",
        amount = amount,
        date = date,
        createdAt = createdAt,
    )

    @Test
    fun initialState_isLoading() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dataLoaded_mapsToGroupsAndSummary() = runTest {
        val currentMonth = YearMonth.now()
        val bills = listOf(
            makeBill(1L, BillType.EXPENSE, 2000L, LocalDate.of(2026, 6, 1)),
            makeBill(2L, BillType.INCOME, 5000L, LocalDate.of(2026, 6, 2)),
        )
        val summary = MonthlySummary(income = 5000L, expense = 2000L)
        fakeRepo.setBillsForMonth(currentMonth, bills)
        fakeRepo.setSummaryForMonth(currentMonth, summary)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(2, loaded.groups.size)
            assertEquals(summary, loaded.summary)
            assertNull(loaded.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun grouping_dayNetAmount_incomeMinusExpense() = runTest {
        val currentMonth = YearMonth.now()
        val date = LocalDate.of(2026, 6, 10)

        // 收入 5000 + 支出 2000 → 净额 3000（净收入）
        val billsWithBoth = listOf(
            makeBill(1L, BillType.INCOME, 5000L, date),
            makeBill(2L, BillType.EXPENSE, 2000L, date),
        )
        fakeRepo.setBillsForMonth(currentMonth, billsWithBoth)
        fakeRepo.setSummaryForMonth(currentMonth, MonthlySummary(income = 5000L, expense = 2000L))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            val group = state.groups.first { it.date == date }
            assertEquals(3000L, group.dayNetAmount)
            cancelAndIgnoreRemainingEvents()
        }

        // 仅支出 2000 → 净额 -2000（净支出）
        val dateOnly = LocalDate.of(2026, 6, 11)
        val billsExpenseOnly = listOf(
            makeBill(3L, BillType.EXPENSE, 2000L, dateOnly),
        )
        fakeRepo.setBillsForMonth(currentMonth, billsExpenseOnly)
        fakeRepo.setSummaryForMonth(currentMonth, MonthlySummary(income = 0L, expense = 2000L))

        val viewModel2 = HomeViewModel(fakeRepo)
        viewModel2.uiState.test {
            skipItems(1)
            val state = awaitItem()
            val group = state.groups.first { it.date == dateOnly }
            assertEquals(-2000L, group.dayNetAmount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun emptyMonth_groupsEmpty() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertTrue(state.groups.isEmpty())
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun openPicker_setsVisibleTrue() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            skipItems(1)

            viewModel.onIntent(HomeUiIntent.OpenYearMonthPicker)
            val state = awaitItem()
            assertTrue(state.isYearMonthPickerVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dismissPicker_setsVisibleFalse() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            skipItems(1)

            viewModel.onIntent(HomeUiIntent.OpenYearMonthPicker)
            skipItems(1)

            viewModel.onIntent(HomeUiIntent.DismissYearMonthPicker)
            val state = awaitItem()
            assertFalse(state.isYearMonthPickerVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun confirmSelection_updatesYearMonth() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            skipItems(1)

            viewModel.onIntent(HomeUiIntent.ConfirmYearMonthSelection(2025, 3))
            val state = awaitItem()
            assertEquals(YearMonth.of(2025, 3), state.yearMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun navigateToRecord_emitsEffect() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(HomeUiIntent.NavigateToRecord)
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.NavigateToRecord)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun comingSoon_emitsShowToast() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(HomeUiIntent.OnBottomBarComingSoon)
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.ShowToast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun repositoryError_setsErrorState() = runTest {
        val errorRepository = object : BillRepository {
            override fun observeBillsByMonth(yearMonth: YearMonth): Flow<List<Bill>> =
                kotlinx.coroutines.flow.flow { throw RuntimeException("DB error") }
            override fun observeMonthlySummary(yearMonth: YearMonth): Flow<MonthlySummary> =
                kotlinx.coroutines.flow.flowOf(MonthlySummary())
            override suspend fun insertBill(bill: Bill): Long = 0L
            override suspend fun deleteBill(id: Long) = Unit
        }

        val errorViewModel = HomeViewModel(errorRepository)

        errorViewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun grouping_preservesRepositoryOrder() = runTest {
        val currentMonth = YearMonth.now()
        val june16 = LocalDate.of(2026, 6, 16)
        val june15 = LocalDate.of(2026, 6, 15)
        val bills = listOf(
            makeBill(id = 1L, date = june16, createdAt = Instant.ofEpochSecond(200)),
            makeBill(id = 2L, date = june16, createdAt = Instant.ofEpochSecond(100)),
            makeBill(id = 3L, date = june15, createdAt = Instant.ofEpochSecond(50)),
        )
        fakeRepo.setBillsForMonth(currentMonth, bills)

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assertEquals(2, state.groups.size)
            assertEquals(june16, state.groups[0].date)
            assertEquals(june15, state.groups[1].date)
            assertEquals(listOf(1L, 2L), state.groups[0].bills.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun confirmSelection_refreshesSummaryAndBills() = runTest {
        val june = YearMonth.of(2026, 6)
        val july = YearMonth.of(2026, 7)
        fakeRepo.setBillsForMonth(june, listOf(makeBill(id = 1L, type = BillType.EXPENSE, amount = 1000L)))
        fakeRepo.setSummaryForMonth(june, MonthlySummary(income = 0L, expense = 1000L))
        fakeRepo.setBillsForMonth(july, listOf(makeBill(id = 2L, type = BillType.INCOME, amount = 5000L)))
        fakeRepo.setSummaryForMonth(july, MonthlySummary(income = 5000L, expense = 0L))

        viewModel.uiState.test {
            skipItems(1) // loading (june)
            skipItems(1) // loaded (june)

            viewModel.onIntent(HomeUiIntent.ConfirmYearMonthSelection(2026, 7))

            skipItems(1) // interim state (july month + old june data from combine)
            skipItems(1) // loading (july)
            val state = awaitItem() // loaded (july)
            assertEquals(1, state.groups.size)
            assertEquals(5000L, state.summary.income)
            assertEquals(0L, state.summary.expense)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeBillRepository : BillRepository {
    private val billsByMonth = mutableMapOf<YearMonth, MutableStateFlow<List<Bill>>>()
    private val summaryByMonth = mutableMapOf<YearMonth, MutableStateFlow<MonthlySummary>>()

    fun setBillsForMonth(yearMonth: YearMonth, bills: List<Bill>) {
        billsByMonth.getOrPut(yearMonth) { MutableStateFlow(emptyList()) }.value = bills
    }

    fun setSummaryForMonth(yearMonth: YearMonth, summary: MonthlySummary) {
        summaryByMonth.getOrPut(yearMonth) { MutableStateFlow(MonthlySummary()) }.value = summary
    }

    override fun observeBillsByMonth(yearMonth: YearMonth): Flow<List<Bill>> =
        billsByMonth.getOrPut(yearMonth) { MutableStateFlow(emptyList()) }

    override fun observeMonthlySummary(yearMonth: YearMonth): Flow<MonthlySummary> =
        summaryByMonth.getOrPut(yearMonth) { MutableStateFlow(MonthlySummary()) }

    override suspend fun insertBill(bill: Bill): Long = 0L
    override suspend fun deleteBill(id: Long) = Unit
}
