//
//  PlantsViewModel.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import Foundation

/// Estado y lógica de la lista de plantas.
///
/// Gestiona la carga de plantas, el filtrado por nombre y el filtrado por
/// estado de riego. El filtrado se aplica en memoria sobre las plantas
/// ya cargadas, sin tocar la persistencia.
@MainActor
@Observable
final class PlantsViewModel {
    private(set) var plants: [Plant] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?

    /// Texto de búsqueda actual. Vacío cuando no hay búsqueda activa.
    private(set) var searchText = ""
    /// Filtro de estado de riego activo. `nil` cuando no hay filtro.
    private(set) var selectedStatusFilter: WateringStatus?

    /// Plantas que cumplen tanto el texto de búsqueda como el filtro de estado.
    /// Se recalcula automáticamente cuando cambian `plants`, `searchText` o
    /// `selectedStatusFilter`.
    var filteredPlants: [Plant] {
        plants.filter { plant in
            matchesSearch(plant) && matchesStatusFilter(plant)
        }
    }

    /// `true` cuando hay búsqueda activa o filtro de estado seleccionado.
    var hasActiveFilters: Bool {
        !searchText.isEmpty || selectedStatusFilter != nil
    }

    private let repository: any PlantRepository
    private let reminders: any WateringReminderScheduling

    init(
        repository: any PlantRepository,
        reminders: any WateringReminderScheduling
    ) {
        self.repository = repository
        self.reminders = reminders
    }

    /// Carga las plantas guardadas. Es idempotente mientras hay una carga en curso.
    func load() async {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil

        do {
            let loadedPlants = try await repository.fetchPlants()
            plants = loadedPlants
            isLoading = false

            // Recalcula los recordatorios según el estado de riego actual.
            await reminders.sync(plants: loadedPlants)
        } catch {
            isLoading = false
            errorMessage = "No hemos podido cargar tus plantas. Inténtalo de nuevo."
        }
    }

    /// Refleja al instante una planta que cambió en el detalle y actualiza sus
    /// recordatorios. La lista se actualiza antes de esperar a las notificaciones.
    func replace(_ plant: Plant) async {
        guard let index = plants.firstIndex(where: { $0.id == plant.id }) else { return }
        plants[index] = plant

        await reminders.sync(plants: plants)
    }

    // MARK: - Búsqueda y filtros

    /// Actualiza el texto de búsqueda y lo aplica al filtrado.
    ///
    /// - Parameter text: Nuevo texto de búsqueda. `nil` o vacío limpia la búsqueda.
    func setSearchText(_ text: String?) {
        searchText = text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
    }

    /// Activa o desactiva el filtro por estado de riego.
    ///
    /// Si el estado ya está seleccionado, se desactiva el filtro (toggle).
    /// - Parameter status: Estado de riego a filtrar.
    func setStatusFilter(_ status: WateringStatus) {
        if selectedStatusFilter == status {
            selectedStatusFilter = nil
        } else {
            selectedStatusFilter = status
        }
    }

    /// Limpia la búsqueda y cualquier filtro de estado activo,
    /// restaurando la lista completa de plantas.
    func clearFilters() {
        searchText = ""
        selectedStatusFilter = nil
    }

    // MARK: - Filtros privados

    private func matchesSearch(_ plant: Plant) -> Bool {
        guard !searchText.isEmpty else { return true }
        return plant.name.localizedCaseInsensitiveContains(searchText)
    }

    private func matchesStatusFilter(_ plant: Plant) -> Bool {
        guard let status = selectedStatusFilter else { return true }
        return plant.wateringStatus() == status
    }
}
