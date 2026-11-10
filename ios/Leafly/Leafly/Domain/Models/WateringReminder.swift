//
//  WateringReminder.swift
//  Leafly
//
//  Created by RaulF on 08/11/2026.
//

import Foundation

/// Recordatorio local que se muestra cuando una planta necesita atención.
///
/// Es un valor de dominio: no conoce `UserNotifications`. El adaptador de
/// notificaciones lo traduce a una petición del sistema.
nonisolated struct WateringReminder: Identifiable, Equatable, Sendable {
    let plantID: UUID
    let plantName: String
    let status: WateringStatus
    let fireDate: Date

    /// Identificador estable por planta. Permite cancelar o reemplazar el
    /// recordatorio sin afectar a las demás.
    var id: String {
        Self.identifier(for: plantID)
    }

    static func identifier(for plantID: UUID) -> String {
        "watering-reminder-\(plantID.uuidString)"
    }

    var title: String {
        switch status {
        case .healthy:
            "\(plantName) está al día"
        case .needsWaterSoon:
            "Un poco de agua para \(plantName)"
        case .needsWaterNow:
            "\(plantName) necesita agua"
        }
    }

    var body: String {
        switch status {
        case .healthy:
            "\(plantName) no necesita nada por ahora."
        case .needsWaterSoon:
            "Riega pronto a \(plantName) para que siga creciendo con calma."
        case .needsWaterNow:
            "Un riego ahora ayudará a \(plantName) a volver a estar al día."
        }
    }
}
