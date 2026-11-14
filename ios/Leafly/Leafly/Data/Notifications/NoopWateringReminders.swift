//
//  NoopWateringReminders.swift
//  Leafly
//
//  Created by RaulF on 08/11/2026.
//

import Foundation

/// Implementación vacía para previews y contextos donde no se quieren tocar
/// las notificaciones del sistema.
@MainActor
struct NoopWateringReminders: WateringReminderScheduling {
    func sync(
        plants: [Plant],
        preferredHour: Int,
        preferredMinute: Int
    ) async {}
    func cancelReminder(for plantID: UUID) async {}
}
