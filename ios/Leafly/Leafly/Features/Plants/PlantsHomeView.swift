//
//  PlantsHomeView.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Lista de todas las plantas, con su estado de riego de un vistazo.
///
/// Permite buscar por nombre y filtrar por estado de riego.
struct PlantsHomeView: View {
    private let repository: any PlantRepository
    private let reminders: any WateringReminderScheduling
    private let settingsViewModel: SettingsViewModel

    @State private var viewModel: PlantsViewModel
    @State private var isShowingAddPlant = false
    @State private var isShowingSettings = false

    init(
        repository: any PlantRepository,
        reminders: any WateringReminderScheduling,
        settingsViewModel: SettingsViewModel
    ) {
        self.repository = repository
        self.reminders = reminders
        self.settingsViewModel = settingsViewModel
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
                        searchAndFilters
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
            .navigationDestination(isPresented: $isShowingSettings) {
                SettingsView(viewModel: settingsViewModel)
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

            Button {
                isShowingSettings = true
            } label: {
                Image(systemName: "gearshape")
            }
            .buttonStyle(LeaflyCircularButtonStyle())
            .accessibilityLabel("Ajustes")
            .accessibilityIdentifier("settings-button")
        }
    }

    private var plantsSubtitle: String {
        let filteredCount = viewModel.filteredPlants.count
        if viewModel.hasActiveFilters {
            return "\(filteredCount) planta\(filteredCount == 1 ? "" : "s") coinciden"
        }
        switch filteredCount {
        case 0: return "Un lugar tranquilo para tus plantas."
        case 1: return "Una planta a tu cuidado."
        default: return "\(filteredCount) plantas a tu cuidado."
        }
    }

    // MARK: - Búsqueda y filtros

    private var searchAndFilters: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.md) {
            searchField
            statusFilterChips
            if viewModel.hasActiveFilters {
                clearFiltersButton
            }
        }
    }

    private var searchField: some View {
        HStack(spacing: LeaflySpacing.sm) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 16, weight: .regular))
                .foregroundStyle(LeaflyColor.textTertiary)

            TextField(
                "Buscar por nombre",
                text: Binding(
                    get: { viewModel.searchText },
                    set: { viewModel.setSearchText($0) }
                )
            )
            .font(LeaflyFont.body)
            .foregroundStyle(LeaflyColor.textPrimary)

            if !viewModel.searchText.isEmpty {
                Button {
                    viewModel.setSearchText(nil)
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 16, weight: .regular))
                        .foregroundStyle(LeaflyColor.textTertiary)
                }
                .accessibilityLabel("Limpiar búsqueda")
            }
        }
        .padding(.horizontal, LeaflySpacing.md)
        .padding(.vertical, LeaflySpacing.sm)
        .background(
            RoundedRectangle(cornerRadius: LeaflyRadius.field, style: .continuous)
                .fill(LeaflyColor.fieldSurface)
        )
        .accessibilityLabel("Buscar planta por nombre")
    }

    private var statusFilterChips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: LeaflySpacing.sm) {
                ForEach(WateringStatus.allCases, id: \.self) { status in
                    LeaflyChip(
                        title: status.title,
                        systemImage: status.symbolName,
                        isSelected: viewModel.selectedStatusFilter == status
                    ) {
                        viewModel.setStatusFilter(status)
                    }
                    .accessibilityLabel("\(status.title), \(viewModel.selectedStatusFilter == status ? "seleccionado" : "no seleccionado")")
                }
            }
            .padding(.horizontal, LeaflySpacing.xs)
        }
    }

    private var clearFiltersButton: some View {
        Button {
            viewModel.clearFilters()
        } label: {
            HStack(spacing: LeaflySpacing.xs) {
                Image(systemName: "xmark.circle.fill")
                    .font(.system(size: 14, weight: .regular))
                Text(LeaflyCopy.Empty.SearchResults.button)
                    .font(LeaflyFont.callout)
            }
            .foregroundStyle(LeaflyColor.secondary)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(LeaflyCopy.Empty.SearchResults.button)
    }

    // MARK: - Contenido

    @ViewBuilder
    private var content: some View {
        if viewModel.isLoading && viewModel.plants.isEmpty {
            loadingView
        } else if let errorMessage = viewModel.errorMessage {
            errorCard(message: errorMessage)
        } else if viewModel.filteredPlants.isEmpty && viewModel.hasActiveFilters {
            emptySearchResults
        } else if viewModel.filteredPlants.isEmpty {
            emptyState
        } else {
            plantsList
        }
    }

    private var emptySearchResults: some View {
        LeaflyCard {
            VStack(spacing: LeaflySpacing.md) {
                ZStack {
                    Circle()
                        .fill(LeaflyColor.primarySoft)
                        .frame(width: 64, height: 64)

                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundStyle(LeaflyColor.secondary)
                }
                .accessibilityHidden(true)

                Text(LeaflyCopy.Empty.SearchResults.title)
                    .font(LeaflyFont.title3)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text(LeaflyCopy.Empty.SearchResults.subtitle)
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)

                Button(LeaflyCopy.Empty.SearchResults.button) {
                    viewModel.clearFilters()
                }
                .buttonStyle(LeaflyPrimaryButtonStyle())
            }
            .frame(maxWidth: .infinity)
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
            ForEach(viewModel.filteredPlants) { plant in
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
                Label(LeaflyCopy.Error.Plants.title, systemImage: "exclamationmark.circle.fill")
                    .font(LeaflyFont.headline)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text(message)
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)

                Button(LeaflyCopy.Error.Plants.retry) {
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

                Text(LeaflyCopy.Empty.Plants.title)
                    .font(LeaflyFont.title3)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text(LeaflyCopy.Empty.Plants.subtitle)
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)

                Button(LeaflyCopy.Empty.Plants.button) {
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
                PlantAvatar(plant: plant)

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
}

#Preview("Mis plantas") {
    PlantsHomeView(
        repository: PreviewPlantsRepository(),
        reminders: NoopWateringReminders(),
        settingsViewModel: SettingsViewModel(
            center: FakeNotificationCenterForPreview(),
            reminders: NoopWateringReminders()
        )
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
    func fetchPhotos(for plantID: UUID) async throws -> [PlantPhoto] { [] }
    func addPhoto(_ photo: PlantPhoto) async throws {}
}

@MainActor
private final class FakeNotificationCenterForPreview: LocalNotificationCenter {
    func requestAuthorization() async -> Bool { true }
    func currentAuthorizationStatus() async -> UNAuthorizationStatus { .authorized }
    func pendingIdentifiers(withPrefix prefix: String) async -> [String] { [] }
    func add(_ reminder: WateringReminder) async {}
    func removePending(identifiers: [String]) async {}
}
