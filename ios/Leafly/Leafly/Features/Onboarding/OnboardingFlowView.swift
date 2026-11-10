//
//  OnboardingFlowView.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Flujo de bienvenida + primera planta.
struct OnboardingFlowView: View {
    private let repository: any PlantRepository

    @State private var step: Step = .welcome

    private enum Step {
        case welcome
        case addPlant
    }

    init(repository: any PlantRepository) {
        self.repository = repository
    }

    var body: some View {
        ZStack {
            switch step {
            case .welcome:
                WelcomeView(onAddPlant: showAddPlant)
                    .transition(.opacity)
            case .addPlant:
                AddPlantView(repository: repository, onCancel: showWelcome)
                    .transition(.move(edge: .trailing).combined(with: .opacity))
            }
        }
    }

    private func showAddPlant() {
        withAnimation(.smooth(duration: 0.35)) {
            step = .addPlant
        }
    }

    private func showWelcome() {
        withAnimation(.smooth(duration: 0.35)) {
            step = .welcome
        }
    }
}
