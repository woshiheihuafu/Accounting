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
import java.time.Year
import java.time.YearMonth

@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentYear = remember { Year.now().value }
    val years = remember { (currentYear - 10..currentYear + 10).map { it.toString() } }
    val months = remember { (1..12).map { "${it}月" } }

    var yearIndex by rememberSaveable {
        mutableIntStateOf((initialDate.year - (currentYear - 10)).coerceIn(0, years.lastIndex))
    }
    var monthIndex by rememberSaveable {
        mutableIntStateOf((initialDate.monthValue - 1).coerceIn(0, months.lastIndex))
    }

    val daysInMonth = remember(yearIndex, monthIndex) {
        YearMonth.of(years[yearIndex].toInt(), monthIndex + 1).lengthOfMonth()
    }
    val days = remember(daysInMonth) { (1..daysInMonth).map { "${it}日" } }

    var dayIndex by rememberSaveable {
        mutableIntStateOf((initialDate.dayOfMonth - 1).coerceIn(0, daysInMonth - 1))
    }

    LaunchedEffect(daysInMonth) {
        if (dayIndex >= daysInMonth) dayIndex = daysInMonth - 1
    }

    BottomSheetPicker(
        title = stringResource(R.string.record_date_picker_title),
        onDismiss = onDismiss,
        onConfirm = {
            val selectedYear = years[yearIndex].toInt()
            val selectedMonth = monthIndex + 1
            val selectedDay = dayIndex + 1
            onConfirm(LocalDate.of(selectedYear, selectedMonth, selectedDay))
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
                selectedIndex = monthIndex,
                onIndexChange = { monthIndex = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("date_picker_month_wheel"),
            )
            WheelPicker(
                items = days,
                selectedIndex = dayIndex,
                onIndexChange = { dayIndex = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("date_picker_day_wheel"),
            )
        }
    }
}
