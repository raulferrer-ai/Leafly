package com.raulferrergarcia.leafly.features.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.data.repository.NotificationPreferencesRepository
import com.raulferrergarcia.leafly.domain.model.NotificationPreferences
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import com.raulferrergarcia.leafly.domain.model.wateringStatus
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.SyncWateringRemindersUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado y lógica de la lista de plantas.
 *
 * Room es la fuente de verdad: cada cambio se refleja automáticamente. El
 * estado solo añade carga y error para que la pantalla pueda ser calmada
 * incluso cuando algo va mal.
 *
 * Además gestiona el filtrado por nombre y por estado de riego, aplicado en
 * memoria sobre las plantas ya cargadas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlantsViewModel(
    private val plantRepository: PlantRepository,
    private val syncWateringReminders: SyncWateringRemindersUseCase,
    private val preferencesRepository: NotificationPreferencesRepository? = null,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    // Filter state
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    fun setSearchText(text: String?) {
        _filterState.update { it.copy(searchText = text?.trim() ?: "") }
    }

    fun setStatusFilter(status: WateringStatus) {
        _filterState.update { state ->
            state.copy(selectedStatusFilter = if (state.selectedStatusFilter == status) null else status)
        }
    }

    fun clearFilters() {
        _filterState.update { state -> state.copy(searchText = "", selectedStatusFilter = null) }
    }

    private fun observePlantsUi(): Flow<PlantsUiState> = flow {
        try {
            plantRepository.observePlants()
                .onEach { plants ->
                    val preferences = preferencesRepository?.get() ?: NotificationPreferences.Default
                    syncRemindersSafely(plants, preferences)
                }
                .collect { plants ->
                    emit(
                        PlantsUiState(
                            plants = plants.filter { p ->
                                matchesSearch(p, filterState.value.searchText) &&
                                    matchesStatusFilter(p, filterState.value.selectedStatusFilter)
                            },
                            isLoading = false,
                            searchText = filterState.value.searchText,
                            selectedStatusFilter = filterState.value.selectedStatusFilter,
                        ),
                    )
                }
        } catch (_: Exception) {
            emit(
                PlantsUiState(
                    isLoading = false,
                    loadError = PlantsLoadError.LoadFailed,
                    searchText = filterState.value.searchText,
                    selectedStatusFilter = filterState.value.selectedStatusFilter,
                ),
            )
        }
    }

    val uiState: StateFlow<PlantsUiState> = retryTrigger
        .flatMapLatest { _ -> observePlantsUi() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PlantsUiState(),
        )

    /** Vuelve a escuchar las plantas tras un error. */
    fun retry() {
        retryTrigger.update { it + 1 }
    }

    /**
     * Los recordatorios son un extra: si fallan, la lista debe seguir
     * mostrándose con normalidad.
     */
    private suspend fun syncRemindersSafely(
        plants: List<Plant>,
        preferences: NotificationPreferences,
    ) {
        try {
            syncWateringReminders(plants, preferences)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Sin recordatorios, la app sigue funcionando.
        }
    }

    /** Cambia la hora preferida de los recordatorios y los reprograma. */
    fun setPreferredTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val currentPlants = uiState.value.plants
            val preferences = NotificationPreferences(hour = hour, minute = minute)
            preferencesRepository?.save(preferences)
            syncRemindersSafely(currentPlants, preferences)
        }
    }

    companion object {
        fun factory(
            plantRepository: PlantRepository,
            syncWateringReminders: SyncWateringRemindersUseCase,
            preferencesRepository: NotificationPreferencesRepository,
        ) = viewModelFactory {
            initializer {
                PlantsViewModel(
                    plantRepository = plantRepository,
                    syncWateringReminders = syncWateringReminders,
                    preferencesRepository = preferencesRepository,
                )
            }
        }
    }

    private fun matchesSearch(plant: Plant, searchText: String): Boolean {
        if (searchText.isEmpty()) return true
        return plant.name.lowercase().contains(searchText.lowercase())
    }

    private fun matchesStatusFilter(plant: Plant, statusFilter: WateringStatus?): Boolean {
        if (statusFilter == null) return true
        return plant.wateringStatus() == statusFilter
    }
}
