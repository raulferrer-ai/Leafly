//
//  LeaflyModelContainer.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation
import SwiftData

/// Crea el contenedor local de Leafly.
enum LeaflyModelContainer {

    /// Contenedor de datos de la app.
    ///
    /// - En ejecución normal usa disco.
    /// - En tests unitarios (donde el host lanza la app) usa memoria para no
    ///   interferir con el almacén real.
    /// - En UI tests se puede forzar memoria con `-useInMemoryStoreForUITests`.
    static func make() -> ModelContainer {
        let arguments = ProcessInfo.processInfo.arguments
        let environment = ProcessInfo.processInfo.environment

        let isRunningUnitTests = environment["XCTestConfigurationFilePath"] != nil
        let wantsInMemoryStore = arguments.contains("-useInMemoryStoreForUITests")
        let usesInMemoryStore = isRunningUnitTests || wantsInMemoryStore

        do {
            let configuration = ModelConfiguration(isStoredInMemoryOnly: usesInMemoryStore)
            return try ModelContainer(for: PlantEntity.self, configurations: configuration)
        } catch {
            assertionFailure("No se pudo abrir el almacén local: \(error)")

            do {
                let fallback = ModelConfiguration(isStoredInMemoryOnly: true)
                return try ModelContainer(for: PlantEntity.self, configurations: fallback)
            } catch {
                fatalError("No se pudo crear el almacén de datos de Leafly: \(error)")
            }
        }
    }
}
