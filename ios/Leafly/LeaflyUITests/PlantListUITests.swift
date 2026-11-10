//
//  PlantListUITests.swift
//  LeaflyUITests
//
//  Created by RaulF on 07/11/2026.
//

import XCTest

final class PlantListUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
    }

    func testListShowsPlantAndOpensDetail() throws {
        let app = launchAppWithFirstPlant(named: "Monstera")

        let card = app.buttons["plant-card-Monstera"]
        XCTAssertTrue(card.waitForExistence(timeout: 10))
        XCTAssertTrue(card.label.contains("Al día"))

        card.tap()

        XCTAssertTrue(app.staticTexts["plant-detail-title"].waitForExistence(timeout: 5))
        XCTAssertEqual(app.staticTexts["plant-detail-title"].label, "Monstera")

        app.buttons["Volver a mis plantas"].tap()

        XCTAssertTrue(app.staticTexts["Mis plantas"].waitForExistence(timeout: 5))
    }

    func testMarkingAsWateredUpdatesDetailAndList() throws {
        let app = launchAppWithFirstPlant(named: "Monstera")

        let card = app.buttons["plant-card-Monstera"]
        XCTAssertTrue(card.waitForExistence(timeout: 10))
        XCTAssertTrue(card.label.contains("Añadida hoy"))

        card.tap()

        XCTAssertTrue(app.staticTexts["plant-detail-title"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Añadida hoy"].waitForExistence(timeout: 5))

        let waterButton = app.buttons["water-plant-button"]
        XCTAssertTrue(waterButton.waitForExistence(timeout: 5))
        waterButton.tap()

        XCTAssertTrue(app.staticTexts["Regada hoy"].waitForExistence(timeout: 5))

        app.buttons["Volver a mis plantas"].tap()

        let updatedCard = app.buttons["plant-card-Monstera"]
        XCTAssertTrue(updatedCard.waitForExistence(timeout: 5))
        XCTAssertTrue(updatedCard.label.contains("Regada hoy"))
        XCTAssertTrue(updatedCard.label.contains("Al día"))
    }

    func testAddingAnotherPlantFromTheList() throws {
        let app = launchAppWithFirstPlant(named: "Monstera")

        let addButton = app.buttons["add-plant-button"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 10))
        addButton.tap()

        XCTAssertTrue(app.staticTexts["Añade una planta"].waitForExistence(timeout: 5))

        let nameField = app.textFields["Por ejemplo, Monstera"]
        XCTAssertTrue(nameField.waitForExistence(timeout: 5))
        nameField.tap()
        nameField.typeText("Ficus")

        app.buttons["Guardar planta"].tap()

        XCTAssertTrue(app.buttons["plant-card-Ficus"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.buttons["plant-card-Monstera"].exists)
    }

    func testCancellingAddFromListKeepsOnlyExistingPlants() throws {
        let app = launchAppWithFirstPlant(named: "Monstera")

        let addButton = app.buttons["add-plant-button"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 10))
        addButton.tap()

        XCTAssertTrue(app.staticTexts["Añade una planta"].waitForExistence(timeout: 5))
        app.buttons["Cancelar y volver a mis plantas"].tap()

        XCTAssertTrue(app.buttons["plant-card-Monstera"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Añade una planta"].waitForNonExistence(timeout: 5))
    }

    // MARK: - Helpers

    /// Arranca la app limpia y crea la primera planta desde el onboarding.
    private func launchAppWithFirstPlant(named name: String) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments.append("-useInMemoryStoreForUITests")
        app.launch()

        let startButton = app.buttons["Añadir mi primera planta"]
        XCTAssertTrue(startButton.waitForExistence(timeout: 10))
        startButton.tap()

        let nameField = app.textFields["Por ejemplo, Monstera"]
        XCTAssertTrue(nameField.waitForExistence(timeout: 5))
        nameField.tap()
        nameField.typeText(name)

        app.buttons["Guardar planta"].tap()

        XCTAssertTrue(app.staticTexts["Mis plantas"].waitForExistence(timeout: 10))
        return app
    }
}
