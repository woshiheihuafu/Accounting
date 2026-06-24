package com.cocos.androidaccounting.ui.record

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocos.androidaccounting.R
import com.cocos.androidaccounting.ui.component.BottomSheetPicker
import com.cocos.androidaccounting.ui.component.WheelPicker
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    // 不允许选择未来日期：年/月/日上限均钳制到今天
    val today = remember { LocalDate.now() }
    val currentYear = today.year
    val years = remember { (currentYear - 10..currentYear).map { it.toString() } }

    var yearIndex by rememberSaveable {
        mutableIntStateOf((initialDate.year - (currentYear - 10)).coerceIn(0, years.lastIndex))
    }
    var monthIndex by rememberSaveable {
        mutableIntStateOf((initialDate.monthValue - 1).coerceIn(0, 11))
    }
    var dayIndex by rememberSaveable {
        mutableIntStateOf((initialDate.dayOfMonth - 1).coerceAtLeast(0))
    }

    val selectedYear = years[yearIndex].toInt()
    val maxMonth = if (selectedYear == currentYear) today.monthValue else 12
    val months = remember(maxMonth) { (1..maxMonth).map { "${it}月" } }
    val safeMonthIndex = monthIndex.coerceIn(0, months.lastIndex)
    val selectedMonth = safeMonthIndex + 1

    LaunchedEffect(maxMonth) {
        if (monthIndex > maxMonth - 1) monthIndex = maxMonth - 1
    }

    val lengthOfMonth = remember(selectedYear, selectedMonth) {
        YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    }
    val maxDay =
        if (selectedYear == currentYear && selectedMonth == today.monthValue) today.dayOfMonth
        else lengthOfMonth
    val days = remember(maxDay) { (1..maxDay).map { "${it}日" } }
    val safeDayIndex = dayIndex.coerceIn(0, days.lastIndex)

    LaunchedEffect(maxDay) {
        if (dayIndex > maxDay - 1) dayIndex = maxDay - 1
    }

    BottomSheetPicker(
        title = stringResource(R.string.record_date_picker_title),
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(LocalDate.of(selectedYear, selectedMonth, safeDayIndex + 1))
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            WheelPicker(
                items = years,
                selectedIndex = yearIndex,
                onIndexChange = { yearIndex = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("date_picker_year_wheel"),
            )
            WheelPicker(
                items = months,
                selectedIndex = safeMonthIndex,
                onIndexChange = { monthIndex = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("date_picker_month_wheel"),
            )
            WheelPicker(
                items = days,
                selectedIndex = safeDayIndex,
                onIndexChange = { dayIndex = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("date_picker_day_wheel"),
            )
        }
    }
}
