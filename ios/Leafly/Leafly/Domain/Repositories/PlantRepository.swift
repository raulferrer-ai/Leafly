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
    /// Devuelve el historial de fotos de una planta, de más reciente a más antigua.
    func fetchPhotos(for plantID: UUID) async throws -> [PlantPhoto]
    /// Añade una foto al historial y la deja como foto principal de la planta.
    func addPhoto(_ photo: PlantPhoto) async throws
}

/// Errores esperables del repositorio de plantas.
nonisolated enum PlantRepositoryError: Error, Equatable {
    /// La planta que se quería actualizar ya no existe.
    case plantNotFound
}
