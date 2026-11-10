//
//  PlantDetailView.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import SwiftUI

/// Detalle de una planta: su información, su estado de riego y la acción
/// principal para marcarla como regada.
struct PlantDetailView: View {
    private let onPlantUpdated: @MainActor (Plant) async -> Void

    @State private var viewModel: PlantDetailViewModel
    @Environment(\.dismiss) private var dismiss

    init(
        plant: Plant,
        repository: any PlantRepository,
        reminders: any WateringReminderScheduling,
        onPlantUpdated: @escaping @MainActor (Plant) async -> Void = { _ in }
    ) {
        self.onPlantUpdated = onPlantUpdated
        _viewModel = State(
            initialValue: PlantDetailViewModel(
                plant: plant,
                repository: repository,
                reminders: reminders
            )
        )
    }

    var body: some View {
        LeaflyScreen {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: LeaflySpacing.lg) {
                    topBar
                    header
                    statusCard
                    waterButton

                    if let errorMessage = viewModel.errorMessage {
                        errorCard(message: errorMessage)
                    }
                }
                .frame(maxWidth: LeaflySpacing.contentMaxWidth)
                .padding(.horizontal, LeaflySpacing.screenHorizontal)
                .padding(.top, LeaflySpacing.sm)
                .padding(.bottom, LeaflySpacing.xl)
                .frame(maxWidth: .infinity)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .navigationBarBackButtonHidden(true)
    }

    // MARK: - Cabecera

    private var topBar: some View {
        HStack {
            Button {
                dismiss()
            } label: {
                Image(systemName: "chevron.left")
            }
            .buttonStyle(LeaflyCircularButtonStyle())
            .accessibilityLabel("Volver a mis plantas")

            Spacer()
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.xs) {
            Text("TU PLANTA")
                .font(LeaflyFont.caption)
                .tracking(1.4)
                .foregroundStyle(Color.white.opacity(0.75))

            Text(viewModel.plant.name)
                .font(LeaflyFont.display)
                .foregroundStyle(Color.white)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityIdentifier("plant-detail-title")

            if let type = viewModel.plant.type {
                Text(type.displayName)
                    .font(LeaflyFont.body)
                    .foregroundStyle(Color.white.opacity(0.88))
                    .accessibilityIdentifier("plant-detail-type")
            }
        }
    }

    // MARK: - Estado de riego

    private var statusCard: some View {
        LeaflyCard(padding: LeaflySpacing.md) {
            HStack(spacing: LeaflySpacing.md) {
                PlantAvatar(plant: viewModel.plant, size: 64)

                VStack(alignment: .leading, spacing: 4) {
                    LeaflyStatusBadge(
                        title: viewModel.status.title,
                        color: viewModel.status.color,
                        systemImage: viewModel.status.symbolName
                    )
                    .accessibilityIdentifier("plant-detail-status")

                    Text(viewModel.summary)
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textSecondary)
                        .accessibilityIdentifier("plant-detail-summary")
                }

                Spacer(minLength: 0)
            }
        }
        .animation(.snappy(duration: 0.25), value: viewModel.plant.lastWateredAt)
    }

    // MARK: - Acción principal

    private var waterButton: some View {
        Button {
            Task {
                if await viewModel.markAsWatered() {
                    await onPlantUpdated(viewModel.plant)
                }
            }
        } label: {
            if viewModel.isSaving {
                ProgressView()
                    .tint(LeaflyColor.onPrimary)
            } else {
                Label("Ya la regué", systemImage: "drop.fill")
            }
        }
        .buttonStyle(LeaflyPrimaryButtonStyle())
        .disabled(viewModel.isSaving)
        .accessibilityIdentifier("water-plant-button")
    }

    private func errorCard(message: String) -> some View {
        LeaflyCard {
            Text(message)
                .font(LeaflyFont.footnote)
                .foregroundStyle(LeaflyColor.textSecondary)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityIdentifier("plant-detail-error")
        }
    }
}

#Preview("Detalle de planta") {
    NavigationStack {
        PlantDetailView(
            plant: Plant(name: "Monstera", type: .monstera),
            repository: PreviewPlantDetailRepository(),
            reminders: NoopWateringReminders()
        )
    }
}

// MARK: - Preview

@MainActor
private final class PreviewPlantDetailRepository: PlantRepository {
    func fetchPlants() async throws -> [Plant] { [] }
    func add(_ plant: Plant) async throws {}
    func update(_ plant: Plant) async throws {}
}
