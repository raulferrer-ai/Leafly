//
//  OnboardingUITests.swift
//  LeaflyUITests
//
//  Created by RaulF on 06/11/2026.
//

import XCTest

final class OnboardingUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
    }

    func testAddingTheFirstPlant() throws {
        let app = XCUIApplication()
        app.launchArguments.append("-useInMemoryStoreForUITests")
        app.launch()

        let addButton = app.buttons["Añadir mi primera planta"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 10))
        addButton.tap()

        let nameField = app.textFields["Por ejemplo, Monstera"]
        XCTAssertTrue(nameField.waitForExistence(timeout: 5))
        nameField.tap()
        nameField.typeText("Monstera")

        let saveButton = app.buttons["Guardar planta"]
        XCTAssertTrue(saveButton.waitForExistence(timeout: 5))
        saveButton.tap()

        XCTAssertTrue(app.staticTexts["Mis plantas"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.staticTexts["Monstera"].exists)
    }

    func testCancellingReturnsToWelcomeWithoutSaving() throws {
        let app = XCUIApplication()
        app.launchArguments.append("-useInMemoryStoreForUITests")
        app.launch()

        let addButton = app.buttons["Añadir mi primera planta"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 10))
        addButton.tap()

        XCTAssertTrue(app.staticTexts["Añade tu primera planta"].waitForExistence(timeout: 5))
        app.buttons["Cancelar y volver a la bienvenida"].tap()

        XCTAssertTrue(addButton.waitForExistence(timeout: 5))
    }

    func testSavingWithoutNameShowsValidationMessage() throws {
        let app = XCUIApplication()
        app.launchArguments.append("-useInMemoryStoreForUITests")
        app.launch()

        let addButton = app.buttons["Añadir mi primera planta"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 10))
        addButton.tap()

        XCTAssertTrue(app.staticTexts["Añade tu primera planta"].waitForExistence(timeout: 5))
        app.buttons["Guardar planta"].tap()

        XCTAssertTrue(
            app.staticTexts["Ponle un nombre a tu planta para poder guardarla."]
                .waitForExistence(timeout: 5)
        )
        XCTAssertTrue(app.staticTexts["Añade tu primera planta"].exists)
    }
}
