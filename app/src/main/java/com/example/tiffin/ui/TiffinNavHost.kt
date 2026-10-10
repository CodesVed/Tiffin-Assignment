package com.example.tiffin.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tiffin.ui.detail.KitchenDetailScreen
import com.example.tiffin.ui.list.KitchenListScreen
import com.example.tiffin.ui.paywall.PaywallScreen

@Composable
fun TiffinNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = KitchenListRoute,
        modifier = modifier,
    ) {
        composable<KitchenListRoute> {
            KitchenListScreen(
                onKitchenClick = { id -> navController.navigate(KitchenDetailRoute(id)) },
            )
        }
        composable<KitchenDetailRoute> {
            KitchenDetailScreen(
                onBack = { navController.popBackStack() },
                onSubscribe = { navController.navigate(PaywallRoute) },
            )
        }
        composable<PaywallRoute> {
            PaywallScreen(onClose = { navController.popBackStack() })
        }
    }
}