//
//  SwiftDataPlantRepository.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation
import SwiftData

/// Implementación de `PlantRepository` sobre SwiftData.
@MainActor
final class SwiftDataPlantRepository: PlantRepository {
    /// El contexto no mantiene vivo el contenedor por sí solo; lo retenemos aquí.
    private let container: ModelContainer
    private let context: ModelContext

    init(container: ModelContainer) {
        self.container = container
        self.context = container.mainContext
    }

    func fetchPlants() async throws -> [Plant] {
        let descriptor = FetchDescriptor<PlantEntity>(
            sortBy: [SortDescriptor(\.createdAt, order: .forward)]
        )
        return try context.fetch(descriptor).map(\.domainModel)
    }

    func add(_ plant: Plant) async throws {
        context.insert(PlantEntity(plant: plant))
        try context.save()
    }
}
