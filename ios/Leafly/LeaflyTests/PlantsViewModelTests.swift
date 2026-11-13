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
        let viewModel = makeViewModel(repository: repository)

        await viewModel.load()

        XCTAssertEqual(viewModel.plants, plants)
        XCTAssertNil(viewModel.errorMessage)
        XCTAssertFalse(viewModel.isLoading)
    }

    func testLoadWithoutPlantsLeavesEmptyListWithoutError() async {
        let viewModel = makeViewModel(repository: MockPlantRepository(plants: []))

        await viewModel.load()

        XCTAssertTrue(viewModel.plants.isEmpty)
        XCTAssertNil(viewModel.errorMessage)
        XCTAssertFalse(viewModel.isLoading)
    }

    func testLoadFailureShowsCalmMessage() async {
        let repository = MockPlantRepository(plants: [])
        repository.errorToThrow = MockError.fetchFailed
        let viewModel = makeViewModel(repository: repository)

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
        let viewModel = makeViewModel(repository: repository)

        await viewModel.load()
        XCTAssertNotNil(viewModel.errorMessage)

        repository.errorToThrow = nil
        repository.plants = [Plant(name: "Orquídea", type: .orchid)]
        await viewModel.load()

        XCTAssertNil(viewModel.errorMessage)
        XCTAssertEqual(viewModel.plants.map(\.name), ["Orquídea"])
    }

    // MARK: - Recordatorios

    func testLoadSynchronizesRemindersWithTheLoadedPlants() async {
        let plants = [Plant(name: "Monstera"), Plant(name: "Ficus")]
        let reminders = MockPlantsReminders()
        let viewModel = makeViewModel(
            repository: MockPlantRepository(plants: plants),
            reminders: reminders
        )

        await viewModel.load()

        XCTAssertEqual(reminders.syncedPlants, [plants])
    }

    func testLoadFailureDoesNotTouchReminders() async {
        let repository = MockPlantRepository(plants: [])
        repository.errorToThrow = MockError.fetchFailed
        let reminders = MockPlantsReminders()
        let viewModel = makeViewModel(repository: repository, reminders: reminders)

        await viewModel.load()

        XCTAssertTrue(reminders.syncedPlants.isEmpty)
    }

    func testReplaceReflectsAPlantUpdatedInTheDetail() async {
        let plant = Plant(name: "Monstera", type: .monstera)
        let viewModel = makeViewModel(repository: MockPlantRepository(plants: [plant]))
        await viewModel.load()

        var wateredPlant = plant
        wateredPlant.lastWateredAt = .now
        await viewModel.replace(wateredPlant)

        XCTAssertEqual(viewModel.plants.first?.lastWateredAt, wateredPlant.lastWateredAt)
        XCTAssertEqual(viewModel.plants.first?.name, "Monstera")
    }

    func testReplaceSynchronizesRemindersAfterTheChange() async {
        let plant = Plant(name: "Monstera")
        let reminders = MockPlantsReminders()
        let viewModel = makeViewModel(
            repository: MockPlantRepository(plants: [plant]),
            reminders: reminders
        )
        await viewModel.load()

        var wateredPlant = plant
        wateredPlant.lastWateredAt = .now
        await viewModel.replace(wateredPlant)

        XCTAssertEqual(reminders.syncedPlants.count, 2)
        XCTAssertEqual(reminders.syncedPlants.last, [wateredPlant])
    }

    func testReplaceIgnoresUnknownPlants() async {
        let reminders = MockPlantsReminders()
        let viewModel = makeViewModel(
            repository: MockPlantRepository(plants: [Plant(name: "Monstera")]),
            reminders: reminders
        )
        await viewModel.load()

        await viewModel.replace(Plant(name: "Otra"))

        XCTAssertEqual(viewModel.plants.map(\.name), ["Monstera"])
        XCTAssertEqual(reminders.syncedPlants.count, 1)
    }

    // MARK: - Búsqueda y filtros

    func testFilterByNameCaseInsensitive() async {
        let plants = [
            Plant(name: "Monstera", type: .monstera),
            Plant(name: "Ficus", type: .ficus),
            Plant(name: "Potos", type: .pothos),
            Plant(name: "Cactus", type: .cactus),
        ]
        let repository = MockPlantRepository(plants: plants)
        let viewModel = makeViewModel(repository: repository)
        await viewModel.load()

        viewModel.setSearchText("mon")
        XCTAssertEqual(viewModel.filteredPlants.map(\.name), ["Monstera"])

        viewModel.setSearchText("MON")
        XCTAssertEqual(viewModel.filteredPlants.map(\.name), ["Monstera"])

        viewModel.setSearchText("c")
        XCTAssertEqual(viewModel.filteredPlants.map(\.name).sorted(), ["Cactus", "Ficus"])
    }

    func testFilterByWateringStatus() async {
        let now = Date()
        let plants = [
            Plant(name: "Al día", type: .monstera, lastWateredAt: now),
            Plant(name: "Pronto", type: .ficus, lastWateredAt: now.addingTimeInterval(-6 * 86_400)),
            Plant(name: "Urgente", type: .pothos, lastWateredAt: now.addingTimeInterval(-8 * 86_400)),
        ]
        let repository = MockPlantRepository(plants: plants)
        let viewModel = makeViewModel(repository: repository)
        await viewModel.load()

        viewModel.setStatusFilter(.healthy)
        XCTAssertEqual(viewModel.filteredPlants.map(\.name), ["Al día"])

        viewModel.setStatusFilter(.needsWaterSoon)
        XCTAssertEqual(viewModel.filteredPlants.map(\.name), ["Pronto"])

        viewModel.setStatusFilter(.needsWaterNow)
        XCTAssertEqual(viewModel.filteredPlants.map(\.name), ["Urgente"])
    }

    func testFilterByStatusTogglesOffWhenReselected() async {
        let plants = [Plant(name: "Monstera", type: .monstera, lastWateredAt: .now)]
        let repository = MockPlantRepository(plants: plants)
        let viewModel = makeViewModel(repository: repository)
        await viewModel.load()

        viewModel.setStatusFilter(.healthy)
        XCTAssertEqual(viewModel.filteredPlants.count, 1)

        viewModel.setStatusFilter(.healthy)
        XCTAssertNil(viewModel.selectedStatusFilter)
        XCTAssertEqual(viewModel.filteredPlants.count, 1)
    }

    func testCombineSearchAndStatusFilter() async {
        let now = Date()
        let plants = [
            Plant(name: "Monstera deliciosa", type: .monstera, lastWateredAt: now),
            Plant(name: "Monstera adansonii", type: .monstera, lastWateredAt: now.addingTimeInterval(-6 * 86_400)),
            Plant(name: "Ficus lyrata", type: .ficus, lastWateredAt: now),
            Plant(name: "Ficus elastica", type: .ficus, lastWateredAt: now.addingTimeInterval(-8 * 86_400)),
        ]
        let repository = MockPlantRepository(plants: plants)
        let viewModel = makeViewModel(repository: repository)
        await viewModel.load()

        viewModel.setSearchText("monstera")
        XCTAssertEqual(viewModel.filteredPlants.count, 2)

        viewModel.setStatusFilter(.needsWaterSoon)
        XCTAssertEqual(viewModel.filteredPlants.map(\.name), ["Monstera adansonii"])

        // Búsqueda vacía + filtro activo
        viewModel.setSearchText(nil)
        XCTAssertEqual(viewModel.filteredPlants.map(\.name), ["Monstera adansonii"])
    }

    func testClearFiltersRestoresFullList() async {
        let plants = [
            Plant(name: "Monstera", type: .monstera, lastWateredAt: .now),
            Plant(name: "Ficus", type: .ficus, lastWateredAt: .now.addingTimeInterval(-6 * 86_400)),
        ]
        let repository = MockPlantRepository(plants: plants)
        let viewModel = makeViewModel(repository: repository)
        await viewModel.load()

        viewModel.setSearchText("ficus")
        viewModel.setStatusFilter(.healthy)
        XCTAssertTrue(viewModel.filteredPlants.isEmpty)

        viewModel.clearFilters()
        XCTAssertEqual(viewModel.filteredPlants.count, 2)
        XCTAssertTrue(viewModel.searchText.isEmpty)
        XCTAssertNil(viewModel.selectedStatusFilter)
    }

    func testSearchWithNoResultsShowsEmptyFilteredList() async {
        let plants = [Plant(name: "Monstera", type: .monstera)]
        let repository = MockPlantRepository(plants: plants)
        let viewModel = makeViewModel(repository: repository)
        await viewModel.load()

        viewModel.setSearchText("cactus")
        XCTAssertTrue(viewModel.filteredPlants.isEmpty)
        XCTAssertTrue(viewModel.hasActiveFilters)
    }

    // MARK: - Helpers

    private func makeViewModel(
        repository: MockPlantRepository,
        reminders: MockPlantsReminders = MockPlantsReminders()
    ) -> PlantsViewModel {
        PlantsViewModel(repository: repository, reminders: reminders)
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

    func fetchPhotos(for plantID: UUID) async throws -> [PlantPhoto] { [] }
    func addPhoto(_ photo: PlantPhoto) async throws {}
}

@MainActor
private final class MockPlantsReminders: WateringReminderScheduling {
    private(set) var syncedPlants: [[Plant]] = []
    private(set) var cancelledPlantIDs: [UUID] = []

    func sync(plants: [Plant]) async {
        syncedPlants.append(plants)
    }

    func cancelReminder(for plantID: UUID) async {
        cancelledPlantIDs.append(plantID)
    }
}

private enum MockError: Error {
    case fetchFailed
}
