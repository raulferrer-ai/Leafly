package com.raulferrergarcia.leafly.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulferrergarcia.leafly.data.repository.NotificationPreferencesRepository
import com.raulferrergarcia.leafly.domain.model.NotificationPreferences
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.usecase.SyncWateringRemindersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado y lógica de la pantalla de ajustes de notificaciones.
 *
 * Muestra el estado del permiso de notificaciones y permite elegir la hora
 * preferida de los recordatorios de riego.
 *
 * No solicita permiso de forma agresiva: solo consulta el estado actual.
 */
class SettingsViewModel(
    private val preferencesRepository: NotificationPreferencesRepository,
    private val syncWateringReminders: SyncWateringRemindersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /** Carga el estado actual: permiso y preferencias guardadas. */
    fun load(isPermissionGranted: Boolean) {
        val preferences = preferencesRepository.get()

        _uiState.update {
            it.copy(
                isPermissionGranted = isPermissionGranted,
                preferredHour = preferences.hour,
                preferredMinute = preferences.minute,
            )
        }
    }

    /** Cambia la hora preferida, la persiste y reprograma los recordatorios. */
    fun setPreferredTime(hour: Int, minute: Int, plants: List<Plant>) {
        val newPrefs = NotificationPreferences(hour = hour, minute = minute)
        preferencesRepository.save(newPrefs)

        _uiState.update {
            it.copy(preferredHour = hour, preferredMinute = minute)
        }

        viewModelScope.launch {
            syncWateringReminders(plants, newPrefs)
        }
    }
}

/** Estado inmutable de la pantalla de ajustes. */
data class SettingsUiState(
    val isPermissionGranted: Boolean = false,
    val preferredHour: Int = 10,
    val preferredMinute: Int = 0,
)
