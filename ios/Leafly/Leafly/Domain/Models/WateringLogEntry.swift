//
//  WateringLogEntry.swift
//  Leafly
//
//  Created by RaulF on 11/11/2026.
//

import Foundation

/// Una entrada del historial de riegos de una planta.
///
/// Cada vez que el usuario marca una planta como regada, se añade una
/// entrada aquí con la fecha del riego. El historial crece con el tiempo y
/// se muestra del más reciente al más antiguo.
nonisolated struct WateringLogEntry: Identifiable, Hashable, Sendable {
    let id: UUID
    let plantID: UUID
    /// Momento en el que se registró el riego.
    let wateredAt: Date

    init(
        id: UUID = UUID(),
        plantID: UUID,
        wateredAt: Date = .now
    ) {
        self.id = id
        self.plantID = plantID
        self.wateredAt = wateredAt
    }
}
