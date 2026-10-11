package com.example.tiffin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tiffin.AnalyticsEvents
import com.example.tiffin.AppAnalytics
import com.example.tiffin.ui.detail.KitchenDetailScreen
import com.example.tiffin.ui.list.KitchenListScreen
import com.example.tiffin.ui.paywall.PaywallScreen

@Composable
fun TiffinNavHost(showPaywallOnStart: Boolean, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = KitchenListRoute,
        modifier = modifier,
    ) {
        composable<KitchenListRoute> {
            KitchenListScreen(
                onKitchenClick = { id ->
                    // Ignore a second tap while the first navigation is still running:
                    // no duplicate event and no duplicate detail screen.
                    if (navController.isResumed()) {
                        AppAnalytics.instance.logEvent(
                            AnalyticsEvents.KITCHEN_SELECTED,
                            mapOf("kitchen_id" to id),
                        )
                        navController.navigate(KitchenDetailRoute(id))
                    }
                },
            )
        }
        composable<KitchenDetailRoute> {
            KitchenDetailScreen(
                onBack = { navController.popBackStack() },
                onSubscribe = {
                    if (navController.isResumed()) {
                        navController.navigate(PaywallRoute(PaywallSource.SUBSCRIBE_TAP))
                    }
                },
            )
        }
        composable<PaywallRoute> {
            PaywallScreen(onClose = { navController.popBackStack() })
        }
    }

    // One-shot. showPaywallOnStart is true only for a freshly created Activity (see MainActivity),
    // so rotation and process-death restore never run this navigation a second time.
    LaunchedEffect(Unit) {
        if (showPaywallOnStart) navController.navigate(PaywallRoute(PaywallSource.LAUNCH))
    }
}

/** True only when the current screen is fully visible and not mid-transition. */
private fun NavController.isResumed(): Boolean =
    currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED