package com.example.tiffin.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchPolicyTest {

    @Test
    fun firstAndSecondLaunch_neverShowPaywall() {
        assertFalse(shouldShowPaywallOnLaunch(launchCount = 1, isPaid = false))
        assertFalse(shouldShowPaywallOnLaunch(launchCount = 2, isPaid = false))
    }

    @Test
    fun thirdLaunch_unpaid_showsPaywall() {
        assertTrue(shouldShowPaywallOnLaunch(launchCount = 3, isPaid = false))
    }

    @Test
    fun thirdLaunch_paid_doesNotShowPaywall() {
        assertFalse(shouldShowPaywallOnLaunch(launchCount = 3, isPaid = true))
    }

    @Test
    fun laterLaunches_doNotShowPaywall_underCurrentAssumption() {
        listOf(4, 5, 100).forEach {
            assertFalse(shouldShowPaywallOnLaunch(launchCount = it, isPaid = false))
        }
    }

    @Test
    fun paidUser_neverSeesPaywall() {
        (1..10).forEach { assertFalse(shouldShowPaywallOnLaunch(launchCount = it, isPaid = true)) }
    }
}