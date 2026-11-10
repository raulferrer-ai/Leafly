//
//  WateringStatus+Style.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import SwiftUI

/// Traducción del estado de riego al lenguaje visual y cercano de la app.
extension WateringStatus {

    var title: String {
        switch self {
        case .healthy: "Al día"
        case .needsWaterSoon: "Riega pronto"
        case .needsWaterNow: "Necesita agua"
        }
    }

    var color: Color {
        switch self {
        case .healthy: LeaflyColor.healthy
        case .needsWaterSoon: LeaflyColor.needsWaterSoon
        case .needsWaterNow: LeaflyColor.needsWaterNow
        }
    }

    var symbolName: String {
        switch self {
        case .healthy: "checkmark.circle.fill"
        case .needsWaterSoon: "drop.circle.fill"
        case .needsWaterNow: "exclamationmark.circle.fill"
        }
    }
}

extension Plant {

    /// Texto calmado con la última vez que se regó la planta.
    func wateringSummary(on date: Date = .now, calendar: Calendar = .current) -> String {
        let days = daysSinceWatering(on: date, calendar: calendar)

        guard lastWateredAt != nil else {
            switch days {
            case 0:
                return "Añadida hoy"
            case 1:
                return "Añadida ayer"
            default:
                let formattedDate = createdAt.formatted(
                    .dateTime
                        .day()
                        .month(.abbreviated)
                        .locale(Locale(identifier: "es_ES"))
                )
                return "Añadida el \(formattedDate)"
            }
        }

        switch days {
        case 0: return "Regada hoy"
        case 1: return "Regada ayer"
        default: return "Regada hace \(days) días"
        }
    }
}
