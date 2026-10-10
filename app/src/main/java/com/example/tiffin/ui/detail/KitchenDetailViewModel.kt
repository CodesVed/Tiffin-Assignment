package com.example.tiffin.ui.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tiffin.data.Kitchen
import com.example.tiffin.data.KitchenAssetLoader
import com.example.tiffin.data.KitchenDataException
import com.example.tiffin.ui.KitchenDetailRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface KitchenDetailUiState {
    data object Loading : KitchenDetailUiState
    data class Success(val kitchen: Kitchen) : KitchenDetailUiState
    data object NotFound : KitchenDetailUiState
    data object Error : KitchenDetailUiState
}

class KitchenDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    // The only navigation argument: restored automatically after process death.
    private val kitchenId: String = savedStateHandle.toRoute<KitchenDetailRoute>().kitchenId

    private val loader = KitchenAssetLoader(application)

    private val _uiState = MutableStateFlow<KitchenDetailUiState>(KitchenDetailUiState.Loading)
    val uiState: StateFlow<KitchenDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        _uiState.value = KitchenDetailUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = try {
                val kitchen = loader.load().firstOrNull { it.id == kitchenId }
                if (kitchen == null) KitchenDetailUiState.NotFound
                else KitchenDetailUiState.Success(kitchen)
            } catch (e: KitchenDataException) {
                Log.e("TiffinData", "Could not load kitchen $kitchenId", e)
                KitchenDetailUiState.Error
            }
        }
    }
}