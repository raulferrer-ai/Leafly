//
//  WateringReminderService.swift
//  Leafly
//
//  Created by RaulF on 08/11/2026.
//

import Foundation

/// Mantiene sincronizados los recordatorios de riego con las plantas guardadas.
///
/// Reglas:
/// - Solo programa recordatorios para plantas que necesitan atención.
/// - Cancela los recordatorios que ya no corresponden.
/// - Si el permiso está denegado, no programa nada y no falla.
@MainActor
final class WateringReminderService: WateringReminderScheduling {

    private let center: any LocalNotificationCenter
    private let now: () -> Date
    private let calendar: Calendar

    /// Evita pedir permiso más de una vez por sesión.
    private var didRequestAuthorization = false

    init(
        center: (any LocalNotificationCenter)? = nil,
        now: @escaping () -> Date = Date.init,
        calendar: Calendar = .current
    ) {
        self.center = center ?? UserNotificationCenterAdapter()
        self.now = now
        self.calendar = calendar
    }

    func sync(
        plants: [Plant],
        preferredHour: Int,
        preferredMinute: Int
    ) async {
        let reminders = WateringReminderPlanner.reminders(
            for: plants,
            now: now(),
            calendar: calendar,
            preferredHour: preferredHour,
            preferredMinute: preferredMinute
        )

        await removeStaleReminders(keeping: reminders)

        guard !reminders.isEmpty else { return }
        guard await ensureAuthorization() else { return }

        for reminder in reminders {
            await center.add(reminder)
        }
    }

    func cancelReminder(for plantID: UUID) async {
        await center.removePending(
            identifiers: [WateringReminder.identifier(for: plantID)]
        )
    }

    // MARK: - Interno

    /// Elimina los recordatorios pendientes que ya no representan a ninguna
    /// planta que necesite atención (por ejemplo, tras regarla).
    private func removeStaleReminders(keeping reminders: [WateringReminder]) async {
        let pending = await center.pendingIdentifiers(
            withPrefix: Self.identifierPrefix
        )
        guard !pending.isEmpty else { return }

        let wanted = Set(reminders.map(\.id))
        let stale = pending.filter { !wanted.contains($0) }
        guard !stale.isEmpty else { return }

        await center.removePending(identifiers: stale)
    }

    private func ensureAuthorization() async -> Bool {
        guard !didRequestAuthorization else { return true }
        didRequestAuthorization = true
        return await center.requestAuthorization()
    }

    private static let identifierPrefix = "watering-reminder-"
}
