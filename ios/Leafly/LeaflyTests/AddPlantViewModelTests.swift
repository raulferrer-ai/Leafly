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
        XCTAssertNil(repository.savedPlants.first?.photoData)
    }

    func testSaveKeepsSelectedPhoto() async {
        let repository = MockPlantRepository()
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.name = "Monstera"
        let photoData = Data([0x01, 0x02, 0x03, 0x04])
        viewModel.setPhotoData(photoData)

        let didSave = await viewModel.save()

        XCTAssertTrue(didSave)
        XCTAssertEqual(repository.savedPlants.first?.photoData, photoData)
    }

    func testSelectingPhotoDoesNotPersistUntilSave() async {
        let repository = MockPlantRepository()
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.name = "Ficus"

        viewModel.setPhotoData(Data([0x0A, 0x0B]))

        XCTAssertTrue(repository.savedPlants.isEmpty)
        XCTAssertEqual(viewModel.photoData, Data([0x0A, 0x0B]))
    }

    func testRemovingPhotoSavesPlantWithoutImage() async {
        let repository = MockPlantRepository()
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.name = "Potos"
        viewModel.setPhotoData(Data([0x0C]))

        viewModel.removePhoto()
        let didSave = await viewModel.save()

        XCTAssertTrue(didSave)
        XCTAssertNil(viewModel.photoData)
        XCTAssertNil(repository.savedPlants.first?.photoData)
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

    // MARK: - Edición

    func testLoadPrecachesPlantData() async {
        let repository = MockPlantRepository()
        let plant = Plant(
            name: "Monstera",
            type: .monstera,
            photoData: Data([0x01, 0x02])
        )
        let viewModel = AddPlantViewModel(repository: repository)

        viewModel.load(plant)

        XCTAssertEqual(viewModel.name, "Monstera")
        XCTAssertEqual(viewModel.selectedType, .monstera)
        XCTAssertEqual(viewModel.photoData, Data([0x01, 0x02]))
        XCTAssertEqual(viewModel.editingPlant?.id, plant.id)
    }

    func testEditSaveUpdatesExistingPlant() async {
        let repository = MockPlantRepository()
        let plant = Plant(
            name: "Monstera",
            type: .monstera,
            lastWateredAt: Date.distantPast
        )
        repository.savedPlants.append(plant)

        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.load(plant)
        viewModel.name = "Monstera Editada"
        viewModel.selectedType = .ficus

        let didSave = await viewModel.save()

        XCTAssertTrue(didSave)
        XCTAssertNil(viewModel.saveError)
        XCTAssertEqual(repository.savedPlants.count, 1)
        XCTAssertEqual(repository.savedPlants.first?.name, "Monstera Editada")
        XCTAssertEqual(repository.savedPlants.first?.type, .ficus)
        // Preserva id y fechas originales
        XCTAssertEqual(repository.savedPlants.first?.id, plant.id)
        XCTAssertEqual(repository.savedPlants.first?.lastWateredAt, plant.lastWateredAt)
    }

    func testEditSaveWithoutNameShowsError() async {
        let repository = MockPlantRepository()
        let plant = Plant(name: "Monstera")
        repository.savedPlants.append(plant)
        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.load(plant)
        viewModel.name = ""

        let didSave = await viewModel.save()

        XCTAssertFalse(didSave)
        XCTAssertEqual(
            viewModel.nameError,
            "Ponle un nombre a tu planta para poder guardarla."
        )
        XCTAssertEqual(repository.savedPlants.count, 1)
        XCTAssertEqual(repository.savedPlants.first?.name, "Monstera")
    }

    func testEditSaveRepositoryFailureShowsCalmMessage() async {
        let repository = MockPlantRepository()
        repository.errorToThrow = MockError.saveFailed
        let plant = Plant(name: "Monstera")
        repository.savedPlants.append(plant)

        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.load(plant)
        viewModel.name = "Monstera Editada"

        let didSave = await viewModel.save()

        XCTAssertFalse(didSave)
        XCTAssertEqual(
            viewModel.saveError,
            "No hemos podido guardar los cambios. Inténtalo de nuevo."
        )
        XCTAssertFalse(viewModel.isSaving)
    }

    func testEditRemovingPhotoSavesPlantWithoutImage() async {
        let repository = MockPlantRepository()
        let plant = Plant(name: "Monstera", photoData: Data([0x01]))
        repository.savedPlants.append(plant)

        let viewModel = AddPlantViewModel(repository: repository)
        viewModel.load(plant)
        viewModel.removePhoto()

        let didSave = await viewModel.save()

        XCTAssertTrue(didSave)
        XCTAssertNil(viewModel.photoData)
        XCTAssertNil(repository.savedPlants.first?.photoData)
    }
}

// MARK: - Dobles de prueba

@MainActor
private final class MockPlantRepository: PlantRepository {
    var savedPlants: [Plant] = []
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

    func fetchPhotos(for plantID: UUID) async throws -> [PlantPhoto] { [] }
    func addPhoto(_ photo: PlantPhoto) async throws {}
}

private enum MockError: Error {
    case saveFailed
}
