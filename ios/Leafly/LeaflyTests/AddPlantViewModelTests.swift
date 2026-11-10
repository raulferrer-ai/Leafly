//
//  AddPlantViewModelTests.swift
//  LeaflyTests
//
//  Created by RaulF on 06/11/2026.
//

import XCTest
@testable import Leafly

@MainActor
final class AddPlantViewModelTests: XCTestCase {

    func testSaveWithoutNameShowsErrorAndDoesNotPersist() async {
        let repository = MockPlantRepository()
        let viewModel = AddPlantViewModel(repository: repository)

        let didSave = await viewModel.save()

        XCTAssertFalse(didSave)
        XCTAssertEqual(
            viewModel.nameError,
            "Ponle un nombre a tu planta para poder guardarla."
        )
        XCTAssertTrue(repository.savedPlants.isEmpty)
    }

    func testSaveWithOnlyWhitespaceDoesNotPersist() async {
        let repository = MockPlantRepository()
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.name = "   \n  "

        let didSave = await viewModel.save()

        XCTAssertFalse(didSave)
        XCTAssertNotNil(viewModel.nameError)
        XCTAssertTrue(repository.savedPlants.isEmpty)
    }

    func testSaveTrimsNameAndKeepsSelectedType() async {
        let repository = MockPlantRepository()
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.name = "  Mi monstera  "
        viewModel.selectedType = .monstera

        let didSave = await viewModel.save()

        XCTAssertTrue(didSave)
        XCTAssertNil(viewModel.nameError)
        XCTAssertEqual(repository.savedPlants.count, 1)
        XCTAssertEqual(repository.savedPlants.first?.name, "Mi monstera")
        XCTAssertEqual(repository.savedPlants.first?.type, .monstera)
    }

    func testSaveWithoutSelectedTypeIsAllowed() async {
        let repository = MockPlantRepository()
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.name = "Planta nueva"

        let didSave = await viewModel.save()

        XCTAssertTrue(didSave)
        XCTAssertEqual(repository.savedPlants.first?.type, nil)
    }

    func testSelectingSameTypeTwiceDeselectsIt() async {
        let viewModel = AddPlantViewModel(repository: MockPlantRepository())

        viewModel.toggleType(.ficus)
        XCTAssertEqual(viewModel.selectedType, .ficus)

        viewModel.toggleType(.ficus)
        XCTAssertNil(viewModel.selectedType)

        await Task.yield()
    }

    func testTypingClearsNameError() async {
        let viewModel = AddPlantViewModel(repository: MockPlantRepository())

        await viewModel.save()
        XCTAssertNotNil(viewModel.nameError)

        viewModel.name = "Orquídea"
        viewModel.clearNameError()

        XCTAssertNil(viewModel.nameError)
    }

    func testRepositoryFailureShowsCalmMessage() async {
        let repository = MockPlantRepository()
        repository.errorToThrow = MockError.saveFailed
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.name = "Ficus"

        let didSave = await viewModel.save()

        XCTAssertFalse(didSave)
        XCTAssertEqual(
            viewModel.saveError,
            "No hemos podido guardar tu planta. Inténtalo de nuevo."
        )
        XCTAssertFalse(viewModel.isSaving)
        XCTAssertTrue(repository.savedPlants.isEmpty)
    }
}

// MARK: - Dobles de prueba

@MainActor
private final class MockPlantRepository: PlantRepository {
    private(set) var savedPlants: [Plant] = []
    var errorToThrow: Error?

    func fetchPlants() async throws -> [Plant] {
        savedPlants
    }

    func add(_ plant: Plant) async throws {
        if let errorToThrow {
            throw errorToThrow
        }
        savedPlants.append(plant)
    }

    func update(_ plant: Plant) async throws {
        if let errorToThrow {
            throw errorToThrow
        }

        guard let index = savedPlants.firstIndex(where: { $0.id == plant.id }) else {
            throw PlantRepositoryError.plantNotFound
        }
        savedPlants[index] = plant
    }
}

private enum MockError: Error {
    case saveFailed
}
