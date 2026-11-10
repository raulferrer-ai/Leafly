//
//  PlantRepository.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation

/// Acceso a las plantas guardadas localmente.
/// El dominio no conoce SwiftData: solo este contrato.
@MainActor
protocol PlantRepository {
    func fetchPlants() async throws -> [Plant]
    func add(_ plant: Plant) async throws
    /// Guarda los cambios de una planta que ya existía (por ejemplo, un riego).
    func update(_ plant: Plant) async throws
}

/// Errores esperables del repositorio de plantas.
nonisolated enum PlantRepositoryError: Error, Equatable {
    /// La planta que se quería actualizar ya no existe.
    case plantNotFound
}
