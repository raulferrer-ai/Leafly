//
//  WateringReminderPlannerTests.swift
//  LeaflyTests
//
//  Created by RaulF on 08/11/2026.
//

import XCTest
@testable import Leafly

final class WateringReminderPlannerTests: XCTestCase {

    private var calendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Europe/Madrid")!
        return calendar
    }

    private func date(
        year: Int = 2026,
        month: Int = 11,
        day: Int,
        hour: Int = 12,
        minute: Int = 0
    ) -> Date {
        calendar.date(
            from: DateComponents(
                year: year,
                month: month,
                day: day,
                hour: hour,
                minute: minute
            )
        )!
    }

    // MARK: - Qué plantas generan recordatorio

    func testHealthyPlantDoesNotCreateReminder() {
        let plant = Plant(name: "Monstera", lastWateredAt: date(day: 7))
        let now = date(day: 7)

        let reminders = WateringReminderPlanner.reminders(
            for: [plant],
            now: now,
            calendar: calendar
        )

        XCTAssertTrue(reminders.isEmpty)
    }

    func testPlantThatNeedsWaterSoonCreatesReminder() {
        let plant = Plant(name: "Ficus", lastWateredAt: date(day: 1))

        let reminders = WateringReminderPlanner.reminders(
            for: [plant],
            now: date(day: 6),
            calendar: calendar
        )

        XCTAssertEqual(reminders.count, 1)
        XCTAssertEqual(reminders.first?.plantID, plant.id)
        XCTAssertEqual(reminders.first?.plantName, "Ficus")
        XCTAssertEqual(reminders.first?.status, .needsWaterSoon)
    }

    func testPlantThatNeedsWaterNowCreatesReminder() {
        let plant = Plant(name: "Monstera", lastWateredAt: date(day: 1))

        let reminders = WateringReminderPlanner.reminders(
            for: [plant],
            now: date(day: 9),
            calendar: calendar
        )

        XCTAssertEqual(reminders.count, 1)
        XCTAssertEqual(reminders.first?.status, .needsWaterNow)
    }

    func testOnlyPlantsThatNeedAttentionAreIncluded() {
        let plants = [
            Plant(name: "Al día", lastWateredAt: date(day: 7)),
            Plant(name: "Pronto", lastWateredAt: date(day: 3)),
            Plant(name: "Urgente", lastWateredAt: date(day: 1)),
        ]

        let reminders = WateringReminderPlanner.reminders(
            for: plants,
            now: date(day: 9),
            calendar: calendar
        )

        XCTAssertEqual(reminders.map(\.plantName), ["Pronto", "Urgente"])
        XCTAssertEqual(reminders.map(\.status), [.needsWaterSoon, .needsWaterNow])
    }

    func testEachReminderHasAStableIdentifierForItsPlant() {
        let plant = Plant(name: "Potos", lastWateredAt: date(day: 1))

        let first = WateringReminderPlanner.reminders(
            for: [plant],
            now: date(day: 8),
            calendar: calendar
        )
        let second = WateringReminderPlanner.reminders(
            for: [plant],
            now: date(day: 9),
            calendar: calendar
        )

        XCTAssertEqual(first.first?.id, second.first?.id)
        XCTAssertEqual(first.first?.id, WateringReminder.identifier(for: plant.id))
        XCTAssertEqual(first.first?.id, "watering-reminder-\(plant.id.uuidString)")
    }

    func testReminderTextsAreCalmAndIncludeThePlantName() {
        let soon = Plant(name: "Ficus", lastWateredAt: date(day: 2))
        let urgent = Plant(name: "Monstera", lastWateredAt: date(day: 1))

        let reminders = WateringReminderPlanner.reminders(
            for: [soon, urgent],
            now: date(day: 9),
            calendar: calendar
        )

        let soonReminder = reminders.first { $0.plantID == soon.id }
        XCTAssertTrue(soonReminder?.title.contains("Ficus") == true)
        XCTAssertTrue(soonReminder?.body.contains("Ficus") == true)

        let urgentReminder = reminders.first { $0.plantID == urgent.id }
        XCTAssertTrue(urgentReminder?.title.contains("Monstera") == true)
        XCTAssertTrue(urgentReminder?.body.contains("Monstera") == true)
    }

    // MARK: - Momento del recordatorio

    func testReminderUsesTodayAtTenWhenThereIsEnoughTime() {
        let fireDate = WateringReminderPlanner.nextReminderDate(
            after: date(day: 8, hour: 8),
            calendar: calendar
        )

        XCTAssertEqual(fireDate, date(day: 8, hour: 10))
    }

    func testReminderMovesToTomorrowWhenTheSlotHasPassed() {
        let fireDate = WateringReminderPlanner.nextReminderDate(
            after: date(day: 8, hour: 11),
            calendar: calendar
        )

        XCTAssertEqual(fireDate, date(day: 9, hour: 10))
    }

    func testReminderKeepsAtLeastOneHourOfMargin() {
        let fireDate = WateringReminderPlanner.nextReminderDate(
            after: date(day: 8, hour: 9, minute: 30),
            calendar: calendar
        )

        XCTAssertEqual(fireDate, date(day: 9, hour: 10))
    }

    func testReminderDateIsAlwaysInTheFuture() {
        let now = date(day: 8, hour: 10)

        let fireDate = WateringReminderPlanner.nextReminderDate(
            after: now,
            calendar: calendar
        )

        XCTAssertGreaterThan(fireDate, now)
        XCTAssertGreaterThanOrEqual(
            fireDate.timeIntervalSince(now),
            WateringReminderPlanner.minimumLeadTime
        )
    }
}
