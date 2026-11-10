//
//  SwiftDataPlantRepositoryTests.swift
//  LeaflyTests
//
//  Created by RaulF on 06/11/2026.
//

import XCTest
import SwiftData
@testable import Leafly

@MainActor
final class SwiftDataPlantRepositoryTests: XCTestCase {

    func testFetchWithoutPlantsReturnsEmptyList() async throws {
        let repository = try makeRepository()

        let plants = try await repository.fetchPlants()

        XCTAssertTrue(plants.isEmpty)
    }

    func testAddPersistsPlantWithType() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "Monstera", type: .monstera)

        try await repository.add(plant)

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.count, 1)
        XCTAssertEqual(plants.first?.id, plant.id)
        XCTAssertEqual(plants.first?.name, "Monstera")
        XCTAssertEqual(plants.first?.type, .monstera)
    }

    func testAddPersistsPlantWithoutType() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "La de la abuela")

        try await repository.add(plant)

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.count, 1)
        XCTAssertEqual(plants.first?.name, "La de la abuela")
        XCTAssertNil(plants.first?.type)
    }

    func testPlantsKeepCreationOrder() async throws {
        let repository = try makeRepository()
        let firstPlant = Plant(name: "Primera", createdAt: Date(timeIntervalSince1970: 1_000))
        let secondPlant = Plant(name: "Segunda", createdAt: Date(timeIntervalSince1970: 2_000))

        try await repository.add(secondPlant)
        try await repository.add(firstPlant)

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.map(\.name), ["Primera", "Segunda"])
    }

    func testAddPersistsLastWateredDate() async throws {
        let repository = try makeRepository()
        let lastWateredAt = Date(timeIntervalSince1970: 1_800_000_000)
        let plant = Plant(name: "Regada", lastWateredAt: lastWateredAt)

        try await repository.add(plant)

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.first?.lastWateredAt, lastWateredAt)
    }

    func testAddWithoutWateringKeepsLastWateredDateEmpty() async throws {
        let repository = try makeRepository()

        try await repository.add(Plant(name: "Sin regar"))

        let plants = try await repository.fetchPlants()
        XCTAssertNil(plants.first?.lastWateredAt)
    }

    func testAddPersistsPhoto() async throws {
        let repository = try makeRepository()
        let photoData = Data([0xFF, 0xD8, 0xFF, 0xD9])
        let plant = Plant(name: "Con foto", photoData: photoData)

        try await repository.add(plant)

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.first?.photoData, photoData)
    }

    func testUpdateCanReplaceAndRemovePhoto() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "Con foto", photoData: Data([0x01]))
        try await repository.add(plant)

        var updated = plant
        updated.photoData = Data([0x02, 0x03])
        try await repository.update(updated)

        var plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.first?.photoData, Data([0x02, 0x03]))

        updated.photoData = nil
        try await repository.update(updated)

        plants = try await repository.fetchPlants()
        XCTAssertNil(plants.first?.photoData)
    }

    func testUpdatePersistsLastWateredDate() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "Monstera", type: .monstera)
        try await repository.add(plant)

        let wateredAt = Date(timeIntervalSince1970: 1_900_000_000)
        var wateredPlant = plant
        wateredPlant.lastWateredAt = wateredAt
        try await repository.update(wateredPlant)

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.count, 1)
        XCTAssertEqual(plants.first?.lastWateredAt, wateredAt)
        XCTAssertEqual(plants.first?.name, "Monstera")
        XCTAssertEqual(plants.first?.type, .monstera)
    }

    func testUpdateUnknownPlantThrows() async throws {
        let repository = try makeRepository()

        do {
            try await repository.update(Plant(name: "Fantasma"))
            XCTFail("Se esperaba un error al actualizar una planta inexistente")
        } catch {
            XCTAssertEqual(error as? PlantRepositoryError, .plantNotFound)
        }
    }

    // MARK: - Historial de fotos

    func testFetchPhotosWithoutHistoryReturnsEmpty() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "Sin fotos")
        try await repository.add(plant)

        let photos = try await repository.fetchPhotos(for: plant.id)

        XCTAssertTrue(photos.isEmpty)
    }

    func testAddPhotoPersistsItAndUpdatesTheMainPhoto() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "Monstera")
        try await repository.add(plant)

        let addedAt = Date(timeIntervalSince1970: 1_900_000_000)
        let photoData = Data([0xFF, 0xD8, 0xFF, 0xD9])
        let photo = PlantPhoto(
            plantID: plant.id,
            addedAt: addedAt,
            data: photoData
        )
        try await repository.addPhoto(photo)

        let photos = try await repository.fetchPhotos(for: plant.id)
        XCTAssertEqual(photos.count, 1)
        XCTAssertEqual(photos.first?.id, photo.id)
        XCTAssertEqual(photos.first?.addedAt, addedAt)
        XCTAssertEqual(photos.first?.data, photoData)

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.first?.photoData, photoData)
    }

    func testPhotosAreReturnedNewestFirst() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "Monstera")
        try await repository.add(plant)

        let older = PlantPhoto(
            plantID: plant.id,
            addedAt: Date(timeIntervalSince1970: 1_000),
            data: Data([0x01])
        )
        let newest = PlantPhoto(
            plantID: plant.id,
            addedAt: Date(timeIntervalSince1970: 2_000),
            data: Data([0x02])
        )

        try await repository.addPhoto(older)
        try await repository.addPhoto(newest)

        let photos = try await repository.fetchPhotos(for: plant.id)
        XCTAssertEqual(photos.map(\.id), [newest.id, older.id])
    }

    func testAddPhotoToUnknownPlantThrows() async throws {
        let repository = try makeRepository()
        let photo = PlantPhoto(
            plantID: UUID(),
            data: Data([0x01])
        )

        do {
            try await repository.addPhoto(photo)
            XCTFail("Se esperaba un error al añadir una foto a una planta inexistente")
        } catch {
            XCTAssertEqual(error as? PlantRepositoryError, .plantNotFound)
        }
    }

    func testUpdatingAPlantKeepsItsPhotoHistory() async throws {
        let repository = try makeRepository()
        let plant = Plant(name: "Monstera")
        try await repository.add(plant)

        let photo = PlantPhoto(
            plantID: plant.id,
            addedAt: Date(timeIntervalSince1970: 1_500),
            data: Data([0x0A])
        )
        try await repository.addPhoto(photo)

        var watered = plant
        watered.photoData = photo.data
        watered.lastWateredAt = Date(timeIntervalSince1970: 2_500)
        try await repository.update(watered)

        let photos = try await repository.fetchPhotos(for: plant.id)
        XCTAssertEqual(photos.map(\.id), [photo.id])

        let plants = try await repository.fetchPlants()
        XCTAssertEqual(plants.first?.photoData, photo.data)
    }

    // MARK: - Helpers

    private func makeRepository() throws -> SwiftDataPlantRepository {
        let configuration = ModelConfiguration(
            "LeaflyTests-\(UUID().uuidString)",
            isStoredInMemoryOnly: true
        )
        let container = try ModelContainer(
            for: PlantEntity.self,
            PlantPhotoEntity.self,
            configurations: configuration
        )
        return SwiftDataPlantRepository(container: container)
    }
}
