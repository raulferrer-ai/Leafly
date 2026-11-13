package com.raulferrergarcia.leafly.features.plants

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import com.raulferrergarcia.leafly.domain.model.wateringStatus

/** Errores que la lista de plantas puede mostrar. */
enum class PlantsLoadError {
    LoadFailed,
}

/**
 * Estado inmutable de la lista de plantas.
 */
data class PlantsUiState(
    val plants: List<Plant> = emptyList(),
    val isLoading: Boolean = true,
    val loadError: PlantsLoadError? = null,
    /** Texto de búsqueda actual (vacío cuando no hay búsqueda activa). */
    val searchText: String = "",
    /** Filtro de estado de riego activo (null cuando no hay filtro). */
    val selectedStatusFilter: WateringStatus? = null,
) {
    /** Plantas que cumplen tanto el texto de búsqueda como el filtro de estado. */
    val filteredPlants: List<Plant>
        get() = plants.filter { matchesSearch(it) && matchesStatusFilter(it) }

    /** `true` cuando hay búsqueda activa o filtro de estado seleccionado. */
    val hasActiveFilters: Boolean
        get() = searchText.isNotEmpty() || selectedStatusFilter != null

    private fun matchesSearch(plant: Plant): Boolean {
        if (searchText.isEmpty()) return true
        return plant.name.lowercase().contains(searchText.lowercase())
    }

    private fun matchesStatusFilter(plant: Plant): Boolean {
        if (selectedStatusFilter == null) return true
        return plant.wateringStatus() == selectedStatusFilter
    }
}

/** Filtro de historial aplicado a la lista de plantas. */
data class FilterState(
    val searchText: String = "",
    val selectedStatusFilter: WateringStatus? = null,
)