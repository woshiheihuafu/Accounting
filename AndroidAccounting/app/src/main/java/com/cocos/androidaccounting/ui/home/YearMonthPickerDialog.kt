package com.cocos.androidaccounting.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocos.androidaccounting.R
import com.cocos.androidaccounting.ui.component.BottomSheetPicker
import com.cocos.androidaccounting.ui.component.WheelPicker
import java.time.Year

@Composable
fun YearMonthPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    onConfirm: (year: Int, month: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentYear = remember { Year.now().value }
    val years = remember { (currentYear - 10..currentYear + 10).map { it.toString() } }
    val months = remember { (1..12).map { "${it}月" } }

    var yearIndex by rememberSaveable {
        mutableIntStateOf((initialYear - (currentYear - 10)).coerceIn(0, years.lastIndex))
    }
    var monthIndex by rememberSaveable {
        mutableIntStateOf((initialMonth - 1).coerceIn(0, months.lastIndex))
    }

    BottomSheetPicker(
        title = stringResource(R.string.home_year_month_picker_title),
        onDismiss = onDismiss,
        onConfirm = {
            val selectedYear = years[yearIndex].toInt()
            val selectedMonth = monthIndex + 1
            onConfirm(selectedYear, selectedMonth)
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
                modifier = Modifier.weight(1f),
            )
            WheelPicker(
                items = months,
                selectedIndex = monthIndex,
                onIndexChange = { monthIndex = it },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
