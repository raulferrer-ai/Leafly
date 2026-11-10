//
//  WateringReminderScheduling.swift
//  Leafly
//
//  Created by RaulF on 08/11/2026.
//

import Foundation

/// Contrato para mantener al día los recordatorios de riego.
///
/// La UI y los ViewModels solo conocen este contrato; el sistema de
/// notificaciones queda detrás de `LocalNotificationCenter`.
@MainActor
protocol WateringReminderScheduling {
    /// Recalcula los recordatorios a partir del estado actual de las plantas.
    ///
    /// No lanza: si no hay permiso, simplemente no programa nada.
    func sync(plants: [Plant]) async

    /// Elimina el recordatorio pendiente de una planta concreta.
    func cancelReminder(for plantID: UUID) async
}
