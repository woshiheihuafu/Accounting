package com.cocos.androidaccounting.ui.record

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cocos.androidaccounting.R
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.ui.theme.LocalAccountingColors
import com.cocos.androidaccounting.ui.theme.LocalAccountingTypography
import com.cocos.androidaccounting.util.formatRecordDate

@Composable
fun RecordContent(
    uiState: RecordUiState,
    onIntent: (RecordUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAccountingColors.current
    val activeColor = if (uiState.type == BillType.EXPENSE) colors.expenseRed else colors.incomeGreen

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (uiState.type == BillType.EXPENSE) 0 else 1,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = activeColor,
        ) {
            Tab(
                selected = uiState.type == BillType.EXPENSE,
                onClick = { onIntent(RecordUiIntent.SelectType(BillType.EXPENSE)) },
                text = {
                    Text(
                        text = stringResource(R.string.record_tab_expense),
                        color = if (uiState.type == BillType.EXPENSE) colors.expenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (uiState.type == BillType.EXPENSE) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
            Tab(
                selected = uiState.type == BillType.INCOME,
                onClick = { onIntent(RecordUiIntent.SelectType(BillType.INCOME)) },
                text = {
                    Text(
                        text = stringResource(R.string.record_tab_income),
                        color = if (uiState.type == BillType.INCOME) colors.incomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (uiState.type == BillType.INCOME) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }

        CategoryGrid(
            categories = uiState.categories,
            selected = uiState.selectedCategory,
            onSelect = { onIntent(RecordUiIntent.SelectCategory(it)) },
            type = uiState.type,
        )

        Spacer(modifier = Modifier.weight(1f))

        val displayAmount = if (uiState.amountInput.isEmpty()) "¥0" else "¥${uiState.amountInput}"
        Text(
            text = displayAmount,
            style = LocalAccountingTypography.current.amountDisplay.copy(
                color = if (uiState.amountInput.isEmpty())
                    MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
            ),
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        TextField(
            value = uiState.remark,
            onValueChange = { onIntent(RecordUiIntent.ChangeRemark(it)) },
            placeholder = {
                Text(
                    text = stringResource(R.string.record_remark_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIntent(RecordUiIntent.OpenDatePicker) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatRecordDate(uiState.date),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        AmountKeyboard(
            onKey = { onIntent(RecordUiIntent.InputAmount(it)) },
            isSaving = uiState.isSaving,
        )
    }
}
