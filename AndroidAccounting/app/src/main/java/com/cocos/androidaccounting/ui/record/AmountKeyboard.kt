package com.cocos.androidaccounting.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocos.androidaccounting.R

private val KeyHeight = 56.dp

@Composable
fun AmountKeyboard(
    onKey: (AmountKey) -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("record_keyboard"),
    ) {
        Column(modifier = Modifier.weight(3f)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberKey(1, onKey, Modifier.weight(1f))
                NumberKey(2, onKey, Modifier.weight(1f))
                NumberKey(3, onKey, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberKey(4, onKey, Modifier.weight(1f))
                NumberKey(5, onKey, Modifier.weight(1f))
                NumberKey(6, onKey, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                NumberKey(7, onKey, Modifier.weight(1f))
                NumberKey(8, onKey, Modifier.weight(1f))
                NumberKey(9, onKey, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                KeyCell(
                    modifier = Modifier
                        .weight(1f)
                        .height(KeyHeight)
                        .testTag("record_key_dot"),
                    onClick = { onKey(AmountKey.Dot) },
                ) {
                    Text(
                        text = ".",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                KeyCell(
                    modifier = Modifier
                        .weight(2f)
                        .height(KeyHeight)
                        .testTag("record_key_0"),
                    onClick = { onKey(AmountKey.Digit(0)) },
                ) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            KeyCell(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(KeyHeight)
                    .testTag("record_key_delete"),
                onClick = { onKey(AmountKey.Delete) },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.record_delete_desc),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            KeyCell(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(KeyHeight)
                    .testTag("record_key_plus_minus"),
                onClick = { onKey(AmountKey.PlusMinus) },
            ) {
                Text(text = "+/−", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            KeyCell(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(KeyHeight * 2)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("record_key_done"),
                onClick = { if (!isSaving) onKey(AmountKey.Done) },
            ) {
                Text(
                    text = stringResource(R.string.record_key_done),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun NumberKey(n: Int, onKey: (AmountKey) -> Unit, modifier: Modifier = Modifier) {
    KeyCell(
        modifier = modifier.height(KeyHeight).testTag("record_key_$n"),
        onClick = { onKey(AmountKey.Digit(n)) },
    ) {
        Text(
            text = n.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun KeyCell(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
