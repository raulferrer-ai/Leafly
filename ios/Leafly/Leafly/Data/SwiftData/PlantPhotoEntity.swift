//
//  PlantPhotoEntity.swift
//  Leafly
//
//  Created by RaulF on 10/11/2026.
//

import Foundation
import SwiftData

/// Representación persistente de una foto del historial.
///
/// Cada foto pertenece a una planta. La imagen se guarda fuera de la base de
/// datos para mantenerla ligera, igual que la foto principal.
@Model
final class PlantPhotoEntity {
    @Attribute(.unique) var id: UUID
    var addedAt: Date
    @Attribute(.externalStorage) var photoData: Data
    /// Planta a la que pertenece la foto. SwiftData mantiene el inverso en
    /// `PlantEntity.photos`.
    var plant: PlantEntity?

    init(
        id: UUID,
        addedAt: Date,
        photoData: Data,
        plant: PlantEntity? = nil
    ) {
        self.id = id
        self.addedAt = addedAt
        self.photoData = photoData
        self.plant = plant
    }
}

extension PlantPhotoEntity {

    /// Traduce la entidad persistente al modelo de dominio.
    var domainModel: PlantPhoto? {
        guard let plant else { return nil }

        return PlantPhoto(
            id: id,
            plantID: plant.id,
            addedAt: addedAt,
            data: photoData
        )
    }
}
