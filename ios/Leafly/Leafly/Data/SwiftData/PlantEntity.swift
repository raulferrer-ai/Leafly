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

    init(id: UUID, name: String, typeRawValue: String?, createdAt: Date) {
        self.id = id
        self.name = name
        self.typeRawValue = typeRawValue
        self.createdAt = createdAt
    }
}

extension PlantEntity {

    convenience init(plant: Plant) {
        self.init(
            id: plant.id,
            name: plant.name,
            typeRawValue: plant.type?.rawValue,
            createdAt: plant.createdAt
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
            createdAt: createdAt
        )
    }
}
