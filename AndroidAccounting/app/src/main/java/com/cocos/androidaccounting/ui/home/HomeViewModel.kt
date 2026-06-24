package com.cocos.androidaccounting.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocos.androidaccounting.R
import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.MonthlySummary
import com.cocos.androidaccounting.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

private sealed interface DataState {
    data object Loading : DataState
    data class Success(val groups: List<BillGroup>, val summary: MonthlySummary) : DataState
    data class Error(val message: String) : DataState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BillRepository,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val pickerVisible = MutableStateFlow(false)

    private val _effect = MutableSharedFlow<HomeUiEffect>()
    val effect: SharedFlow<HomeUiEffect> = _effect.asSharedFlow()

    private val dataFlow: Flow<DataState> = selectedMonth
        .flatMapLatest { ym ->
            combine<List<Bill>, MonthlySummary, DataState>(
                repository.observeBillsByMonth(ym),
                repository.observeMonthlySummary(ym),
            ) { bills, summary ->
                DataState.Success(groups = bills.toBillGroups(), summary = summary)
            }
            .onStart { emit(DataState.Loading) }
            .catch { e -> emit(DataState.Error(e.message ?: "加载失败")) }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        selectedMonth, pickerVisible, dataFlow,
    ) { ym, picker, data ->
        when (data) {
            is DataState.Loading -> HomeUiState(
                yearMonth = ym, isLoading = true, isYearMonthPickerVisible = picker,
            )
            is DataState.Success -> HomeUiState(
                yearMonth = ym, summary = data.summary, groups = data.groups,
                isLoading = false, isYearMonthPickerVisible = picker,
            )
            is DataState.Error -> HomeUiState(
                yearMonth = ym, isLoading = false, error = data.message,
                isYearMonthPickerVisible = picker,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.OpenYearMonthPicker -> pickerVisible.value = true
            is HomeUiIntent.DismissYearMonthPicker -> pickerVisible.value = false
            is HomeUiIntent.ConfirmYearMonthSelection -> {
                selectedMonth.value = YearMonth.of(intent.year, intent.month)
                pickerVisible.value = false
            }
            is HomeUiIntent.NavigateToRecord -> {
                viewModelScope.launch { _effect.emit(HomeUiEffect.NavigateToRecord) }
            }
            is HomeUiIntent.OnBottomBarComingSoon -> {
                viewModelScope.launch {
                    _effect.emit(HomeUiEffect.ShowToast(R.string.coming_soon))
                }
            }
        }
    }
}

private fun List<Bill>.toBillGroups(): List<BillGroup> =
    groupBy { it.date }
        .map { (date, bills) ->
            BillGroup(
                date = date,
                bills = bills,
                dayExpense = bills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount },
            )
        }
