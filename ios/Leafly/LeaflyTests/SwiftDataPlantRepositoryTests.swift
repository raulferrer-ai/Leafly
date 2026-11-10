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

    // MARK: - Helpers

    private func makeRepository() throws -> SwiftDataPlantRepository {
        let configuration = ModelConfiguration(
            "LeaflyTests-\(UUID().uuidString)",
            isStoredInMemoryOnly: true
        )
        let container = try ModelContainer(for: PlantEntity.self, configurations: configuration)
        return SwiftDataPlantRepository(container: container)
    }
}
