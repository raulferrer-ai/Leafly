//
//  WateringStatusTests.swift
//  LeaflyTests
//
//  Created by RaulF on 07/11/2026.
//

import XCTest
@testable import Leafly

final class WateringStatusTests: XCTestCase {

    private var calendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Europe/Madrid")!
        return calendar
    }

    private func date(year: Int = 2026, month: Int = 11, day: Int, hour: Int = 12) -> Date {
        calendar.date(from: DateComponents(year: year, month: month, day: day, hour: hour))!
    }

    // MARK: - Umbrales

    func testFreshlyWateredPlantIsHealthy() {
        let plant = Plant(name: "Monstera", lastWateredAt: date(day: 7))

        XCTAssertEqual(plant.wateringStatus(on: date(day: 7), calendar: calendar), .healthy)
    }

    func testLastDayBeforeSoonThresholdIsHealthy() {
        let plant = Plant(name: "Monstera", lastWateredAt: date(day: 1))

        XCTAssertEqual(plant.wateringStatus(on: date(day: 5), calendar: calendar), .healthy)
    }

    func testSoonThresholdStartsAtFiveDays() {
        let plant = Plant(name: "Monstera", lastWateredAt: date(day: 1))

        XCTAssertEqual(plant.wateringStatus(on: date(day: 6), calendar: calendar), .needsWaterSoon)
    }

    func testLastDayBeforeUrgentThresholdStillNeedsWaterSoon() {
        let plant = Plant(name: "Monstera", lastWateredAt: date(day: 1))

        XCTAssertEqual(plant.wateringStatus(on: date(day: 7), calendar: calendar), .needsWaterSoon)
    }

    func testUrgentThresholdStartsAtSevenDays() {
        let plant = Plant(name: "Monstera", lastWateredAt: date(day: 1))

        XCTAssertEqual(plant.wateringStatus(on: date(day: 8), calendar: calendar), .needsWaterNow)
    }

    func testStatusFromDaysIsClampedForFutureDates() {
        XCTAssertEqual(WateringStatus(daysSinceWatering: -3), .healthy)
    }

    // MARK: - Fecha de referencia

    func testPlantWithoutWateringUsesCreationDate() {
        let plant = Plant(name: "Nueva", createdAt: date(day: 1))

        XCTAssertEqual(plant.wateringReferenceDate, date(day: 1))
        XCTAssertEqual(plant.wateringStatus(on: date(day: 9), calendar: calendar), .needsWaterNow)
    }

    func testLastWateredDateTakesPrecedenceOverCreationDate() {
        let plant = Plant(
            name: "Ficus",
            createdAt: date(day: 1),
            lastWateredAt: date(day: 8)
        )

        XCTAssertEqual(plant.wateringReferenceDate, date(day: 8))
        XCTAssertEqual(plant.wateringStatus(on: date(day: 9), calendar: calendar), .healthy)
    }

    func testDaysSinceWateringIgnoresTimeOfDay() {
        let plant = Plant(name: "Potos", lastWateredAt: date(day: 1, hour: 23))

        XCTAssertEqual(plant.daysSinceWatering(on: date(day: 3, hour: 1), calendar: calendar), 2)
    }
}
