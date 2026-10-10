package com.example.tiffin.ui.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiffin.data.Kitchen
import com.example.tiffin.data.KitchenAssetLoader
import com.example.tiffin.data.KitchenDataException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface KitchenListUiState {
    data object Loading : KitchenListUiState
    data class Success(val kitchens: List<Kitchen>) : KitchenListUiState
    data object Empty : KitchenListUiState
    data object Error : KitchenListUiState
}

class KitchenListViewModel(application: Application) : AndroidViewModel(application) {

    private val loader = KitchenAssetLoader(application)

    private val _uiState = MutableStateFlow<KitchenListUiState>(KitchenListUiState.Loading)
    val uiState: StateFlow<KitchenListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel() // a second tap on retry replaces the first load
        _uiState.value = KitchenListUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = try {
                val kitchens = loader.load()
                if (kitchens.isEmpty()) KitchenListUiState.Empty
                else KitchenListUiState.Success(kitchens)
            } catch (e: KitchenDataException) {
                Log.e("TiffinData", "Could not load kitchens", e)
                KitchenListUiState.Error
            }
        }
    }
}