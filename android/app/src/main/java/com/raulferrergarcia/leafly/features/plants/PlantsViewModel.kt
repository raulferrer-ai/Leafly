package com.raulferrergarcia.leafly.features.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Estado y lógica de la lista de plantas.
 *
 * Room es la fuente de verdad: cada cambio se refleja automáticamente. El
 * estado solo añade carga y error para que la pantalla pueda ser calmada
 * incluso cuando algo va mal.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlantsViewModel(
    observePlants: ObservePlantsUseCase,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<PlantsUiState> = retryTrigger
        .flatMapLatest {
            observePlants()
                .map<List<Plant>, PlantsUiState> { plants ->
                    PlantsUiState(plants = plants, isLoading = false)
                }
                .catch {
                    emit(
                        PlantsUiState(
                            isLoading = false,
                            loadError = PlantsLoadError.LoadFailed,
                        ),
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = PlantsUiState(),
        )

    /** Vuelve a escuchar las plantas tras un error. */
    fun retry() {
        retryTrigger.update { it + 1 }
    }

    companion object {
        fun factory(observePlants: ObservePlantsUseCase) = viewModelFactory {
            initializer { PlantsViewModel(observePlants) }
        }
    }
}
