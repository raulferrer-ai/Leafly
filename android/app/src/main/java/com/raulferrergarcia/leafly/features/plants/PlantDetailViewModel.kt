package com.raulferrergarcia.leafly.features.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.designsystem.LeaflyCopy
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.repository.PlantNotFoundException
import com.raulferrergarcia.leafly.domain.usecase.CancelWateringReminderUseCase
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado y lógica del detalle de una planta.
 *
 * La pantalla no calcula nada: aquí vive el guardado del riego y el historial
 * de fotos. La lista se entera por Room, así que al volver ya muestra el
 * estado actualizado.
 */
class PlantDetailViewModel(
    private val plant: Plant,
    private val repository: PlantRepository,
    private val markPlantWatered: MarkPlantWateredUseCase,
    private val cancelWateringReminder: CancelWateringReminderUseCase,
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

                // Ya está al día: su recordatorio pendiente deja de tener sentido.
                cancelWateringReminder(wateredPlant.id)

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

    // ------------------------------------------------------------------
    // Historial de fotos
    // ------------------------------------------------------------------

    /** Carga el historial de fotos de la planta (más reciente primero). */
    fun loadPhotoHistory() {
        _uiState.update { state ->
            state.copy(isLoadingPhotos = true, photoErrorMessage = null)
        }

        viewModelScope.launch {
            try {
                val photos = repository.observePhotos(plant.id).first()
                _uiState.update { state ->
                    state.copy(photos = photos, isLoadingPhotos = false)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoadingPhotos = false,
                        photoErrorMessage = LeaflyCopy.Error.PhotoHistory.message,
                    )
                }
            }
        }
    }

    /**
     * Añade una foto ya procesada al historial y la convierte en la principal.
     *
     * - Returns: `true` si la foto se guardó correctamente.
     */
    fun addPhoto(data: ByteArray) {
        val current = _uiState.value
        if (current.isAddingPhoto) return

        _uiState.update { state ->
            state.copy(isAddingPhoto = true, photoErrorMessage = null)
        }

        viewModelScope.launch {
            try {
                val photo = PlantPhoto(plantId = plant.id, data = data)
                repository.addPhoto(photo)

                // La foto recién añadida es la más reciente.
                _uiState.update { state ->
                    state.copy(
                        photos = listOf(photo) + state.photos,
                        plant = state.plant.copy(photoData = data),
                        isAddingPhoto = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isAddingPhoto = false,
                        photoErrorMessage = LeaflyCopy.Error.AddPhoto.message,
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            plant: Plant,
            repository: PlantRepository,
            markPlantWatered: MarkPlantWateredUseCase,
            cancelWateringReminder: CancelWateringReminderUseCase,
        ) = viewModelFactory {
            initializer {
                PlantDetailViewModel(
                    plant = plant,
                    repository = repository,
                    markPlantWatered = markPlantWatered,
                    cancelWateringReminder = cancelWateringReminder,
                )
            }
        }
    }
}
