//
//  WateringReminderPlanner.swift
//  Leafly
//
//  Created by RaulF on 08/11/2026.
//

import Foundation

/// Decide qué recordatorios deben existir a partir del estado de riego.
///
/// Es lógica pura: recibe plantas y una fecha, y devuelve los recordatorios
/// pendientes. Así se puede probar sin tocar el sistema de notificaciones.
nonisolated enum WateringReminderPlanner {

    /// Hora local a la que se entregan los recordatorios.
    static let reminderHour = 10

    /// Margen mínimo para no enviar una notificación casi inmediata.
    static let minimumLeadTime: TimeInterval = 60 * 60

    /// Plantas "Riega pronto" o "Necesita agua" generan recordatorio.
    /// Las que están "Al día" no se notifican.
    static func reminders(
        for plants: [Plant],
        now: Date = .now,
        calendar: Calendar = .current
    ) -> [WateringReminder] {
        let fireDate = nextReminderDate(after: now, calendar: calendar)

        return plants.compactMap { plant in
            let status = plant.wateringStatus(on: now, calendar: calendar)
            guard status != .healthy else { return nil }

            return WateringReminder(
                plantID: plant.id,
                plantName: plant.name,
                status: status,
                fireDate: fireDate
            )
        }
    }

    /// Próxima mañana tranquila (10:00), con al menos una hora de margen.
    static func nextReminderDate(after now: Date, calendar: Calendar = .current) -> Date {
        let startOfDay = calendar.startOfDay(for: now)

        guard let todaySlot = calendar.date(
            byAdding: .hour,
            value: reminderHour,
            to: startOfDay
        ) else {
            return now.addingTimeInterval(minimumLeadTime)
        }

        if todaySlot.timeIntervalSince(now) >= minimumLeadTime {
            return todaySlot
        }

        return calendar.date(byAdding: .day, value: 1, to: todaySlot)
            ?? now.addingTimeInterval(minimumLeadTime)
    }
}
