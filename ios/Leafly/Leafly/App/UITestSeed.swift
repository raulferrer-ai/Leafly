//
//  UITestSeed.swift
//  Leafly
//
//  Created by RaulF on 10/11/2026.
//

#if DEBUG

import Foundation
import SwiftData
import UIKit

/// Datos de ejemplo para los UI tests del historial de fotos.
///
/// Solo existe en compilaciones DEBUG y se activa con un argumento de
/// lanzamiento. Así se puede recorrer el historial sin depender de la cámara
/// o de la galería del simulador.
@MainActor
enum UITestSeed {

    static let photoHistoryArgument = "-seedPhotoHistoryForUITests"

    /// Siembra una planta con dos fotos (hoy y hace tres días) cuando la app
    /// se lanza con el argumento correspondiente y el almacén está vacío.
    static func seedPhotoHistoryIfRequested(in container: ModelContainer) {
        guard ProcessInfo.processInfo.arguments.contains(photoHistoryArgument) else {
            return
        }

        let context = container.mainContext
        let existingPlants = (try? context.fetch(FetchDescriptor<PlantEntity>())) ?? []
        guard existingPlants.isEmpty else { return }

        let calendar = Calendar.current
        let today = calendar.startOfDay(for: .now)
        let threeDaysAgo = calendar.date(byAdding: .day, value: -3, to: today) ?? today

        let plant = PlantEntity(
            id: UUID(),
            name: "Monstera",
            typeRawValue: PlantType.monstera.rawValue,
            createdAt: threeDaysAgo,
            lastWateredAt: today,
            photoData: nil
        )
        context.insert(plant)

        let newest = PlantPhotoEntity(
            id: UUID(),
            addedAt: today,
            photoData: sampleJPEG(tint: .systemGreen),
            plant: plant
        )
        let older = PlantPhotoEntity(
            id: UUID(),
            addedAt: threeDaysAgo,
            photoData: sampleJPEG(tint: .systemTeal),
            plant: plant
        )
        context.insert(newest)
        context.insert(older)

        // La foto más reciente también es la imagen principal de la planta.
        plant.photoData = newest.photoData

        try? context.save()
    }

    private static func sampleJPEG(tint: UIColor) -> Data {
        let size = CGSize(width: 300, height: 300)
        let renderer = UIGraphicsImageRenderer(size: size)
        let image = renderer.image { context in
            tint.setFill()
            context.fill(CGRect(origin: .zero, size: size))
        }

        return image.jpegData(compressionQuality: 0.8) ?? Data()
    }
}

#endif
