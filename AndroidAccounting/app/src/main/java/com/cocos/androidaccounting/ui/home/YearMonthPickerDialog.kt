package com.cocos.androidaccounting.ui.home

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
import java.time.YearMonth

@Composable
fun YearMonthPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    onConfirm: (year: Int, month: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    // 不允许选择未来年月：年上限为今年，今年时月上限为当前月
    val now = remember { YearMonth.now() }
    val currentYear = now.year
    val years = remember { (currentYear - 10..currentYear).map { it.toString() } }

    var yearIndex by rememberSaveable {
        mutableIntStateOf((initialYear - (currentYear - 10)).coerceIn(0, years.lastIndex))
    }
    var monthIndex by rememberSaveable {
        mutableIntStateOf((initialMonth - 1).coerceIn(0, 11))
    }

    val selectedYear = years[yearIndex].toInt()
    val maxMonth = if (selectedYear == currentYear) now.monthValue else 12
    val months = remember(maxMonth) { (1..maxMonth).map { "${it}月" } }
    val safeMonthIndex = monthIndex.coerceIn(0, months.lastIndex)

    LaunchedEffect(maxMonth) {
        if (monthIndex > maxMonth - 1) monthIndex = maxMonth - 1
    }

    BottomSheetPicker(
        title = stringResource(R.string.home_year_month_picker_title),
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(selectedYear, safeMonthIndex + 1)
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
                modifier = Modifier.weight(1f).testTag("year_month_picker_year_wheel"),
            )
            WheelPicker(
                items = months,
                selectedIndex = safeMonthIndex,
                onIndexChange = { monthIndex = it },
                modifier = Modifier.weight(1f).testTag("year_month_picker_month_wheel"),
            )
        }
    }
}
