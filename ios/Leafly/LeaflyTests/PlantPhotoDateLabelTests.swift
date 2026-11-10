//
//  PlantPhotoDateLabelTests.swift
//  LeaflyTests
//
//  Created by RaulF on 10/11/2026.
//

import XCTest
@testable import Leafly

final class PlantPhotoDateLabelTests: XCTestCase {

    private var calendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(secondsFromGMT: 0) ?? .gmt
        calendar.locale = Locale(identifier: "es_ES")
        return calendar
    }

    private func date(year: Int, month: Int, day: Int) -> Date {
        let components = DateComponents(year: year, month: month, day: day, hour: 12)
        return calendar.date(from: components) ?? Date(timeIntervalSince1970: 0)
    }

    func testTodayIsShownAsHoy() {
        let label = PlantPhotoDateLabel.label(
            for: date(year: 2026, month: 11, day: 10),
            now: date(year: 2026, month: 11, day: 10),
            calendar: calendar
        )

        XCTAssertEqual(label, "Hoy")
    }

    func testYesterdayIsShownAsAyer() {
        let label = PlantPhotoDateLabel.label(
            for: date(year: 2026, month: 11, day: 9),
            now: date(year: 2026, month: 11, day: 10),
            calendar: calendar
        )

        XCTAssertEqual(label, "Ayer")
    }

    func testOlderPhotoInTheCurrentYearShowsDayAndMonth() {
        let label = PlantPhotoDateLabel.label(
            for: date(year: 2026, month: 3, day: 4),
            now: date(year: 2026, month: 11, day: 10),
            calendar: calendar
        )

        XCTAssertTrue(label.contains("4"))
        XCTAssertTrue(label.lowercased().contains("mar"))
        XCTAssertFalse(label.contains("2026"))
    }

    func testPhotoFromAnotherYearShowsTheYear() {
        let label = PlantPhotoDateLabel.label(
            for: date(year: 2025, month: 12, day: 31),
            now: date(year: 2026, month: 11, day: 10),
            calendar: calendar
        )

        XCTAssertTrue(label.contains("2025"))
    }
}
