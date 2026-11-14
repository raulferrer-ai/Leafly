//
//  PlantDetailViewModelTests.swift
//  LeaflyTests
//
//  Created by RaulF on 08/11/2026.
//

import XCTest
@testable import Leafly

@MainActor
final class PlantDetailViewModelTests: XCTestCase {

    private let now = Date(timeIntervalSince1970: 1_800_000_000)

    private func date(daysAgo days: Int, from date: Date) -> Date {
        Calendar.current.date(byAdding: .day, value: -days, to: date) ?? date
    }

    // MARK: - Información mostrada

    func testShowsPlantInformation() async {
        let plant = Plant(
            name: "Ficus",
            type: .ficus,
            createdAt: date(daysAgo: 30, from: now),
            lastWateredAt: now
        )
        let viewModel = makeViewModel(plant: plant)

        XCTAssertEqual(viewModel.plant.name, "Ficus")
        XCTAssertEqual(viewModel.plant.type, .ficus)
        XCTAssertEqual(viewModel.status, .healthy)
        XCTAssertEqual(viewModel.summary, "Regada hoy")
        XCTAssertNil(viewModel.errorMessage)
        XCTAssertFalse(viewModel.isSaving)
    }

    func testPlantWithoutWateringShowsWhenItWasAdded() async {
        let plant = Plant(name: "Nueva", createdAt: now)
        let viewModel = makeViewModel(plant: plant)

        XCTAssertEqual(viewModel.status, .healthy)
        XCTAssertEqual(viewModel.summary, "Añadida hoy")
    }

    func testPlantThatNeedsWaterShowsCalmSummary() async {
        let plant = Plant(
            name: "Monstera",
            lastWateredAt: date(daysAgo: 6, from: now)
        )
        let viewModel = makeViewModel(plant: plant)

        XCTAssertEqual(viewModel.status, .needsWaterSoon)
        XCTAssertEqual(viewModel.summary, "Regada hace 6 días")
    }

    // MARK: - Marcar como regada

    func testMarkAsWateredUpdatesDateAndStatus() async {
        let plant = Plant(
            name: "Monstera",
            type: .monstera,
            lastWateredAt: date(daysAgo: 10, from: now)
        )
        let viewModel = makeViewModel(plant: plant)
        XCTAssertEqual(viewModel.status, .needsWaterNow)

        let didSave = await viewModel.markAsWatered()

        XCTAssertTrue(didSave)
        XCTAssertEqual(viewModel.plant.lastWateredAt, now)
        XCTAssertEqual(viewModel.status, .healthy)
        XCTAssertEqual(viewModel.summary, "Regada hoy")
        XCTAssertNil(viewModel.errorMessage)
        XCTAssertFalse(viewModel.isSaving)
    }

    func testMarkAsWateredPersistsTheUpdatedPlant() async {
        let plant = Plant(name: "Ficus", type: .ficus)
        let repository = MockPlantDetailRepository(plants: [plant])
        let viewModel = makeViewModel(plant: plant, repository: repository)

        await viewModel.markAsWatered()

        XCTAssertEqual(repository.updatedPlants.count, 1)
        XCTAssertEqual(repository.updatedPlants.first?.id, plant.id)
        XCTAssertEqual(repository.updatedPlants.first?.lastWateredAt, now)
        XCTAssertEqual(repository.plants.first?.lastWateredAt, now)
    }

    func testMarkAsWateredKeepsTheRestOfThePlant() async {
        let createdAt = date(daysAgo: 20, from: now)
        let plant = Plant(name: "Potos", type: .pothos, createdAt: createdAt)
        let repository = MockPlantDetailRepository(plants: [plant])
        let viewModel = makeViewModel(plant: plant, repository: repository)

        await viewModel.markAsWatered()

        XCTAssertEqual(viewModel.plant.id, plant.id)
        XCTAssertEqual(viewModel.plant.name, "Potos")
        XCTAssertEqual(viewModel.plant.type, .pothos)
        XCTAssertEqual(viewModel.plant.createdAt, createdAt)
    }

    func testMarkAsWateredUsesTheCurrentDateAtCallTime() async {
        let firstNow = now
        let laterNow = now.addingTimeInterval(3_600)
        var currentDate = firstNow

        let plant = Plant(name: "Monstera", lastWateredAt: firstNow)
        let repository = MockPlantDetailRepository(plants: [plant])
        let viewModel = PlantDetailViewModel(
            plant: plant,
            repository: repository,
            reminders: MockDetailReminders(),
            now: { currentDate }
        )

        currentDate = laterNow
        await viewModel.markAsWatered()

        XCTAssertEqual(viewModel.plant.lastWateredAt, laterNow)
    }

    // MARK: - Recordatorios

    func testMarkAsWateredCancelsThePlantReminder() async {
        let plant = Plant(
            name: "Monstera",
            lastWateredAt: date(daysAgo: 8, from: now)
        )
        let reminders = MockDetailReminders()
        let viewModel = makeViewModel(plant: plant, reminders: reminders)

        let didSave = await viewModel.markAsWatered()

        XCTAssertTrue(didSave)
        XCTAssertEqual(reminders.cancelledPlantIDs, [plant.id])
    }

    func testMarkAsWateredFailureKeepsTheReminder() async {
        let plant = Plant(name: "Monstera", lastWateredAt: date(daysAgo: 8, from: now))
        let repository = MockPlantDetailRepository(plants: [plant])
        repository.updateError = MockError.updateFailed
        let reminders = MockDetailReminders()
        let viewModel = makeViewModel(
            plant: plant,
            repository: repository,
            reminders: reminders
        )

        let didSave = await viewModel.markAsWatered()

        XCTAssertFalse(didSave)
        XCTAssertTrue(reminders.cancelledPlantIDs.isEmpty)
    }

    // MARK: - Errores

    func testMarkAsWateredFailureKeepsPlantAndShowsMessage() async {
        let plant = Plant(name: "Monstera", lastWateredAt: date(daysAgo: 8, from: now))
        let repository = MockPlantDetailRepository(plants: [plant])
        repository.updateError = MockError.updateFailed
        let viewModel = makeViewModel(plant: plant, repository: repository)

        let didSave = await viewModel.markAsWatered()

        XCTAssertFalse(didSave)
        XCTAssertEqual(viewModel.plant, plant)
        XCTAssertEqual(viewModel.status, .needsWaterNow)
        XCTAssertEqual(
            viewModel.errorMessage,
            "No hemos podido guardar el riego. Inténtalo de nuevo."
        )
        XCTAssertFalse(viewModel.isSaving)
    }

    // MARK: - Historial de fotos

    func testLoadPhotoHistoryShowsNewestFirst() async {
        let plant = Plant(name: "Monstera")
        let older = PlantPhoto(
            plantID: plant.id,
            addedAt: date(daysAgo: 5, from: now),
            data: Data([0x01])
        )
        let newest = PlantPhoto(plantID: plant.id, addedAt: now, data: Data([0x02]))
        let repository = MockPlantDetailRepository(
            plants: [plant],
            photos: [older, newest]
        )
        let viewModel = makeViewModel(plant: plant, repository: repository)

        await viewModel.loadPhotoHistory()

        XCTAssertEqual(viewModel.photos.map(\.id), [newest.id, older.id])
        XCTAssertNil(viewModel.photoErrorMessage)
        XCTAssertFalse(viewModel.isLoadingPhotos)
    }

    func testLoadPhotoHistoryWithoutPhotosLeavesItEmpty() async {
        let plant = Plant(name: "Nueva")
        let viewModel = makeViewModel(plant: plant)

        await viewModel.loadPhotoHistory()

        XCTAssertTrue(viewModel.photos.isEmpty)
        XCTAssertNil(viewModel.photoErrorMessage)
    }

    func testLoadPhotoHistoryFailureShowsCalmMessage() async {
        let plant = Plant(name: "Monstera")
        let repository = MockPlantDetailRepository(plants: [plant])
        repository.fetchPhotosError = MockError.fetchPhotosFailed
        let viewModel = makeViewModel(plant: plant, repository: repository)

        await viewModel.loadPhotoHistory()

        XCTAssertTrue(viewModel.photos.isEmpty)
        XCTAssertEqual(
            viewModel.photoErrorMessage,
            "No hemos podido cargar las fotos. Inténtalo de nuevo."
        )
        XCTAssertFalse(viewModel.isLoadingPhotos)
    }

    func testLoadPhotoHistoryRetryAfterFailureLoadsSuccessfully() async {
        let plant = Plant(name: "Monstera")
        let repository = MockPlantDetailRepository(
            plants: [plant],
            photos: [PlantPhoto(plantID: plant.id, addedAt: now, data: Data([0x01]))]
        )
        repository.fetchPhotosError = MockError.fetchPhotosFailed
        let viewModel = makeViewModel(plant: plant, repository: repository)

        // Primera carga: falla
        await viewModel.loadPhotoHistory()
        XCTAssertNotNil(viewModel.photoErrorMessage)
        XCTAssertTrue(viewModel.photos.isEmpty)

        // Segundo carga: se resuelve el error y se reintenta
        repository.fetchPhotosError = nil
        await viewModel.loadPhotoHistory()

        XCTAssertEqual(viewModel.photos.count, 1)
        XCTAssertNil(viewModel.photoErrorMessage)
        XCTAssertFalse(viewModel.isLoadingPhotos)
    }

    func testAddPhotoPersistsItAndUpdatesTheMainPhoto() async {
        let plant = Plant(name: "Monstera")
        let repository = MockPlantDetailRepository(plants: [plant])
        let viewModel = makeViewModel(plant: plant, repository: repository)
        let photoData = Data([0x0A, 0x0B])

        let didAdd = await viewModel.addPhoto(photoData)

        XCTAssertTrue(didAdd)
        XCTAssertEqual(viewModel.photos.count, 1)
        XCTAssertEqual(viewModel.photos.first?.data, photoData)
        XCTAssertEqual(viewModel.photos.first?.addedAt, now)
        XCTAssertEqual(viewModel.plant.photoData, photoData)
        XCTAssertEqual(repository.photosByPlant[plant.id]?.count, 1)
        XCTAssertNil(viewModel.photoErrorMessage)
        XCTAssertFalse(viewModel.isAddingPhoto)
    }

    func testAddingAPhotoKeepsTheExistingHistoryAndGoesFirst() async {
        let plant = Plant(name: "Monstera")
        let previous = PlantPhoto(
            plantID: plant.id,
            addedAt: date(daysAgo: 3, from: now),
            data: Data([0x01])
        )
        let repository = MockPlantDetailRepository(
            plants: [plant],
            photos: [previous]
        )
        let viewModel = makeViewModel(plant: plant, repository: repository)
        await viewModel.loadPhotoHistory()

        let newPhotoData = Data([0x02])
        await viewModel.addPhoto(newPhotoData)

        XCTAssertEqual(viewModel.photos.count, 2)
        XCTAssertEqual(viewModel.photos.first?.data, newPhotoData)
        XCTAssertEqual(viewModel.photos.last?.id, previous.id)
        XCTAssertEqual(viewModel.plant.photoData, newPhotoData)
    }

    func testAddPhotoFailureKeepsHistoryAndShowsMessage() async {
        let plant = Plant(name: "Monstera")
        let repository = MockPlantDetailRepository(plants: [plant])
        repository.addPhotoError = MockError.addPhotoFailed
        let viewModel = makeViewModel(plant: plant, repository: repository)

        let didAdd = await viewModel.addPhoto(Data([0x0C]))

        XCTAssertFalse(didAdd)
        XCTAssertTrue(viewModel.photos.isEmpty)
        XCTAssertNil(viewModel.plant.photoData)
        XCTAssertEqual(
            viewModel.photoErrorMessage,
            "No hemos podido guardar la foto. Inténtalo de nuevo."
        )
        XCTAssertFalse(viewModel.isAddingPhoto)
    }

    func testMarkAsWateredKeepsThePhoto() async {
        let photoData = Data([0x01, 0x02, 0x03])
        let plant = Plant(name: "Ficus", photoData: photoData)
        let viewModel = makeViewModel(plant: plant)

        await viewModel.markAsWatered()

        XCTAssertEqual(viewModel.plant.photoData, photoData)
    }

    // MARK: - Helpers

    private func makeViewModel(
        plant: Plant,
        repository: MockPlantDetailRepository? = nil,
        reminders: MockDetailReminders = MockDetailReminders()
    ) -> PlantDetailViewModel {
        PlantDetailViewModel(
            plant: plant,
            repository: repository ?? MockPlantDetailRepository(plants: [plant]),
            reminders: reminders,
            now: { self.now }
        )
    }
}

// MARK: - Dobles de prueba

@MainActor
private final class MockPlantDetailRepository: PlantRepository {
    var plants: [Plant]
    private(set) var updatedPlants: [Plant] = []
    private(set) var photosByPlant: [UUID: [PlantPhoto]] = [:]
    var updateError: Error?
    var fetchPhotosError: Error?
    var addPhotoError: Error?

    init(plants: [Plant], photos: [PlantPhoto] = []) {
        self.plants = plants

        for photo in photos {
            photosByPlant[photo.plantID, default: []].append(photo)
        }
    }

    func fetchPlants() async throws -> [Plant] {
        plants
    }

    func add(_ plant: Plant) async throws {
        plants.append(plant)
    }

    func update(_ plant: Plant) async throws {
        if let updateError {
            throw updateError
        }

        guard let index = plants.firstIndex(where: { $0.id == plant.id }) else {
            throw PlantRepositoryError.plantNotFound
        }

        plants[index] = plant
        updatedPlants.append(plant)
    }

    func fetchPhotos(for plantID: UUID) async throws -> [PlantPhoto] {
        if let fetchPhotosError {
            throw fetchPhotosError
        }

        return (photosByPlant[plantID] ?? []).sorted { $0.addedAt > $1.addedAt }
    }

    func addPhoto(_ photo: PlantPhoto) async throws {
        if let addPhotoError {
            throw addPhotoError
        }

        photosByPlant[photo.plantID, default: []].append(photo)
    }
}

@MainActor
private final class MockDetailReminders: WateringReminderScheduling {
    private(set) var syncedPlants: [[Plant]] = []
    private(set) var cancelledPlantIDs: [UUID] = []

    func sync(
        plants: [Plant],
        preferredHour: Int,
        preferredMinute: Int
    ) async {
        syncedPlants.append(plants)
    }

    func cancelReminder(for plantID: UUID) async {
        cancelledPlantIDs.append(plantID)
    }
}

private enum MockError: Error {
    case updateFailed
    case fetchPhotosFailed
    case addPhotoFailed
}
