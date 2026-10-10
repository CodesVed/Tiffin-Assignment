package com.example.tiffin.ui

import kotlinx.serialization.Serializable

@Serializable
data object KitchenListRoute

@Serializable
data class KitchenDetailRoute(val kitchenId: String)

@Serializable
data object PaywallRoute