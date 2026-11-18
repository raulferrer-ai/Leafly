//
//  WateringLogEntryEntity.swift
//  Leafly
//
//  Created by RaulF on 11/11/2026.
//

import Foundation
import SwiftData

/// Representación persistente de una entrada del historial de riegos.
///
/// Cada entrada pertenece a una planta. Al borrar la planta, sus entradas
/// de riego se borran con ella (cascade).
@Model
final class WateringLogEntryEntity {
    @Attribute(.unique) var id: UUID
    var wateredAt: Date
    /// Planta a la que pertenece la entrada. SwiftData mantiene el inverso en
    /// `PlantEntity.wateringLog`.
    var plant: PlantEntity?

    init(
        id: UUID,
        wateredAt: Date,
        plant: PlantEntity? = nil
    ) {
        self.id = id
        self.wateredAt = wateredAt
        self.plant = plant
    }
}

extension WateringLogEntryEntity {

    /// Traduce la entidad persistente al modelo de dominio.
    var domainModel: WateringLogEntry? {
        guard let plant else { return nil }

        return WateringLogEntry(
            id: id,
            plantID: plant.id,
            wateredAt: wateredAt
        )
    }
}
