package com.example.tiffin.ui

import kotlinx.serialization.Serializable

@Serializable
data object KitchenListRoute

@Serializable
data class KitchenDetailRoute(val kitchenId: String)

/** [source] says what opened the paywall; see [PaywallSource]. */
@Serializable
data class PaywallRoute(val source: String)

object PaywallSource {
    const val LAUNCH = "launch"
    const val SUBSCRIBE_TAP = "subscribe_tap"
}