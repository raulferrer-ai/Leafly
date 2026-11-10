package com.raulferrergarcia.leafly.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado y lógica de la pantalla de añadir planta.
 */
class AddPlantViewModel(
    private val addPlant: AddPlantUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPlantUiState())
    val uiState: StateFlow<AddPlantUiState> = _uiState.asStateFlow()

    /**
     * El error desaparece en cuanto la persona empieza a escribir.
     */
    fun onNameChange(name: String) {
        _uiState.update { state -> state.copy(name = name, nameError = null) }
    }

    /**
     * El tipo es opcional: tocarlo de nuevo lo deselecciona.
     */
    fun toggleType(type: PlantType) {
        _uiState.update { state ->
            state.copy(selectedType = if (state.selectedType == type) null else type)
        }
    }

    fun clearType() {
        _uiState.update { state -> state.copy(selectedType = null) }
    }

    /**
     * Deja la pantalla como recién abierta. Se usa al entrar y al cancelar,
     * para que cancelar no conserve ni guarde nada.
     */
    fun reset() {
        _uiState.value = AddPlantUiState()
    }

    fun save() {
        val current = _uiState.value
        if (current.isSaving) return

        val trimmedName = current.name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { state -> state.copy(nameError = AddPlantError.BlankName) }
            return
        }

        // Marcamos "guardando" antes de lanzar para que un doble toque no duplique.
        _uiState.update { state -> state.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                addPlant(name = trimmedName, type = current.selectedType)
                _uiState.update { state -> state.copy(isSaving = false, isSaved = true) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { state ->
                    state.copy(isSaving = false, saveError = AddPlantError.SaveFailed)
                }
            }
        }
    }

    companion object {
        fun factory(addPlant: AddPlantUseCase) = viewModelFactory {
            initializer { AddPlantViewModel(addPlant) }
        }
    }
}
