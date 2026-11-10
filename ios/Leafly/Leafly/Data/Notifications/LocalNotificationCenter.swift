//
//  LocalNotificationCenter.swift
//  Leafly
//
//  Created by RaulF on 08/11/2026.
//

import Foundation

/// Abstracción mínima de las notificaciones locales.
///
/// Permite probar el servicio de recordatorios con un doble de prueba, sin
/// depender de `UNUserNotificationCenter`.
@MainActor
protocol LocalNotificationCenter {
    /// Pide permiso al sistema. Devuelve `false` si no se concede.
    func requestAuthorization() async -> Bool

    /// Identificadores pendientes que empiezan por `prefix`.
    func pendingIdentifiers(withPrefix prefix: String) async -> [String]

    /// Programa (o reemplaza) un recordatorio.
    func add(_ reminder: WateringReminder) async

    /// Cancela las notificaciones pendientes indicadas.
    func removePending(identifiers: [String]) async
}
