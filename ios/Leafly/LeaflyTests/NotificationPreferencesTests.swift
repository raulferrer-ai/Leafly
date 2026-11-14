//
//  NotificationPreferencesTests.swift
//  LeaflyTests
//

import XCTest
@testable import Leafly

final class NotificationPreferencesTests: XCTestCase {

    // MARK: - Valores por defecto

    func testDefaultPreferencesAreTenAM() {
        let preferences = NotificationPreferences()
        XCTAssertEqual(preferences.hour, 10)
        XCTAssertEqual(preferences.minute, 0)
    }

    func testCustomPreferencesAreStoredAsGiven() {
        let preferences = NotificationPreferences(hour: 18, minute: 30)
        XCTAssertEqual(preferences.hour, 18)
        XCTAssertEqual(preferences.minute, 30)
    }

    // MARK: - Clamp de valores

    func testHourIsClampedToValidRange() {
        let preferences = NotificationPreferences(hour: 25, minute: 0)
        XCTAssertEqual(preferences.hour, 23)
    }

    func testNegativeHourIsClampedToZero() {
        let preferences = NotificationPreferences(hour: -1, minute: 0)
        XCTAssertEqual(preferences.hour, 0)
    }

    func testMinuteIsClampedToValidRange() {
        let preferences = NotificationPreferences(hour: 10, minute: 70)
        XCTAssertEqual(preferences.minute, 59)
    }

    func testNegativeMinuteIsClampedToZero() {
        let preferences = NotificationPreferences(hour: 10, minute: -5)
        XCTAssertEqual(preferences.minute, 0)
    }

    // MARK: - Persistencia en UserDefaults

    func testStoredPreferencesReturnDefaultsWhenNothingSaved() {
        // Usar una clave única para no afectar otros tests
        // NotificationPreferences.stored() lee de UserDefaults estándar
        // Si no hay nada guardado, devuelve 10:00 por defecto
        let preferences = NotificationPreferences.stored()
        XCTAssertEqual(preferences.hour, 10)
        XCTAssertEqual(preferences.minute, 0)
    }

    func testStoreAndRetrievePreferences() {
        let preferences = NotificationPreferences(hour: 15, minute: 45)
        preferences.store()

        let retrieved = NotificationPreferences.stored()
        XCTAssertEqual(retrieved.hour, 15)
        XCTAssertEqual(retrieved.minute, 45)

        // Limpiar para no afectar otros tests
        NotificationPreferences(hour: 10, minute: 0).store()
    }

    // MARK: - Equatable

    func testPreferencesAreEquatable() {
        let a = NotificationPreferences(hour: 10, minute: 0)
        let b = NotificationPreferences(hour: 10, minute: 0)
        let c = NotificationPreferences(hour: 11, minute: 0)

        XCTAssertEqual(a, b)
        XCTAssertNotEqual(a, c)
    }
}
