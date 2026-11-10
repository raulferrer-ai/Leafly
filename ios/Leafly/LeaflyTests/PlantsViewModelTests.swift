//
//  PlantsViewModelTests.swift
//  LeaflyTests
//
//  Created by RaulF on 07/11/2026.
//

import XCTest
@testable import Leafly

@MainActor
final class PlantsViewModelTests: XCTestCase {

    func testLoadShowsPlantsFromRepository() async {
        let plants = [
            Plant(name: "Monstera", type: .monstera),
            Plant(name: "Ficus", type: .ficus)
        ]
        let repository = MockPlantRepository(plants: plants)
        let viewModel = PlantsViewModel(repository: repository)

        await viewModel.load()

        XCTAssertEqual(viewModel.plants, plants)
        XCTAssertNil(viewModel.errorMessage)
        XCTAssertFalse(viewModel.isLoading)
    }

    func testLoadWithoutPlantsLeavesEmptyListWithoutError() async {
        let viewModel = PlantsViewModel(repository: MockPlantRepository(plants: []))

        await viewModel.load()

        XCTAssertTrue(viewModel.plants.isEmpty)
        XCTAssertNil(viewModel.errorMessage)
        XCTAssertFalse(viewModel.isLoading)
    }

    func testLoadFailureShowsCalmMessage() async {
        let repository = MockPlantRepository(plants: [])
        repository.errorToThrow = MockError.fetchFailed
        let viewModel = PlantsViewModel(repository: repository)

        await viewModel.load()

        XCTAssertTrue(viewModel.plants.isEmpty)
        XCTAssertEqual(
            viewModel.errorMessage,
            "No hemos podido cargar tus plantas. Inténtalo de nuevo."
        )
        XCTAssertFalse(viewModel.isLoading)
    }

    func testReloadReplacesPlantsAndClearsPreviousError() async {
        let repository = MockPlantRepository(plants: [])
        repository.errorToThrow = MockError.fetchFailed
        let viewModel = PlantsViewModel(repository: repository)

        await viewModel.load()
        XCTAssertNotNil(viewModel.errorMessage)

        repository.errorToThrow = nil
        repository.plants = [Plant(name: "Orquídea", type: .orchid)]
        await viewModel.load()

        XCTAssertNil(viewModel.errorMessage)
        XCTAssertEqual(viewModel.plants.map(\.name), ["Orquídea"])
    }

    func testReplaceReflectsAPlantUpdatedInTheDetail() async {
        let plant = Plant(name: "Monstera", type: .monstera)
        let viewModel = PlantsViewModel(repository: MockPlantRepository(plants: [plant]))
        await viewModel.load()

        var wateredPlant = plant
        wateredPlant.lastWateredAt = .now
        viewModel.replace(wateredPlant)

        XCTAssertEqual(viewModel.plants.first?.lastWateredAt, wateredPlant.lastWateredAt)
        XCTAssertEqual(viewModel.plants.first?.name, "Monstera")
    }

    func testReplaceIgnoresUnknownPlants() async {
        let viewModel = PlantsViewModel(
            repository: MockPlantRepository(plants: [Plant(name: "Monstera")])
        )
        await viewModel.load()

        viewModel.replace(Plant(name: "Otra"))

        XCTAssertEqual(viewModel.plants.map(\.name), ["Monstera"])
    }
}

// MARK: - Dobles de prueba

@MainActor
private final class MockPlantRepository: PlantRepository {
    var plants: [Plant]
    var errorToThrow: Error?

    init(plants: [Plant]) {
        self.plants = plants
    }

    func fetchPlants() async throws -> [Plant] {
        if let errorToThrow {
            throw errorToThrow
        }
        return plants
    }

    func add(_ plant: Plant) async throws {
        plants.append(plant)
    }

    func update(_ plant: Plant) async throws {
        guard let index = plants.firstIndex(where: { $0.id == plant.id }) else {
            throw PlantRepositoryError.plantNotFound
        }
        plants[index] = plant
    }
}

private enum MockError: Error {
    case fetchFailed
}
