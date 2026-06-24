package com.cocos.androidaccounting.ui.record

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cocos.androidaccounting.data.model.BillType
import com.cocos.androidaccounting.data.model.Category
import com.cocos.androidaccounting.ui.theme.LocalAccountingColors

@Composable
fun CategoryGrid(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category) -> Unit,
    type: BillType,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAccountingColors.current
    val activeColor = if (type == BillType.EXPENSE) colors.expenseRed else colors.incomeGreen

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        categories.forEach { category ->
            val isSelected = category == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(category) }
                    .padding(vertical = 8.dp)
                    .testTag("record_category_${category.name}"),
            ) {
                Icon(
                    painter = painterResource(id = category.iconRes),
                    contentDescription = category.name,
                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
