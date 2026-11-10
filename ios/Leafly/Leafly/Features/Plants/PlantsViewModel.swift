//
//  PlantsViewModel.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import Foundation

/// Estado y lógica de la lista de plantas.
@MainActor
@Observable
final class PlantsViewModel {
    private(set) var plants: [Plant] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?

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
}
