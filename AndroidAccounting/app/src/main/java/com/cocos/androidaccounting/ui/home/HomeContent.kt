package com.cocos.androidaccounting.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocos.androidaccounting.R
import com.cocos.androidaccounting.data.model.Bill
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.Categories
import com.cocos.androidaccounting.data.model.MonthlySummary
import com.cocos.androidaccounting.ui.theme.LocalAccountingColors
import com.cocos.androidaccounting.ui.theme.LocalAccountingTypography
import com.cocos.androidaccounting.util.DateUtil
import com.cocos.androidaccounting.util.MoneyFormatter
import java.time.YearMonth

@Composable
fun HomeContent(
    groups: List<BillGroup>,
    isLoading: Boolean,
    summary: MonthlySummary,
    yearMonth: YearMonth,
    onMonthClick: () -> Unit,
    onBillClick: (Bill) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { HomeHeader() }
        item { MonthSelectorRow(yearMonth = yearMonth, onMonthClick = onMonthClick) }
        item { SummaryCard(summary = summary) }
        item { DetailSectionTitle() }

        if (groups.isEmpty() && !isLoading) {
            item { HomeEmptyState() }
        } else {
            groups.forEach { group ->
                item(key = "header_${group.date}") {
                    DateGroupHeader(group = group)
                }
                items(group.bills, key = { it.id }) { bill ->
                    BillItemRow(bill = bill, onClick = { onBillClick(bill) })
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Text(
        text = stringResource(R.string.home_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun MonthSelectorRow(yearMonth: YearMonth, onMonthClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMonthClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = DateUtil.formatMonthLabel(yearMonth),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "▾",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = yearMonth.year.toString(),
            style = LocalAccountingTypography.current.amountSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryCard(summary: MonthlySummary) {
    val accountingColors = LocalAccountingColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_label_expense),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = MoneyFormatter.formatYuan(summary.expense),
                style = LocalAccountingTypography.current.amountMedium,
                color = accountingColors.expenseRed,
            )
        }
        VerticalDivider(
            modifier = Modifier.height(40.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_label_income),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = MoneyFormatter.formatYuan(summary.income),
                style = LocalAccountingTypography.current.amountMedium,
                color = accountingColors.incomeGreen,
            )
        }
    }
}

@Composable
private fun DetailSectionTitle() {
    Text(
        text = stringResource(R.string.home_section_detail),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun DateGroupHeader(group: BillGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = DateUtil.formatGroupHeader(group.date),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (group.dayNetAmount != 0L) {
            val text = if (group.dayNetAmount > 0L)
                "+${MoneyFormatter.formatYuan(group.dayNetAmount)}"
            else
                "-${MoneyFormatter.formatYuan(-group.dayNetAmount)}"
            Text(
                text = text,
                style = LocalAccountingTypography.current.amountSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BillItemRow(bill: Bill, onClick: () -> Unit) {
    val accountingColors = LocalAccountingColors.current
    val amountColor = if (bill.type == BillType.EXPENSE) accountingColors.expenseRed
    else accountingColors.incomeGreen

    Row(
        modifier = Modifier
            .testTag("bill_item_${bill.id}")
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = Categories.iconResFor(bill.category)),
            contentDescription = bill.category,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bill.category,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (bill.remark.isNotBlank()) {
                Text(
                    text = bill.remark,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = MoneyFormatter.formatSignedYuan(bill.type, bill.amount),
            style = LocalAccountingTypography.current.amountMedium,
            color = amountColor,
        )
    }
}

@Composable
private fun HomeEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
            .testTag("home_empty"),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.home_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
