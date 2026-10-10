package com.example.tiffin.ui.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tiffin.data.UserPrefs

// PLACEHOLDER: the real paywall (₹1 now, ₹249 later, simulated purchase) is built in S5.
@Composable
fun PaywallScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Paywall placeholder")
            // TEMP (S4 test aid): replaced by the real simulated purchase in S5.
            Button(onClick = { UserPrefs.get(context).markPurchased(System.currentTimeMillis()) }) {
                Text("TEMP: simulate payment")
            }
            Button(onClick = onClose) { Text("Close") }
        }
    }
}