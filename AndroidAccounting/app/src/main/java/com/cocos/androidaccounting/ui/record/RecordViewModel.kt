package com.cocos.androidaccounting.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocos.androidaccounting.R
import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.Categories
import java.time.Instant
import java.time.LocalDate
import com.cocos.androidaccounting.data.repository.BillRepository
import com.cocos.androidaccounting.util.amountInputToCents
import com.cocos.androidaccounting.util.appendDigit
import com.cocos.androidaccounting.util.appendDot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: BillRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RecordUiEffect>()
    val effect: SharedFlow<RecordUiEffect> = _effect.asSharedFlow()

    fun onIntent(intent: RecordUiIntent) {
        when (intent) {
            is RecordUiIntent.SelectType -> _uiState.update {
                it.copy(type = intent.type, selectedCategory = null, categories = Categories.byType(intent.type))
            }
            is RecordUiIntent.SelectCategory -> _uiState.update {
                it.copy(selectedCategory = intent.category)
            }
            is RecordUiIntent.InputAmount -> handleAmountKey(intent.key)
            is RecordUiIntent.ChangeRemark -> _uiState.update {
                it.copy(remark = intent.text.take(MAX_REMARK_LENGTH))
            }
            RecordUiIntent.OpenDatePicker -> _uiState.update { it.copy(isDatePickerVisible = true) }
            RecordUiIntent.DismissDatePicker -> _uiState.update { it.copy(isDatePickerVisible = false) }
            is RecordUiIntent.ConfirmDate -> _uiState.update {
                // 防御：不允许保存未来日期，超出今天则钳制为今天
                val today = LocalDate.now()
                val safeDate = if (intent.date.isAfter(today)) today else intent.date
                it.copy(date = safeDate, isDatePickerVisible = false)
            }
            RecordUiIntent.Save -> save()
        }
    }

    private fun handleAmountKey(key: AmountKey) {
        when (key) {
            is AmountKey.Digit -> _uiState.update {
                it.copy(amountInput = appendDigit(it.amountInput, key.n))
            }
            AmountKey.Dot -> _uiState.update {
                it.copy(amountInput = appendDot(it.amountInput))
            }
            AmountKey.Delete -> _uiState.update {
                it.copy(amountInput = it.amountInput.dropLast(1))
            }
            AmountKey.PlusMinus -> _uiState.update {
                val newType = if (it.type == BillType.EXPENSE) BillType.INCOME else BillType.EXPENSE
                it.copy(type = newType, selectedCategory = null, categories = Categories.byType(newType))
            }
            AmountKey.Done -> save()
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val category = state.selectedCategory
        if (category == null) {
            emitEffect(RecordUiEffect.ShowToast(R.string.record_error_no_category))
            return
        }
        val cents = amountInputToCents(state.amountInput)
        if (cents == null || cents <= 0L) {
            emitEffect(RecordUiEffect.ShowToast(R.string.record_error_no_amount))
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.insertBill(
                    Bill(
                        type = state.type,
                        category = category.name,
                        amount = cents,
                        date = state.date,
                        remark = state.remark,
                        createdAt = Instant.now(),
                    )
                )
                emitEffect(RecordUiEffect.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                emitEffect(RecordUiEffect.ShowToast(R.string.record_error_save_failed))
            }
        }
    }

    private fun emitEffect(e: RecordUiEffect) = viewModelScope.launch { _effect.emit(e) }

    companion object {
        const val MAX_REMARK_LENGTH = 20
    }
}
