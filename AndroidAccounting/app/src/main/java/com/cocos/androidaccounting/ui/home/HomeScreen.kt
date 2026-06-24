package com.cocos.androidaccounting.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cocos.androidaccounting.navigation.Route
import com.cocos.androidaccounting.ui.component.AccountingBottomBar

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onIntent: (HomeUiIntent) -> Unit,
) {
    Scaffold(
        bottomBar = {
            AccountingBottomBar(
                currentRoute = Route.Home,
                onNavigate = { route ->
                    if (route == Route.Record) {
                        onIntent(HomeUiIntent.NavigateToRecord)
                    }
                },
                onComingSoon = { onIntent(HomeUiIntent.OnBottomBarComingSoon) },
            )
        },
    ) { innerPadding ->
        HomeContent(
            groups = uiState.groups,
            isLoading = uiState.isLoading,
            summary = uiState.summary,
            yearMonth = uiState.yearMonth,
            onMonthClick = { onIntent(HomeUiIntent.OpenYearMonthPicker) },
            onBillClick = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )

        if (uiState.isYearMonthPickerVisible) {
            YearMonthPickerDialog(
                initialYear = uiState.yearMonth.year,
                initialMonth = uiState.yearMonth.monthValue,
                onConfirm = { year, month ->
                    onIntent(HomeUiIntent.ConfirmYearMonthSelection(year, month))
                },
                onDismiss = { onIntent(HomeUiIntent.DismissYearMonthPicker) },
            )
        }
    }
}
