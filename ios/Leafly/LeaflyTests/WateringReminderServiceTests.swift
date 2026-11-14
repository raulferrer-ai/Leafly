//
//  WateringReminderServiceTests.swift
//  LeaflyTests
//
//  Created by RaulF on 08/11/2026.
//

import XCTest
@testable import Leafly

@MainActor
final class WateringReminderServiceTests: XCTestCase {

    private var calendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Europe/Madrid")!
        return calendar
    }

    private func date(
        year: Int = 2026,
        month: Int = 11,
        day: Int,
        hour: Int = 12
    ) -> Date {
        calendar.date(
            from: DateComponents(
                year: year,
                month: month,
                day: day,
                hour: hour
            )
        )!
    }

    private func makeService(
        center: FakeNotificationCenter,
        now: Date
    ) -> WateringReminderService {
        WateringReminderService(
            center: center,
            now: { now },
            calendar: calendar
        )
    }

    // MARK: - Programación

    func testSyncSchedulesOnlyPlantsThatNeedAttention() async {
        let center = FakeNotificationCenter()
        let service = makeService(center: center, now: date(day: 9))

        let plants = [
            Plant(name: "Al día", lastWateredAt: date(day: 9)),
            Plant(name: "Pronto", lastWateredAt: date(day: 4)),
            Plant(name: "Urgente", lastWateredAt: date(day: 1)),
        ]

        await service.sync(plants: plants, preferredHour: 10, preferredMinute: 0)

        XCTAssertEqual(center.addedReminders.map(\.plantName), ["Pronto", "Urgente"])
        XCTAssertEqual(center.authorizationRequests, 1)
    }

    func testSyncDoesNotRequestAuthorizationWhenThereIsNothingToSchedule() async {
        let center = FakeNotificationCenter()
        let service = makeService(center: center, now: date(day: 9))

        await service.sync(plants: [Plant(name: "Al día", lastWateredAt: date(day: 9))], preferredHour: 10, preferredMinute: 0)

        XCTAssertTrue(center.addedReminders.isEmpty)
        XCTAssertEqual(center.authorizationRequests, 0)
    }

    func testSyncRemovesRemindersForPlantsThatAreNowHealthy() async {
        let healthyPlant = Plant(name: "Al día", lastWateredAt: date(day: 9))
        let thirstyPlant = Plant(name: "Urgente", lastWateredAt: date(day: 1))

        let center = FakeNotificationCenter()
        center.pending = [
            WateringReminder.identifier(for: healthyPlant.id),
            WateringReminder.identifier(for: thirstyPlant.id),
        ]

        let service = makeService(center: center, now: date(day: 9))
        await service.sync(plants: [healthyPlant, thirstyPlant], preferredHour: 10, preferredMinute: 0)

        XCTAssertEqual(
            center.removedIdentifiers,
            [[WateringReminder.identifier(for: healthyPlant.id)]]
        )
        XCTAssertEqual(center.addedReminders.map(\.plantName), ["Urgente"])
    }

    func testSyncKeepsRemindersForPlantsThatStillNeedAttention() async {
        let plant = Plant(name: "Urgente", lastWateredAt: date(day: 1))

        let center = FakeNotificationCenter()
        center.pending = [WateringReminder.identifier(for: plant.id)]

        let service = makeService(center: center, now: date(day: 9))
        await service.sync(plants: [plant], preferredHour: 10, preferredMinute: 0)

        XCTAssertTrue(center.removedIdentifiers.isEmpty)
        XCTAssertEqual(center.addedReminders.map(\.plantName), ["Urgente"])
    }

    func testSyncRequestsAuthorizationOnlyOncePerSession() async {
        let center = FakeNotificationCenter()
        let service = makeService(center: center, now: date(day: 9))
        let plant = Plant(name: "Urgente", lastWateredAt: date(day: 1))

        await service.sync(plants: [plant], preferredHour: 10, preferredMinute: 0)
        await service.sync(plants: [plant], preferredHour: 10, preferredMinute: 0)

        XCTAssertEqual(center.authorizationRequests, 1)
        XCTAssertEqual(center.addedReminders.count, 2)
    }

    // MARK: - Permiso denegado

    func testDeniedPermissionDoesNotScheduleAnythingAndDoesNotFail() async {
        let center = FakeNotificationCenter()
        center.authorizationGranted = false
        let service = makeService(center: center, now: date(day: 9))

        await service.sync(plants: [Plant(name: "Urgente", lastWateredAt: date(day: 1))], preferredHour: 10, preferredMinute: 0)

        XCTAssertTrue(center.addedReminders.isEmpty)
        XCTAssertEqual(center.authorizationRequests, 1)
    }

    // MARK: - Cancelación

    func testCancelReminderRemovesOnlyThatPlantIdentifier() async {
        let plant = Plant(name: "Urgente", lastWateredAt: date(day: 1))
        let identifier = WateringReminder.identifier(for: plant.id)

        let center = FakeNotificationCenter()
        center.pending = [identifier, "watering-reminder-otra-planta"]

        let service = makeService(center: center, now: date(day: 9))
        await service.cancelReminder(for: plant.id)

        XCTAssertEqual(center.removedIdentifiers, [[identifier]])
    }

    // MARK: - Texto de las notificaciones

    func testScheduledReminderIncludesThePlantName() async {
        let center = FakeNotificationCenter()
        let service = makeService(center: center, now: date(day: 9))

        await service.sync(plants: [Plant(name: "Monstera", lastWateredAt: date(day: 1))], preferredHour: 10, preferredMinute: 0)

        let reminder = center.addedReminders.first
        XCTAssertEqual(reminder?.plantName, "Monstera")
        XCTAssertTrue(reminder?.title.contains("Monstera") == true)
        XCTAssertTrue(reminder?.body.contains("Monstera") == true)
    }
}

// MARK: - Doble de prueba del centro de notificaciones

@MainActor
private final class FakeNotificationCenter: LocalNotificationCenter {
    var authorizationGranted = true
    private(set) var authorizationRequests = 0

    var pending: [String] = []
    private(set) var addedReminders: [WateringReminder] = []
    private(set) var removedIdentifiers: [[String]] = []

    func requestAuthorization() async -> Bool {
        authorizationRequests += 1
        return authorizationGranted
    }

    func currentAuthorizationStatus() async -> UNAuthorizationStatus {
        authorizationGranted ? .authorized : .denied
    }

    func pendingIdentifiers(withPrefix prefix: String) async -> [String] {
        pending.filter { $0.hasPrefix(prefix) }
    }

    func add(_ reminder: WateringReminder) async {
        addedReminders.append(reminder)
        pending.removeAll { $0 == reminder.id }
        pending.append(reminder.id)
    }

    func removePending(identifiers: [String]) async {
        removedIdentifiers.append(identifiers)
        pending.removeAll { identifiers.contains($0) }
    }
}
