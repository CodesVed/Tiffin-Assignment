package com.example.tiffin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tiffin.data.UserPrefs
import com.example.tiffin.data.shouldShowPaywallOnLaunch
import com.example.tiffin.ui.TiffinNavHost
import com.example.tiffin.ui.theme.TiffinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPrefs = UserPrefs.get(this)

        // A "launch" is a brand-new Activity instance (savedInstanceState == null).
        // Rotation and process-death restore pass a non-null Bundle, so they are never counted
        // and never re-trigger the paywall.
        val showPaywallOnStart = if (savedInstanceState == null) {
            shouldShowPaywallOnLaunch(userPrefs.recordLaunch(), userPrefs.isPaid)
        } else {
            false
        }

        Log.d(
            "TiffinLaunch",
            "newLaunch=${savedInstanceState == null} launchCount=${userPrefs.launchCount} " +
                    "paid=${userPrefs.isPaid} showPaywall=$showPaywallOnStart",
        )

        enableEdgeToEdge()
        setContent {
            TiffinTheme {
                TiffinNavHost(showPaywallOnStart = showPaywallOnStart)
            }
        }
    }
}