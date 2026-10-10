package com.example.tiffin.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiffin.data.Kitchen
import com.example.tiffin.ui.list.LoadingContent
import com.example.tiffin.ui.list.MessageContent
import com.example.tiffin.ui.list.VegBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenDetailScreen(
    onBack: () -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KitchenDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val purchasedAtMillis by viewModel.purchasedAtMillis.collectAsStateWithLifecycle()
    val isPaid = purchasedAtMillis > 0L

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text((uiState as? KitchenDetailUiState.Success)?.kitchen?.name ?: "Kitchen")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            if (uiState is KitchenDetailUiState.Success) {
                SubscribeBar(isPaid = isPaid, onSubscribe = onSubscribe)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                KitchenDetailUiState.Loading -> LoadingContent()
                is KitchenDetailUiState.Success -> KitchenDetailContent(state.kitchen)
                KitchenDetailUiState.NotFound -> MessageContent(
                    title = "Kitchen not found",
                    message = "This kitchen is no longer available.",
                    actionLabel = "Back to kitchens",
                    onAction = onBack,
                )
                KitchenDetailUiState.Error -> MessageContent(
                    title = "Something went wrong",
                    message = "We couldn't load this kitchen. Please try again.",
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                )
            }
        }
    }
}

@Composable
private fun SubscribeBar(isPaid: Boolean, onSubscribe: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 3.dp) {
        Button(
            onClick = onSubscribe,
            enabled = !isPaid,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            Text(if (isPaid) "Subscribed ✓" else "Subscribe")
        }
    }
}

@Composable
private fun KitchenDetailContent(kitchen: Kitchen) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // scroll position survives rotation and process death
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VegBadge(isVeg = kitchen.isVeg)
            Text("★ ${kitchen.rating}", style = MaterialTheme.typography.titleSmall)
            Text("₹${kitchen.pricePerTiffin} / tiffin", style = MaterialTheme.typography.titleSmall)
        }
        Text(
            text = "${kitchen.cuisine} · ${kitchen.distanceKm} km away",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(kitchen.description, style = MaterialTheme.typography.bodyLarge)

        Text("Weekly menu", style = MaterialTheme.typography.titleLarge)
        kitchen.weeklyMenu.forEach { day ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(day.day, style = MaterialTheme.typography.titleSmall)
                Text(day.items.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider()
        }
    }
}