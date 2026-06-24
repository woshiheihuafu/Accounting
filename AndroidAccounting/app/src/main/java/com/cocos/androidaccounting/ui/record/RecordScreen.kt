package com.cocos.androidaccounting.ui.record

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocos.androidaccounting.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    uiState: RecordUiState,
    onIntent: (RecordUiIntent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.record_close_desc),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
    ) { innerPadding ->
        RecordContent(
            uiState = uiState,
            onIntent = onIntent,
            modifier = Modifier.padding(innerPadding),
        )

        if (uiState.isDatePickerVisible) {
            DatePickerDialog(
                initialDate = uiState.date,
                onConfirm = { onIntent(RecordUiIntent.ConfirmDate(it)) },
                onDismiss = { onIntent(RecordUiIntent.DismissDatePicker) },
            )
        }
    }
}
