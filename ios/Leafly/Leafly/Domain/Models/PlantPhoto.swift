//
//  PlantPhoto.swift
//  Leafly
//
//  Created by RaulF on 10/11/2026.
//

import Foundation

/// Una foto del historial de una planta.
///
/// El historial crece con el tiempo: cada foto guarda su fecha para poder
/// recorrer el crecimiento de la planta de la más reciente a la más antigua.
/// `data` ya viene procesada (JPEG reducido) por `PlantPhotoProcessor`.
nonisolated struct PlantPhoto: Identifiable, Hashable, Sendable {
    let id: UUID
    let plantID: UUID
    /// Momento en el que se añadió la foto al historial.
    let addedAt: Date
    /// Foto ya procesada, lista para mostrarse y persistirse.
    let data: Data

    init(
        id: UUID = UUID(),
        plantID: UUID,
        addedAt: Date = .now,
        data: Data
    ) {
        self.id = id
        self.plantID = plantID
        self.addedAt = addedAt
        self.data = data
    }
}
