package com.example.tiffin.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiffin.data.Kitchen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenListScreen(
    onKitchenClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KitchenListViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Tiffin") }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (val state = uiState) {
                KitchenListUiState.Loading -> LoadingContent()
                is KitchenListUiState.Success -> KitchenList(state.kitchens, onKitchenClick)
                KitchenListUiState.Empty -> MessageContent(
                    title = "No kitchens yet",
                    message = "There are no home kitchens to show right now.",
                    actionLabel = "Refresh",
                    onAction = viewModel::retry,
                )
                KitchenListUiState.Error -> MessageContent(
                    title = "Something went wrong",
                    message = "We couldn't load the kitchens. Please try again.",
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageContent(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.size(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.size(24.dp))
        Button(onClick = onAction) { Text(actionLabel) }
    }
}

@Composable
private fun KitchenList(kitchens: List<Kitchen>, onKitchenClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(kitchens, key = { it.id }) { kitchen ->
            KitchenCard(kitchen = kitchen, onClick = { onKitchenClick(kitchen.id) })
        }
    }
}

@Composable
private fun KitchenCard(kitchen: Kitchen, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = kitchen.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text("★ ${kitchen.rating}", style = MaterialTheme.typography.titleSmall)
            }
            Text(
                text = "${kitchen.cuisine} · ${kitchen.distanceKm} km away",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VegBadge(isVeg = kitchen.isVeg)
                Text("₹${kitchen.pricePerTiffin} / tiffin", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun VegBadge(isVeg: Boolean) {
    // Colour plus a text label, so the meaning never depends on colour alone.
    val dotColor = if (isVeg) Color(0xFF2E7D32) else Color(0xFFC62828)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dotColor, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(if (isVeg) "Veg" else "Non-veg", style = MaterialTheme.typography.labelLarge)
    }
}