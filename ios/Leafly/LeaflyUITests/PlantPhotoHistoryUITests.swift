//
//  PlantPhotoHistoryUITests.swift
//  LeaflyUITests
//
//  Created by RaulF on 10/11/2026.
//

import XCTest

final class PlantPhotoHistoryUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
    }

    func testEmptyHistoryOffersAFirstPhotoAndCancelKeepsItEmpty() throws {
        let app = launchAppWithFirstPlant(named: "Monstera")

        let card = app.buttons["plant-card-Monstera"]
        XCTAssertTrue(card.waitForExistence(timeout: 10))
        card.tap()

        XCTAssertTrue(app.staticTexts["photo-history-title"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["photo-history-empty-title"].waitForExistence(timeout: 5))

        let addButton = app.buttons["add-first-photo-button"]
        XCTAssertTrue(addButton.waitForExistence(timeout: 5))
        addButton.tap()

        let galleryOption = app.buttons["Elegir de la galería"]
        XCTAssertTrue(galleryOption.waitForExistence(timeout: 5))

        cancelAddPhotoDialog(in: app)

        XCTAssertTrue(app.staticTexts["photo-history-empty-title"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["add-photo-button"].exists)
    }

    func testHistoryShowsPhotosFromNewestToOldestWithDates() throws {
        let app = launchAppWithSeededPhotoHistory()

        let card = app.buttons["plant-card-Monstera"]
        XCTAssertTrue(card.waitForExistence(timeout: 10))
        card.tap()

        XCTAssertTrue(app.staticTexts["photo-history-title"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.staticTexts["photo-history-empty-title"].exists)
        XCTAssertTrue(app.buttons["add-photo-button"].waitForExistence(timeout: 5))

        let newestItem = app.otherElements["photo-history-item-0"]
        let olderItem = app.otherElements["photo-history-item-1"]

        XCTAssertTrue(newestItem.waitForExistence(timeout: 5))
        XCTAssertTrue(olderItem.exists)
        XCTAssertTrue(newestItem.label.contains("Hoy"))
        XCTAssertFalse(olderItem.label.contains("Hoy"))

        // La más reciente aparece primero, a la izquierda.
        XCTAssertLessThan(newestItem.frame.minX, olderItem.frame.minX)
    }

    // MARK: - Helpers

    /// Cierra el diálogo de "Añadir una foto" sin elegir ninguna opción.
    ///
    /// En iOS 26 el diálogo se presenta como popover y se cancela tocando
    /// fuera; en presentaciones con hoja de acciones existe un "Cancelar".
    private func cancelAddPhotoDialog(in app: XCUIApplication) {
        let dismissRegion = app.otherElements["PopoverDismissRegion"]
        if dismissRegion.waitForExistence(timeout: 2) {
            dismissRegion
                .coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.1))
                .tap()
            return
        }

        let cancelButton = app.buttons["Cancelar"]
        if cancelButton.waitForExistence(timeout: 2) {
            cancelButton.tap()
            return
        }

        // Último recurso: tocar fuera de la zona donde aparece el diálogo.
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.1)).tap()
    }

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

    /// Arranca la app con una planta y dos fotos ya guardadas.
    private func launchAppWithSeededPhotoHistory() -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += [
            "-useInMemoryStoreForUITests",
            "-seedPhotoHistoryForUITests"
        ]
        app.launch()

        XCTAssertTrue(app.staticTexts["Mis plantas"].waitForExistence(timeout: 10))
        return app
    }
}
