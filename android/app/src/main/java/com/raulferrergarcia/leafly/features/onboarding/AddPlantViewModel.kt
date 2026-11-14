package com.raulferrergarcia.leafly.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.EditPlantUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado y lógica de la pantalla de añadir o editar planta.
 */
class AddPlantViewModel(
    private val addPlant: AddPlantUseCase,
    private val editPlant: EditPlantUseCase? = null,
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

    /** La foto se está preparando (reducir, orientar y comprimir). */
    fun startPhotoProcessing() {
        _uiState.update { state ->
            state.copy(isProcessingPhoto = true, photoMessage = null)
        }
    }

    /** Guarda en memoria la foto ya procesada. Solo se persiste al guardar. */
    fun applyPhoto(photoData: ByteArray) {
        _uiState.update { state ->
            state.copy(
                photoData = photoData,
                isProcessingPhoto = false,
                photoMessage = null,
            )
        }
    }

    /** La foto no se pudo preparar: se avisa con calma y se sigue sin ella. */
    fun failPhotoProcessing(message: AddPlantPhotoMessage) {
        _uiState.update { state ->
            state.copy(isProcessingPhoto = false, photoMessage = message)
        }
    }

    /** Aviso calmado (por ejemplo, permiso de cámara denegado). */
    fun showPhotoMessage(message: AddPlantPhotoMessage) {
        _uiState.update { state -> state.copy(photoMessage = message) }
    }

    /** Quita la foto elegida; la planta se guardará sin imagen. */
    fun removePhoto() {
        _uiState.update { state ->
            state.copy(photoData = null, photoMessage = null)
        }
    }

    /**
     * Precarga los datos de una planta existente para editarla.
     * Se llama al abrir la pantalla en modo edición.
     */
    fun load(plant: Plant) {
        _uiState.update { state ->
            state.copy(
                name = plant.name,
                selectedType = plant.type,
                photoData = plant.photoData,
                editingPlant = plant,
            )
        }
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
        if (current.isSaving || current.isProcessingPhoto) return

        val trimmedName = current.name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { state -> state.copy(nameError = AddPlantError.BlankName) }
            return
        }

        // Marcamos "guardando" antes de lanzar para que un doble toque no duplique.
        _uiState.update { state -> state.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                if (current.editingPlant != null) {
                    val editing = current.editingPlant!!
                    editPlant?.invoke(
                        plantId = editing.id,
                        name = trimmedName,
                        type = current.selectedType,
                        photoData = current.photoData,
                        createdAt = editing.createdAt,
                        lastWateredAt = editing.lastWateredAt,
                    )
                } else {
                    addPlant(
                        name = trimmedName,
                        type = current.selectedType,
                        photoData = current.photoData,
                    )
                }
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
        fun factory(
            addPlant: AddPlantUseCase,
            editPlant: EditPlantUseCase? = null,
        ) = viewModelFactory {
            initializer { AddPlantViewModel(addPlant, editPlant) }
        }
    }
}
