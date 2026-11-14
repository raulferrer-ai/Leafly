//
//  NotificationPreferences.swift
//  Leafly
//
//  Preferencias del usuario para los recordatorios de riego.
//

import Foundation

/// Preferencias de notificación del usuario.
///
/// Se persisten en UserDefaults y se usan para decidir a qué hora
/// se entregan los recordatorios de riego.
nonisolated struct NotificationPreferences: Equatable, Sendable {

    /// Hora local preferida (0–23). Por defecto 10.
    var hour: Int

    /// Minuto local preferido (0–59). Por defecto 0.
    var minute: Int

    init(hour: Int = 10, minute: Int = 0) {
        self.hour = max(0, min(23, hour))
        self.minute = max(0, min(59, minute))
    }

    /// Claves usadas en UserDefaults.
    private enum Keys {
        static let hour = "leafly.notificationPreferences.hour"
        static let minute = "leafly.notificationPreferences.minute"
    }

    /// Lee las preferencias guardadas, o devuelve los valores por defecto.
    static func stored() -> NotificationPreferences {
        let defaults = UserDefaults.standard
        let hour = defaults.integer(forKey: Keys.hour)
        let minute = defaults.integer(forKey: Keys.minute)
        return NotificationPreferences(hour: hour, minute: minute)
    }

    /// Guarda las preferencias en UserDefaults.
    func store() {
        let defaults = UserDefaults.standard
        defaults.set(hour, forKey: Keys.hour)
        defaults.set(minute, forKey: Keys.minute)
    }
}
