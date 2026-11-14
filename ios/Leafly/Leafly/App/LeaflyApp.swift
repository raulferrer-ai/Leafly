//
//  LeaflyApp.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI
import SwiftData

@main
struct LeaflyApp: App {
    private let modelContainer: ModelContainer
    private let plantRepository: any PlantRepository
    private let reminders: any WateringReminderScheduling
    private let settingsViewModel: SettingsViewModel

    init() {
        let container = LeaflyModelContainer.make()
        modelContainer = container
        plantRepository = SwiftDataPlantRepository(container: container)
        reminders = WateringReminderService()
        settingsViewModel = SettingsViewModel(
            center: UserNotificationCenterAdapter(),
            reminders: reminders
        )

        #if DEBUG
        UITestSeed.seedPhotoHistoryIfRequested(in: container)
        #endif
    }

    var body: some Scene {
        WindowGroup {
            RootView(
                repository: plantRepository,
                reminders: reminders,
                settingsViewModel: settingsViewModel
            )
            .tint(LeaflyColor.primary)
        }
        .modelContainer(modelContainer)
    }
}
