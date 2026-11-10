//
//  PlantsHomeView.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Lista de todas las plantas, con su estado de riego de un vistazo.
struct PlantsHomeView: View {
    private let repository: any PlantRepository
    private let reminders: any WateringReminderScheduling

    @State private var viewModel: PlantsViewModel
    @State private var isShowingAddPlant = false

    init(
        repository: any PlantRepository,
        reminders: any WateringReminderScheduling
    ) {
        self.repository = repository
        self.reminders = reminders
        _viewModel = State(
            initialValue: PlantsViewModel(
                repository: repository,
                reminders: reminders
            )
        )
    }

    var body: some View {
        NavigationStack {
            LeaflyScreen {
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: LeaflySpacing.lg) {
                        header
                        content
                    }
                    .frame(maxWidth: LeaflySpacing.contentMaxWidth)
                    .padding(.horizontal, LeaflySpacing.screenHorizontal)
                    .padding(.top, LeaflySpacing.xxl)
                    .padding(.bottom, LeaflySpacing.xl)
                    .frame(maxWidth: .infinity)
                }
                .refreshable {
                    await viewModel.load()
                }
            }
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: Plant.self) { plant in
                PlantDetailView(
                    plant: plant,
                    repository: repository,
                    reminders: reminders,
                    onPlantUpdated: { updatedPlant in
                        await viewModel.replace(updatedPlant)
                    }
                )
            }
            .sheet(isPresented: $isShowingAddPlant) {
                addPlantSheet
            }
            .task {
                await viewModel.load()
            }
        }
    }

    // MARK: - Cabecera

    private var header: some View {
        HStack(alignment: .top, spacing: LeaflySpacing.md) {
            VStack(alignment: .leading, spacing: LeaflySpacing.xs) {
                Text("TU JARDÍN")
                    .font(LeaflyFont.caption)
                    .tracking(1.4)
                    .foregroundStyle(Color.white.opacity(0.75))

                Text("Mis plantas")
                    .font(LeaflyFont.display)
                    .foregroundStyle(Color.white)

                Text(plantsSubtitle)
                    .font(LeaflyFont.body)
                    .foregroundStyle(Color.white.opacity(0.88))
            }

            Spacer(minLength: 0)

            Button {
                isShowingAddPlant = true
            } label: {
                Image(systemName: "plus")
            }
            .buttonStyle(LeaflyCircularButtonStyle())
            .accessibilityLabel("Añadir una planta")
            .accessibilityIdentifier("add-plant-button")
        }
    }

    private var plantsSubtitle: String {
        switch viewModel.plants.count {
        case 0: "Un lugar tranquilo para tus plantas."
        case 1: "Una planta a tu cuidado."
        default: "\(viewModel.plants.count) plantas a tu cuidado."
        }
    }

    // MARK: - Contenido

    @ViewBuilder
    private var content: some View {
        if viewModel.isLoading && viewModel.plants.isEmpty {
            loadingView
        } else if let errorMessage = viewModel.errorMessage {
            errorCard(message: errorMessage)
        } else if viewModel.plants.isEmpty {
            emptyState
        } else {
            plantsList
        }
    }

    private var loadingView: some View {
        HStack {
            Spacer()
            ProgressView()
                .tint(.white)
                .padding(.top, LeaflySpacing.xl)
            Spacer()
        }
        .accessibilityLabel("Cargando tus plantas")
    }

    private var plantsList: some View {
        VStack(spacing: LeaflySpacing.sm) {
            ForEach(viewModel.plants) { plant in
                NavigationLink(value: plant) {
                    PlantCard(plant: plant)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("plant-card-\(plant.name)")
                .accessibilityHint("Abre el detalle de la planta")
            }
        }
    }

    private func errorCard(message: String) -> some View {
        LeaflyCard {
            VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
                Label("No hemos podido cargar tus plantas", systemImage: "exclamationmark.circle.fill")
                    .font(LeaflyFont.headline)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text(message)
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)

                Button("Reintentar") {
                    Task { await viewModel.load() }
                }
                .buttonStyle(LeaflyQuietButtonStyle())
            }
        }
    }

    private var emptyState: some View {
        LeaflyCard {
            VStack(spacing: LeaflySpacing.md) {
                ZStack {
                    Circle()
                        .fill(LeaflyColor.primarySoft)
                        .frame(width: 64, height: 64)

                    Image(systemName: "leaf")
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundStyle(LeaflyColor.secondary)
                }
                .accessibilityHidden(true)

                Text("Tu jardín está esperando")
                    .font(LeaflyFont.title3)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text("Añade una planta para empezar a cuidarla con calma.")
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)

                Button("Añadir mi primera planta") {
                    isShowingAddPlant = true
                }
                .buttonStyle(LeaflyPrimaryButtonStyle())
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: - Añadir planta

    private var addPlantSheet: some View {
        AddPlantView(
            repository: repository,
            onCancel: { isShowingAddPlant = false },
            onSaved: {
                isShowingAddPlant = false
                Task { await viewModel.load() }
            },
            mode: .anotherPlant
        )
        .presentationDetents([.large])
        .presentationDragIndicator(.hidden)
        .presentationCornerRadius(LeaflyRadius.card)
    }
}

// MARK: - Card de planta

private struct PlantCard: View {
    let plant: Plant

    private var status: WateringStatus {
        plant.wateringStatus()
    }

    private var summary: String {
        plant.wateringSummary()
    }

    var body: some View {
        LeaflyCard(padding: LeaflySpacing.md) {
            HStack(alignment: .center, spacing: LeaflySpacing.md) {
                avatar

                VStack(alignment: .leading, spacing: 4) {
                    Text(plant.name)
                        .font(LeaflyFont.title3)
                        .foregroundStyle(LeaflyColor.textPrimary)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)

                    if let type = plant.type {
                        Text(type.displayName)
                            .font(LeaflyFont.footnote)
                            .foregroundStyle(LeaflyColor.textSecondary)
                    }

                    Text(summary)
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textTertiary)
                }

                Spacer(minLength: LeaflySpacing.xs)

                VStack(alignment: .trailing, spacing: LeaflySpacing.xs) {
                    LeaflyStatusBadge(
                        title: status.title,
                        color: status.color,
                        systemImage: status.symbolName
                    )

                    Image(systemName: "chevron.right")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(LeaflyColor.textTertiary)
                        .accessibilityHidden(true)
                }
            }
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(plant.name), \(status.title), \(summary)")
    }

    private var avatar: some View {
        ZStack {
            Circle()
                .fill(LeaflyColor.primarySoft)
                .frame(width: 58, height: 58)

            Image(systemName: plant.type?.symbolName ?? "leaf.fill")
                .font(.system(size: 22, weight: .semibold))
                .foregroundStyle(LeaflyColor.secondary)
        }
        .accessibilityHidden(true)
    }
}

#Preview("Mis plantas") {
    PlantsHomeView(
        repository: PreviewPlantsRepository(),
        reminders: NoopWateringReminders()
    )
}

// MARK: - Preview

@MainActor
private final class PreviewPlantsRepository: PlantRepository {
    func fetchPlants() async throws -> [Plant] {
        [
            Plant(name: "Monstera", type: .monstera),
            Plant(name: "Ficus", type: .ficus, lastWateredAt: .now.addingTimeInterval(-6 * 86_400)),
            Plant(name: "La de la abuela", lastWateredAt: .now.addingTimeInterval(-10 * 86_400)),
        ]
    }

    func add(_ plant: Plant) async throws {}
    func update(_ plant: Plant) async throws {}
}
