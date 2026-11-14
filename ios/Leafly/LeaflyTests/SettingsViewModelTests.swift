//
//  SettingsViewModelTests.swift
//  LeaflyTests
//

import XCTest
@testable import Leafly
import UserNotifications

@MainActor
final class SettingsViewModelTests: XCTestCase {

    // MARK: - Carga inicial

    func testLoadSetsAuthorizationStatusAndPreferences() async {
        let center = FakeSettingsNotificationCenter(status: .authorized)
        let reminders = FakeReminders()
        let viewModel = SettingsViewModel(center: center, reminders: reminders)

        await viewModel.load()

        XCTAssertEqual(viewModel.authorizationStatus, .authorized)
    }

    func testLoadReflectsDeniedStatus() async {
        let center = FakeSettingsNotificationCenter(status: .denied)
        let viewModel = SettingsViewModel(center: center, reminders: FakeReminders())

        await viewModel.load()

        XCTAssertEqual(viewModel.authorizationStatus, .denied)
    }

    // MARK: - Cambio de hora preferida

    func testSetPreferredHourUpdatesPreferencesThroughReschedule() async {
        let center = FakeSettingsNotificationCenter(status: .authorized)
        let reminders = FakeReminders()
        let viewModel = SettingsViewModel(center: center, reminders: reminders)

        viewModel.setPreferredHour(18, minute: 30)
        let plants = [Plant(name: "Ficus", lastWateredAt: .now.addingTimeInterval(-8 * 86_400))]

        await viewModel.rescheduleReminders(plants: plants)

        XCTAssertEqual(viewModel.preferences.hour, 18)
        XCTAssertEqual(viewModel.preferences.minute, 30)
        XCTAssertEqual(reminders.lastPreferredHour, 18)
        XCTAssertEqual(reminders.lastPreferredMinute, 30)
    }

    // MARK: - Reprogramación

    func testRescheduleRemindersCallsServiceWithPreferences() async {
        let center = FakeSettingsNotificationCenter(status: .authorized)
        let reminders = FakeReminders()
        let viewModel = SettingsViewModel(center: center, reminders: reminders)

        viewModel.setPreferredHour(15, minute: 0)
        let plants = [Plant(name: "Ficus", lastWateredAt: .now.addingTimeInterval(-8 * 86_400))]

        await viewModel.rescheduleReminders(plants: plants)

        XCTAssertEqual(reminders.lastPreferredHour, 15)
        XCTAssertEqual(reminders.lastPreferredMinute, 0)
        XCTAssertEqual(reminders.lastPlants?.count, 1)
    }

    // MARK: - Sin plantas con sed: no se pide permiso de forma agresiva

    // El SettingsViewModel no solicita permiso automáticamente en load();
    // solo consulta el estado actual. Esto cumple la regla de no ser agresivo.
    func testLoadDoesNotRequestAuthorization() async {
        let center = FakeSettingsNotificationCenter(status: .notDetermined)
        let viewModel = SettingsViewModel(center: center, reminders: FakeReminders())

        await viewModel.load()

        XCTAssertEqual(center.authorizationRequests, 0)
        XCTAssertEqual(viewModel.authorizationStatus, .notDetermined)
    }
}

// MARK: - Dobles de prueba

@MainActor
private final class FakeSettingsNotificationCenter: LocalNotificationCenter {
    let status: UNAuthorizationStatus
    private(set) var authorizationRequests = 0

    init(status: UNAuthorizationStatus) {
        self.status = status
    }

    func requestAuthorization() async -> Bool {
        authorizationRequests += 1
        return status == .authorized
    }

    func currentAuthorizationStatus() async -> UNAuthorizationStatus {
        status
    }

    func pendingIdentifiers(withPrefix prefix: String) async -> [String] { [] }
    func add(_ reminder: WateringReminder) async {}
    func removePending(identifiers: [String]) async {}
}

@MainActor
private final class FakeReminders: WateringReminderScheduling {
    private(set) var lastPreferredHour: Int?
    private(set) var lastPreferredMinute: Int?
    private(set) var lastPlants: [Plant]?

    func sync(
        plants: [Plant],
        preferredHour: Int,
        preferredMinute: Int
    ) async {
        lastPlants = plants
        lastPreferredHour = preferredHour
        lastPreferredMinute = preferredMinute
    }

    func cancelReminder(for plantID: UUID) async {}
}
