package com.example.tiffin

import android.util.Log

/** Single entry point for product analytics. */
interface Analytics {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
}

/** Writes events to Logcat. Filter with: tag:TiffinAnalytics */
class LogcatAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, Any>) {
        val details = if (params.isEmpty()) {
            ""
        } else {
            params.entries.joinToString(prefix = " ", separator = ", ") { "${it.key}=${it.value}" }
        }
        Log.i(TAG, "$name$details")
    }

    private companion object {
        const val TAG = "TiffinAnalytics"
    }
}

/** The four events we track. Keep this list at four. */
object AnalyticsEvents {
    const val APP_LAUNCHED = "app_launched"
    const val KITCHEN_SELECTED = "kitchen_selected"
    const val PAYWALL_VIEWED = "paywall_viewed"
    const val SUBSCRIPTION_PURCHASED = "subscription_purchased"
}

/** Minimal holder so call sites don't construct their own implementation. */
object AppAnalytics {
    val instance: Analytics = LogcatAnalytics()
}