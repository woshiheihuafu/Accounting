package com.cocos.androidaccounting.ui.record

import app.cash.turbine.test
import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.Categories
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeBillRepository
    private lateinit var viewModel: RecordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeBillRepository()
        viewModel = RecordViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState() = runTest {
        val state = viewModel.uiState.value
        assertEquals(BillType.EXPENSE, state.type)
        assertNull(state.selectedCategory)
        assertEquals("", state.amountInput)
        assertEquals("", state.remark)
        assertFalse(state.isDatePickerVisible)
        assertFalse(state.isSaving)
    }

    @Test
    fun selectType_switchesCategories() = runTest {
        val category = Categories.expense.first()
        viewModel.onIntent(RecordUiIntent.SelectCategory(category))
        assertEquals(category, viewModel.uiState.value.selectedCategory)

        viewModel.onIntent(RecordUiIntent.SelectType(BillType.INCOME))
        assertEquals(BillType.INCOME, viewModel.uiState.value.type)
        assertNull(viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun selectCategory() = runTest {
        val category = Categories.expense.first()
        viewModel.onIntent(RecordUiIntent.SelectCategory(category))
        assertEquals(category, viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun inputAmount_digits() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(1)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(2)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(3)))
        assertEquals("123", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_leadingZeroReplaced() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(0)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(5)))
        assertEquals("5", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_leadingZeroZero() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(0)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(0)))
        assertEquals("0", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_emptyLeadingZero() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(0)))
        assertEquals("0", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_decimal() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(1)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Dot))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(5)))
        assertEquals("1.5", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_emptyDot() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Dot))
        assertEquals("0.", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_maxDecimal() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(1)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Dot))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(2)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(3)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(4)))
        assertEquals("1.23", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_singleDot() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(1)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Dot))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Dot))
        assertEquals("1.", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_delete() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(1)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(2)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(3)))
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Delete))
        assertEquals("12", viewModel.uiState.value.amountInput)
    }

    @Test
    fun inputAmount_deleteEmpty() = runTest {
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Delete))
        assertEquals("", viewModel.uiState.value.amountInput)
    }

    @Test
    fun plusMinus_togglesType() = runTest {
        assertEquals(BillType.EXPENSE, viewModel.uiState.value.type)
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.PlusMinus))
        assertEquals(BillType.INCOME, viewModel.uiState.value.type)
        viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.PlusMinus))
        assertEquals(BillType.EXPENSE, viewModel.uiState.value.type)
    }

    @Test
    fun changeRemark() = runTest {
        viewModel.onIntent(RecordUiIntent.ChangeRemark("晚饭"))
        assertEquals("晚饭", viewModel.uiState.value.remark)
    }

    @Test
    fun openDatePicker() = runTest {
        viewModel.onIntent(RecordUiIntent.OpenDatePicker)
        assertTrue(viewModel.uiState.value.isDatePickerVisible)
    }

    @Test
    fun dismissDatePicker() = runTest {
        viewModel.onIntent(RecordUiIntent.OpenDatePicker)
        viewModel.onIntent(RecordUiIntent.DismissDatePicker)
        assertFalse(viewModel.uiState.value.isDatePickerVisible)
    }

    @Test
    fun confirmDate_pastDate_isAccepted() = runTest {
        val pastDate = LocalDate.now().minusDays(30)
        viewModel.onIntent(RecordUiIntent.ConfirmDate(pastDate))
        assertEquals(pastDate, viewModel.uiState.value.date)
        assertFalse(viewModel.uiState.value.isDatePickerVisible)
    }

    @Test
    fun confirmDate_today_isAccepted() = runTest {
        val today = LocalDate.now()
        viewModel.onIntent(RecordUiIntent.ConfirmDate(today))
        assertEquals(today, viewModel.uiState.value.date)
    }

    @Test
    fun confirmDate_futureDate_isClampedToToday() = runTest {
        val future = LocalDate.now().plusDays(1)
        viewModel.onIntent(RecordUiIntent.ConfirmDate(future))
        assertEquals(LocalDate.now(), viewModel.uiState.value.date)
        assertFalse(viewModel.uiState.value.isDatePickerVisible)
    }

    @Test
    fun save_withoutCategory_showsError() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(1)))
            viewModel.onIntent(RecordUiIntent.Save)
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is RecordUiEffect.ShowToast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_withoutAmount_showsError() = runTest {
        viewModel.effect.test {
            val category = Categories.expense.first()
            viewModel.onIntent(RecordUiIntent.SelectCategory(category))
            viewModel.onIntent(RecordUiIntent.Save)
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is RecordUiEffect.ShowToast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_zeroAmount_showsError() = runTest {
        viewModel.effect.test {
            val category = Categories.expense.first()
            viewModel.onIntent(RecordUiIntent.SelectCategory(category))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(0)))
            viewModel.onIntent(RecordUiIntent.Save)
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is RecordUiEffect.ShowToast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_success_navigatesBack() = runTest {
        viewModel.effect.test {
            val category = Categories.expense.first()
            viewModel.onIntent(RecordUiIntent.SelectCategory(category))
            // 12.30 → 1230 cents
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(1)))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(2)))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Dot))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(3)))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(0)))
            viewModel.onIntent(RecordUiIntent.Save)
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is RecordUiEffect.NavigateBack)
            assertEquals(1230L, fakeRepo.insertedBills.last().amount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_failure_showsToast_andResetsSaving() = runTest {
        fakeRepo.shouldThrow = true
        viewModel.effect.test {
            val category = Categories.expense.first()
            viewModel.onIntent(RecordUiIntent.SelectCategory(category))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(5)))
            viewModel.onIntent(RecordUiIntent.Save)
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is RecordUiEffect.ShowToast)
            assertFalse(viewModel.uiState.value.isSaving)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun done_keyTriggersSave() = runTest {
        viewModel.effect.test {
            val category = Categories.expense.first()
            viewModel.onIntent(RecordUiIntent.SelectCategory(category))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(5)))
            viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Done))
            testDispatcher.scheduler.advanceUntilIdle()
            val effect = awaitItem()
            assertTrue(effect is RecordUiEffect.NavigateBack)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_duplicateCall_insertedOnce() = runTest {
        val category = Categories.expense.first()
        viewModel.onIntent(RecordUiIntent.SelectCategory(category))
        repeat(3) { viewModel.onIntent(RecordUiIntent.InputAmount(AmountKey.Digit(it + 1))) }

        viewModel.onIntent(RecordUiIntent.Save)
        viewModel.onIntent(RecordUiIntent.Save)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeRepo.insertedBills.size)
    }
}

private class FakeBillRepository : BillRepository {
    val insertedBills = mutableListOf<Bill>()
    var shouldThrow = false

    override suspend fun insertBill(bill: Bill): Long {
        if (shouldThrow) throw RuntimeException("DB error")
        insertedBills.add(bill)
        return insertedBills.size.toLong()
    }

    override suspend fun deleteBill(id: Long) = Unit

    override fun observeBillsByMonth(yearMonth: YearMonth): Flow<List<Bill>> =
        MutableStateFlow(emptyList())

    override fun observeMonthlySummary(yearMonth: YearMonth): Flow<MonthlySummary> =
        MutableStateFlow(MonthlySummary())
}
