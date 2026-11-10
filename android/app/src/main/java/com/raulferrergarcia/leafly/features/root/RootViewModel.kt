package com.raulferrergarcia.leafly.features.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Estado de arranque de la app.
 *
 * Mientras Room carga mostramos un fondo calmado; después decidimos entre
 * onboarding y jardín. No hay parpadeo de pantallas.
 */
sealed interface RootUiState {
    data object Loading : RootUiState
    data object Onboarding : RootUiState
    data class Plants(val plants: List<Plant>) : RootUiState
}

/**
 * Decide qué mostrar: sin plantas → onboarding; con plantas → jardín.
 */
class RootViewModel(
    observePlants: ObservePlantsUseCase,
) : ViewModel() {

    val uiState: StateFlow<RootUiState> = observePlants()
        .map<List<Plant>, RootUiState> { plants ->
            if (plants.isEmpty()) RootUiState.Onboarding else RootUiState.Plants(plants)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = RootUiState.Loading,
        )

    companion object {
        fun factory(observePlants: ObservePlantsUseCase) = viewModelFactory {
            initializer { RootViewModel(observePlants) }
        }
    }
}
