//
//  Plant.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation

/// Modelo de dominio de una planta.
/// Independiente de SwiftData para mantener el dominio limpio y testeable.
nonisolated struct Plant: Identifiable, Hashable, Sendable {
    let id: UUID
    var name: String
    var type: PlantType?
    var createdAt: Date
    /// Último riego registrado. `nil` significa que aún no se ha regado.
    var lastWateredAt: Date?
    /// Foto de la planta ya procesada (JPEG reducido). `nil` significa sin foto.
    var photoData: Data?

    init(
        id: UUID = UUID(),
        name: String,
        type: PlantType? = nil,
        createdAt: Date = .now,
        lastWateredAt: Date? = nil,
        photoData: Data? = nil
    ) {
        self.id = id
        self.name = name
        self.type = type
        self.createdAt = createdAt
        self.lastWateredAt = lastWateredAt
        self.photoData = photoData
    }
}

extension Plant {

    /// Fecha de referencia para el riego: el último riego o, si no hay, el día en que se añadió.
    nonisolated var wateringReferenceDate: Date {
        lastWateredAt ?? createdAt
    }

    /// Días completos transcurridos desde la fecha de referencia del riego.
    nonisolated func daysSinceWatering(on date: Date = .now, calendar: Calendar = .current) -> Int {
        let start = calendar.startOfDay(for: wateringReferenceDate)
        let end = calendar.startOfDay(for: date)
        let days = calendar.dateComponents([.day], from: start, to: end).day ?? 0
        return max(0, days)
    }

    /// Estado de riego calculado para una fecha concreta. Testeable e independiente de la UI.
    nonisolated func wateringStatus(on date: Date = .now, calendar: Calendar = .current) -> WateringStatus {
        WateringStatus(daysSinceWatering: daysSinceWatering(on: date, calendar: calendar))
    }
}
