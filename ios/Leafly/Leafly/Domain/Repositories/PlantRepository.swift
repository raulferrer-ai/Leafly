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
}
