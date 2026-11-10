//
//  PlantPhotoDateLabel.swift
//  Leafly
//
//  Created by RaulF on 10/11/2026.
//

import Foundation

/// Texto calmado para la fecha de una foto del historial.
///
/// Las fotos recientes se leen como "Hoy" o "Ayer"; las demás muestran el día
/// y el mes, y añaden el año solo cuando no es el actual.
nonisolated enum PlantPhotoDateLabel {

    static func label(
        for date: Date,
        now: Date = .now,
        calendar: Calendar = .current
    ) -> String {
        if calendar.isDate(date, inSameDayAs: now) {
            return "Hoy"
        }

        if let yesterday = calendar.date(byAdding: .day, value: -1, to: now),
           calendar.isDate(date, inSameDayAs: yesterday) {
            return "Ayer"
        }

        let isCurrentYear = calendar.component(.year, from: date)
            == calendar.component(.year, from: now)

        var style = Date.FormatStyle.dateTime
            .day()
            .month(.abbreviated)
            .locale(Locale(identifier: "es_ES"))

        if !isCurrentYear {
            style = style.year()
        }

        return date.formatted(style)
    }
}
