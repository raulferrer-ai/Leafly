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

    init(repository: any PlantRepository) {
        self.repository = repository
    }

    /// Carga las plantas guardadas. Es idempotente mientras hay una carga en curso.
    func load() async {
        guard !isLoading else { return }

        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        do {
            plants = try await repository.fetchPlants()
        } catch {
            errorMessage = "No hemos podido cargar tus plantas. Inténtalo de nuevo."
        }
    }

    /// Refleja al instante una planta que cambió en el detalle, sin recargar.
    func replace(_ plant: Plant) {
        guard let index = plants.firstIndex(where: { $0.id == plant.id }) else { return }
        plants[index] = plant
    }
}
