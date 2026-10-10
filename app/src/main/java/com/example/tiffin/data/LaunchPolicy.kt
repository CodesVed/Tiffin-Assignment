package com.example.tiffin.data

const val PAYWALL_LAUNCH_NUMBER = 3

/**
 * Assumption (also stated in the README): the paywall opens on exactly the 3rd launch,
 * not on every launch from the 3rd onwards. A paid user never sees it.
 */
fun shouldShowPaywallOnLaunch(launchCount: Int, isPaid: Boolean): Boolean =
    !isPaid && launchCount == PAYWALL_LAUNCH_NUMBER