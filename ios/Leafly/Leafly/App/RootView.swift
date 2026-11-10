//
//  RootView.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI
import SwiftData

/// Decide qué mostrar al arrancar:
/// sin plantas → onboarding; con plantas → jardín.
struct RootView: View {
    private let repository: any PlantRepository

    @Query private var plants: [PlantEntity]

    init(repository: any PlantRepository) {
        self.repository = repository
    }

    var body: some View {
        ZStack {
            if plants.isEmpty {
                OnboardingFlowView(repository: repository)
                    .transition(.opacity)
            } else {
                PlantsHomeView()
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.45), value: plants.isEmpty)
        .environment(\.locale, Locale(identifier: "es_ES"))
    }
}
