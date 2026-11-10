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

    init() {
        let container = LeaflyModelContainer.make()
        modelContainer = container
        plantRepository = SwiftDataPlantRepository(container: container)
        reminders = WateringReminderService()
    }

    var body: some Scene {
        WindowGroup {
            RootView(repository: plantRepository, reminders: reminders)
                .tint(LeaflyColor.primary)
        }
        .modelContainer(modelContainer)
    }
}
