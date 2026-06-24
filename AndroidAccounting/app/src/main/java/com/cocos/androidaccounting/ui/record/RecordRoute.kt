package com.cocos.androidaccounting.ui.record

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle

@Composable
fun RecordRoute(
    onNavigateBack: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel.effect, lifecycle) {
        viewModel.effect
            .flowWithLifecycle(lifecycle)
            .collect { effect ->
                when (effect) {
                    RecordUiEffect.NavigateBack -> onNavigateBack()
                    is RecordUiEffect.ShowToast ->
                        Toast.makeText(context, effect.messageRes, Toast.LENGTH_SHORT).show()
                }
            }
    }

    RecordScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onClose = onNavigateBack,
    )
}
