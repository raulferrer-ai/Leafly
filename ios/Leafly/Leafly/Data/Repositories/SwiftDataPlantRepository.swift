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

    func update(_ plant: Plant) async throws {
        let plantID = plant.id
        let descriptor = FetchDescriptor<PlantEntity>(
            predicate: #Predicate { $0.id == plantID }
        )

        guard let entity = try context.fetch(descriptor).first else {
            throw PlantRepositoryError.plantNotFound
        }

        entity.name = plant.name
        entity.typeRawValue = plant.type?.rawValue
        entity.createdAt = plant.createdAt
        entity.lastWateredAt = plant.lastWateredAt
        entity.photoData = plant.photoData

        try context.save()
    }

    func fetchPhotos(for plantID: UUID) async throws -> [PlantPhoto] {
        let descriptor = FetchDescriptor<PlantEntity>(
            predicate: #Predicate { $0.id == plantID }
        )

        guard let plant = try context.fetch(descriptor).first else {
            return []
        }

        return plant.photos
            .compactMap(\.domainModel)
            .sorted { $0.addedAt > $1.addedAt }
    }

    func addPhoto(_ photo: PlantPhoto) async throws {
        let plantID = photo.plantID
        let descriptor = FetchDescriptor<PlantEntity>(
            predicate: #Predicate { $0.id == plantID }
        )

        guard let plant = try context.fetch(descriptor).first else {
            throw PlantRepositoryError.plantNotFound
        }

        let entity = PlantPhotoEntity(
            id: photo.id,
            addedAt: photo.addedAt,
            photoData: photo.data,
            plant: plant
        )
        context.insert(entity)

        // La foto más reciente pasa a ser la imagen principal de la planta.
        plant.photoData = photo.data

        try context.save()
    }
}
