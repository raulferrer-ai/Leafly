//
//  UserNotificationCenterAdapter.swift
//  Leafly
//
//  Created by RaulF on 08/11/2026.
//

import Foundation
import UserNotifications

/// Implementación real de `LocalNotificationCenter` sobre `UserNotifications`.
///
/// Es un adaptador fino: traduce `WateringReminder` a una petición del sistema
/// y no toma decisiones de negocio.
@MainActor
final class UserNotificationCenterAdapter: LocalNotificationCenter {
    private let center: UNUserNotificationCenter

    init(center: UNUserNotificationCenter = .current()) {
        self.center = center
    }

    func requestAuthorization() async -> Bool {
        let options: UNAuthorizationOptions = [.alert, .sound, .badge]
        return (try? await center.requestAuthorization(options: options)) ?? false
    }

    func pendingIdentifiers(withPrefix prefix: String) async -> [String] {
        await center.pendingNotificationRequests()
            .map(\.identifier)
            .filter { $0.hasPrefix(prefix) }
    }

    func add(_ reminder: WateringReminder) async {
        let content = UNMutableNotificationContent()
        content.title = reminder.title
        content.body = reminder.body
        content.sound = .default
        content.userInfo = ["plantID": reminder.plantID.uuidString]

        var components = Calendar.current.dateComponents(
            [.year, .month, .day, .hour, .minute],
            from: reminder.fireDate
        )
        components.timeZone = .current

        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
        let request = UNNotificationRequest(
            identifier: reminder.id,
            content: content,
            trigger: trigger
        )

        try? await center.add(request)
    }

    func removePending(identifiers: [String]) async {
        center.removePendingNotificationRequests(withIdentifiers: identifiers)
    }
}
