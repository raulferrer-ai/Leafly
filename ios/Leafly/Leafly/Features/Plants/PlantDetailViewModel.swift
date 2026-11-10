//
//  PlantDetailViewModel.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import Foundation

/// Estado y lógica de la pantalla de detalle.
///
/// La vista no calcula nada: el ViewModel expone el estado de riego y el
/// resumen ya resueltos, y se encarga de persistir el riego.
@MainActor
@Observable
final class PlantDetailViewModel {

    /// Planta que se está mostrando. Se actualiza al marcar el riego.
    private(set) var plant: Plant

    /// Evita dobles pulsaciones mientras se guarda el riego.
    private(set) var isSaving = false

    /// Mensaje calmado cuando el riego no se pudo guardar.
    private(set) var errorMessage: String?

    private let repository: any PlantRepository
    private let now: () -> Date

    init(
        plant: Plant,
        repository: any PlantRepository,
        now: @escaping () -> Date = Date.init
    ) {
        self.plant = plant
        self.repository = repository
        self.now = now
    }

    /// Estado de riego actual de la planta.
    var status: WateringStatus {
        plant.wateringStatus(on: now())
    }

    /// Texto cercano con la última vez que se regó (o cuándo se añadió).
    var summary: String {
        plant.wateringSummary(on: now())
    }

    /// Marca la planta como regada ahora mismo y lo persiste.
    ///
    /// - Returns: `true` si el riego se guardó correctamente.
    @discardableResult
    func markAsWatered() async -> Bool {
        guard !isSaving else { return false }

        isSaving = true
        errorMessage = nil
        defer { isSaving = false }

        let wateredPlant = Plant(
            id: plant.id,
            name: plant.name,
            type: plant.type,
            createdAt: plant.createdAt,
            lastWateredAt: now()
        )

        do {
            try await repository.update(wateredPlant)
            plant = wateredPlant
            return true
        } catch {
            errorMessage = "No hemos podido guardar el riego. Inténtalo de nuevo."
            return false
        }
    }
}
