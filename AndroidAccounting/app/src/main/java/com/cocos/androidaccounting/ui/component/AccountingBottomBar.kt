package com.cocos.androidaccounting.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
    @param:DrawableRes val iconRes: Int,
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
                iconRes = R.drawable.ic_tab_detail,
            ),
            BottomBarItem(
                labelRes = R.string.nav_chart,
                descRes = R.string.nav_chart_desc,
                route = null,
                testTag = "bottom_bar_chart",
                iconRes = R.drawable.ic_tab_chart,
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
                iconRes = R.drawable.ic_tab_discover,
            ),
            BottomBarItem(
                labelRes = R.string.nav_profile,
                descRes = R.string.nav_profile_desc,
                route = null,
                testTag = "bottom_bar_profile",
                iconRes = R.drawable.ic_tab_me,
            ),
        )
    }

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onBackground,
        selectedTextColor = MaterialTheme.colorScheme.onBackground,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = Color.Transparent,
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
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
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = desc,
                        modifier = Modifier.size(22.dp),
                    )
                },
                label = { Text(text = label) },
                colors = itemColors,
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
                        painter = painterResource(R.drawable.ic_tab_record),
                        contentDescription = stringResource(R.string.nav_record_desc),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            },
            label = { Text(text = stringResource(R.string.nav_record)) },
            colors = itemColors,
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
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = desc,
                        modifier = Modifier.size(22.dp),
                    )
                },
                label = { Text(text = label) },
                colors = itemColors,
                modifier = Modifier.testTag(item.testTag),
            )
        }
    }
}
