package com.example.tiffin.ui.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val state = uiState) {
                is PaywallUiState.Offer -> OfferContent(
                    state = state,
                    onPay = viewModel::onPayClicked,
                    onNotNow = onClose,
                )
                is PaywallUiState.Confirmed -> ConfirmedContent(state = state, onDone = onClose)
            }
        }
    }
}

@Composable
private fun OfferContent(
    state: PaywallUiState.Offer,
    onPay: () -> Unit,
    onNotNow: () -> Unit,
) {
    val bold = SpanStyle(fontWeight = FontWeight.Bold)

    Text("Subscribe to Tiffin", style = MaterialTheme.typography.headlineMedium)

    Text(
        text = buildAnnotatedString {
            append("You will be charged ")
            withStyle(bold) { append("₹1 now") }
            append(". Then ")
            withStyle(bold) { append("₹249") }
            append(" will be charged on ")
            withStyle(bold) { append(state.chargeDate) }
            append(", which is 24 hours after you subscribe.")
        },
        style = MaterialTheme.typography.bodyLarge,
    )

    PriceRow(label = "Today", amount = "₹1")
    PriceRow(label = state.chargeDate, amount = "₹249")

    Text(
        text = "This is a simulation. No real payment is made and no payment details are collected.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    when (state.notice) {
        PaywallNotice.None -> Unit
        PaywallNotice.TermsUpdated -> Text(
            text = "The charge date has changed because the date or time zone changed. " +
                    "Please check the new date above and confirm again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )
        PaywallNotice.SaveFailed -> Text(
            text = "We couldn't save your subscription. Please try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Button(onClick = onPay, modifier = Modifier.fillMaxWidth()) {
        Text("Pay ₹1 (simulated)")
    }
    TextButton(onClick = onNotNow) { Text("Not now") }
}

@Composable
private fun ConfirmedContent(state: PaywallUiState.Confirmed, onDone: () -> Unit) {
    Text("You're subscribed", style = MaterialTheme.typography.headlineMedium)
    Text(
        text = "Simulated ₹1 payment recorded on ${state.purchasedAt}.",
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = "₹249 will be charged on ${state.chargeAt}.",
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = "This is a simulation. No real payment was made.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
}

@Composable
private fun PriceRow(label: String, amount: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(amount, style = MaterialTheme.typography.titleLarge)
        }
    }
}