package com.cocos.androidaccounting.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cocos.androidaccounting.ui.home.HomeRoute
import com.cocos.androidaccounting.ui.record.RecordRoute

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier,
    ) {
        composable<Route.Home> {
            HomeRoute(onNavigateToRecord = { navController.navigate(Route.Record) })
        }
        composable<Route.Record> {
            RecordRoute(onNavigateBack = { navController.popBackStack() })
        }
    }
}
