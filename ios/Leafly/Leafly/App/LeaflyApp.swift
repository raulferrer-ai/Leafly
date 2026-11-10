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

    init() {
        let container = LeaflyModelContainer.make()
        modelContainer = container
        plantRepository = SwiftDataPlantRepository(container: container)
    }

    var body: some Scene {
        WindowGroup {
            RootView(repository: plantRepository)
                .tint(LeaflyColor.primary)
        }
        .modelContainer(modelContainer)
    }
}
