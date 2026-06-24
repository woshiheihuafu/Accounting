package com.cocos.androidaccounting.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cocos.androidaccounting.R
import com.cocos.androidaccounting.navigation.Route
import com.cocos.androidaccounting.ui.theme.SageGreen

private data class BottomBarItem(
    @param:StringRes val labelRes: Int,
    @param:StringRes val descRes: Int,
    val route: Route?,
    val testTag: String,
    val imageVector: ImageVector,
)

@Composable
fun AccountingBottomBar(
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val leftItems = remember {
        listOf(
            BottomBarItem(
                labelRes = R.string.nav_home,
                descRes = R.string.nav_home_desc,
                route = Route.Home,
                testTag = "bottom_bar_home",
                imageVector = Icons.Outlined.Home,
            ),
            BottomBarItem(
                labelRes = R.string.nav_chart,
                descRes = R.string.nav_chart_desc,
                route = null,
                testTag = "bottom_bar_chart",
                imageVector = Icons.Outlined.PieChart,
            ),
        )
    }

    val rightItems = remember {
        listOf(
            BottomBarItem(
                labelRes = R.string.nav_discover,
                descRes = R.string.nav_discover_desc,
                route = null,
                testTag = "bottom_bar_discover",
                imageVector = Icons.Outlined.Explore,
            ),
            BottomBarItem(
                labelRes = R.string.nav_profile,
                descRes = R.string.nav_profile_desc,
                route = null,
                testTag = "bottom_bar_profile",
                imageVector = Icons.Outlined.AccountCircle,
            ),
        )
    }

    NavigationBar(modifier = modifier) {
        leftItems.forEach { item ->
            val label = stringResource(item.labelRes)
            val desc = stringResource(item.descRes)
            NavigationBarItem(
                selected = item.route == currentRoute,
                onClick = {
                    if (item.route != null) {
                        onNavigate(item.route)
                    } else {
                        onComingSoon()
                    }
                },
                icon = { Icon(item.imageVector, contentDescription = desc) },
                label = { Text(text = label) },
                modifier = Modifier.testTag(item.testTag),
            )
        }

        NavigationBarItem(
            selected = false,
            onClick = { onNavigate(Route.Record) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = SageGreen,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .testTag("bottom_bar_record"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.nav_record_desc),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            label = { Text(text = stringResource(R.string.nav_record)) },
            modifier = Modifier.testTag("bottom_bar_record_item"),
        )

        rightItems.forEach { item ->
            val label = stringResource(item.labelRes)
            val desc = stringResource(item.descRes)
            NavigationBarItem(
                selected = item.route == currentRoute,
                onClick = {
                    if (item.route != null) {
                        onNavigate(item.route)
                    } else {
                        onComingSoon()
                    }
                },
                icon = { Icon(item.imageVector, contentDescription = desc) },
                label = { Text(text = label) },
                modifier = Modifier.testTag(item.testTag),
            )
        }
    }
}
