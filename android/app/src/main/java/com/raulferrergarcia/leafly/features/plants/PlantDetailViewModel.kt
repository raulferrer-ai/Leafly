package com.raulferrergarcia.leafly.features.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado y lógica del detalle de una planta.
 *
 * La pantalla no calcula nada: aquí vive el guardado del riego. La lista se
 * entera por Room, así que al volver ya muestra el estado actualizado.
 */
class PlantDetailViewModel(
    plant: Plant,
    private val markPlantWatered: MarkPlantWateredUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantDetailUiState(plant = plant))
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    /** Marca la planta como regada ahora y persiste el cambio. */
    fun markAsWatered() {
        val current = _uiState.value
        if (current.isSaving) return

        // Marcamos "guardando" antes de lanzar para que un doble toque no repita.
        _uiState.update { state -> state.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                val wateredPlant = markPlantWatered(current.plant)
                _uiState.update { state ->
                    state.copy(plant = wateredPlant, isSaving = false)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { state ->
                    state.copy(isSaving = false, saveError = PlantDetailSaveError.SaveFailed)
                }
            }
        }
    }

    companion object {
        fun factory(
            plant: Plant,
            markPlantWatered: MarkPlantWateredUseCase,
        ) = viewModelFactory {
            initializer { PlantDetailViewModel(plant, markPlantWatered) }
        }
    }
}
