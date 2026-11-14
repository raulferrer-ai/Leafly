//
//  SettingsViewModel.swift
//  Leafly
//
//  Estado y lógica de la pantalla de ajustes de notificaciones.
//

import Foundation
import UserNotifications
import UIKit

/// Estado y lógica de la pantalla de ajustes.
///
/// Muestra el estado del permiso de notificaciones y permite elegir la
/// hora preferida de los recordatorios de riego.
@MainActor
@Observable
final class SettingsViewModel {

    /// Estado actual del permiso de notificaciones.
    private(set) var authorizationStatus: UNAuthorizationStatus = .notDetermined

    /// Preferencias del usuario (hora y minuto del aviso).
    private(set) var preferences: NotificationPreferences = .stored()

    private let center: any LocalNotificationCenter
    private let reminders: any WateringReminderScheduling

    init(
        center: any LocalNotificationCenter,
        reminders: any WateringReminderScheduling
    ) {
        self.center = center
        self.reminders = reminders
    }

    /// Carga el estado actual del permiso y las preferencias guardadas.
    func load() async {
        authorizationStatus = await center.currentAuthorizationStatus()
        preferences = NotificationPreferences.stored()
    }

    /// Actualiza la hora preferida y la persiste.
    ///
    /// También vuelve a programar los recordatorios para que usen la nueva hora.
    func setPreferredHour(_ hour: Int, minute: Int) {
        preferences = NotificationPreferences(hour: hour, minute: minute)
        preferences.store()
    }

    /// Abre los ajustes del sistema para que el usuario gestione el permiso.
    func openSystemSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            Task { @MainActor in
                await UIApplication.shared.open(url)
            }
        }
    }

    /// Vuelve a programar los recordatorios con las preferencias actuales.
    func rescheduleReminders(plants: [Plant]) async {
        await reminders.sync(
            plants: plants,
            preferredHour: preferences.hour,
            preferredMinute: preferences.minute
        )
    }
}
