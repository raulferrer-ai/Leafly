//
//  PlantEntity.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation
import SwiftData

/// Representación persistente de una planta.
@Model
final class PlantEntity {
    @Attribute(.unique) var id: UUID
    var name: String
    var typeRawValue: String?
    var createdAt: Date
    var lastWateredAt: Date?
    /// Foto en JPEG. Se guarda fuera de la base de datos para mantenerla ligera.
    @Attribute(.externalStorage) var photoData: Data?

    init(
        id: UUID,
        name: String,
        typeRawValue: String?,
        createdAt: Date,
        lastWateredAt: Date?,
        photoData: Data? = nil
    ) {
        self.id = id
        self.name = name
        self.typeRawValue = typeRawValue
        self.createdAt = createdAt
        self.lastWateredAt = lastWateredAt
        self.photoData = photoData
    }
}

extension PlantEntity {

    convenience init(plant: Plant) {
        self.init(
            id: plant.id,
            name: plant.name,
            typeRawValue: plant.type?.rawValue,
            createdAt: plant.createdAt,
            lastWateredAt: plant.lastWateredAt,
            photoData: plant.photoData
        )
    }

    /// Tipo de planta ya traducido al dominio, si se eligió uno.
    var plantType: PlantType? {
        typeRawValue.flatMap(PlantType.init(rawValue:))
    }

    /// Traduce la entidad persistente al modelo de dominio.
    var domainModel: Plant {
        Plant(
            id: id,
            name: name,
            type: plantType,
            createdAt: createdAt,
            lastWateredAt: lastWateredAt,
            photoData: photoData
        )
    }
}
