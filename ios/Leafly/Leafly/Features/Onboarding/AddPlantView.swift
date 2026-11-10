//
//  AddPlantView.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Contexto en el que se abre la pantalla de añadir planta.
/// Cambia solo los textos, no el comportamiento.
enum AddPlantMode {
    /// Primera planta, dentro del onboarding.
    case firstPlant
    /// Planta adicional, desde la lista del jardín.
    case anotherPlant

    var eyebrow: String {
        switch self {
        case .firstPlant: "PRIMERA PLANTA"
        case .anotherPlant: "NUEVA PLANTA"
        }
    }

    var title: String {
        switch self {
        case .firstPlant: "Añade tu primera planta"
        case .anotherPlant: "Añade una planta"
        }
    }

    var subtitle: String {
        switch self {
        case .firstPlant:
            "Con un nombre es suficiente. Puedes completar el resto más adelante."
        case .anotherPlant:
            "Solo necesitas un nombre. Podrás completar el resto más adelante."
        }
    }

    var cancelAccessibilityLabel: String {
        switch self {
        case .firstPlant: "Cancelar y volver a la bienvenida"
        case .anotherPlant: "Cancelar y volver a mis plantas"
        }
    }
}

/// Pantalla para añadir una planta.
struct AddPlantView: View {
    private let onCancel: () -> Void
    private let onSaved: () -> Void
    private let mode: AddPlantMode

    @State private var viewModel: AddPlantViewModel
    @State private var hasAppeared = false
    @FocusState private var isNameFocused: Bool

    init(
        repository: any PlantRepository,
        onCancel: @escaping () -> Void,
        onSaved: @escaping () -> Void = {},
        mode: AddPlantMode = .firstPlant
    ) {
        _viewModel = State(initialValue: AddPlantViewModel(repository: repository))
        self.onCancel = onCancel
        self.onSaved = onSaved
        self.mode = mode
    }

    var body: some View {
        LeaflyScreen {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: LeaflySpacing.lg) {
                    topBar
                    header
                    nameSection
                    typeSection
                }
                .frame(maxWidth: LeaflySpacing.contentMaxWidth)
                .padding(.horizontal, LeaflySpacing.screenHorizontal)
                .padding(.top, LeaflySpacing.sm)
                .padding(.bottom, LeaflySpacing.lg)
                .frame(maxWidth: .infinity)
                .opacity(hasAppeared ? 1 : 0)
                .offset(y: hasAppeared ? 0 : 14)
            }
            .scrollDismissesKeyboard(.interactively)
            .safeAreaInset(edge: .bottom, spacing: 0) {
                saveSection
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.45)) {
                hasAppeared = true
            }
        }
    }

    // MARK: - Cabecera

    private var topBar: some View {
        HStack {
            Button(action: onCancel) {
                Image(systemName: "xmark")
            }
            .buttonStyle(LeaflyCircularButtonStyle())
            .accessibilityLabel(mode.cancelAccessibilityLabel)

            Spacer()

            Text(mode.eyebrow)
                .font(LeaflyFont.caption)
                .tracking(1.4)
                .foregroundStyle(Color.white.opacity(0.75))

            Spacer()

            Color.clear
                .frame(width: 44, height: 44)
                .accessibilityHidden(true)
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.xs) {
            Text(mode.title)
                .font(LeaflyFont.title)
                .foregroundStyle(Color.white)
                .fixedSize(horizontal: false, vertical: true)

            Text(mode.subtitle)
                .font(LeaflyFont.body)
                .foregroundStyle(Color.white.opacity(0.88))
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    // MARK: - Nombre

    private var nameSection: some View {
        LeaflyCard {
            VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("¿Cómo se llama?")
                        .font(LeaflyFont.headline)
                        .foregroundStyle(LeaflyColor.textPrimary)

                    Text("Su nombre, como tú la llamas.")
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textSecondary)
                }

                HStack(spacing: LeaflySpacing.sm) {
                    Image(systemName: "leaf")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(
                            viewModel.nameError == nil ? LeaflyColor.textTertiary : LeaflyColor.needsWaterNow
                        )

                    TextField("Por ejemplo, Monstera", text: $viewModel.name)
                        .font(LeaflyFont.body)
                        .foregroundStyle(LeaflyColor.textPrimary)
                        .textInputAutocapitalization(.sentences)
                        .autocorrectionDisabled(false)
                        .submitLabel(.done)
                        .focused($isNameFocused)
                        .onSubmit {
                            isNameFocused = false
                        }
                        .onChange(of: viewModel.name) { _, _ in
                            viewModel.clearNameError()
                        }
                }
                .padding(.horizontal, LeaflySpacing.md)
                .padding(.vertical, 15)
                .background(
                    RoundedRectangle(cornerRadius: LeaflyRadius.field, style: .continuous)
                        .fill(LeaflyColor.fieldSurface)
                )
                .overlay(
                    RoundedRectangle(cornerRadius: LeaflyRadius.field, style: .continuous)
                        .stroke(
                            isNameFocused ? LeaflyColor.primary.opacity(0.45) : Color.clear,
                            lineWidth: 1.5
                        )
                )
                .animation(.snappy(duration: 0.2), value: isNameFocused)

                if let nameError = viewModel.nameError {
                    Label(nameError, systemImage: "exclamationmark.circle.fill")
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.needsWaterNow)
                        .fixedSize(horizontal: false, vertical: true)
                        .transition(.opacity.combined(with: .move(edge: .top)))
                }
            }
            .animation(.snappy(duration: 0.25), value: viewModel.nameError)
        }
    }

    // MARK: - Tipo

    private var typeSection: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
            HStack(alignment: .firstTextBaseline) {
                Text("¿Qué tipo de planta es?")
                    .font(LeaflyFont.headline)
                    .foregroundStyle(Color.white)

                Spacer()

                Text("Opcional")
                    .font(LeaflyFont.caption)
                    .foregroundStyle(Color.white.opacity(0.72))
            }

            LazyVGrid(
                columns: [GridItem(.adaptive(minimum: 150), spacing: LeaflySpacing.sm)],
                spacing: LeaflySpacing.sm
            ) {
                LeaflyChip(
                    title: "Sin especificar",
                    systemImage: "questionmark",
                    isSelected: viewModel.selectedType == nil
                ) {
                    withAnimation(.snappy(duration: 0.2)) {
                        viewModel.clearType()
                    }
                }

                ForEach(PlantType.allCases) { type in
                    LeaflyChip(
                        title: type.displayName,
                        systemImage: type.symbolName,
                        isSelected: viewModel.selectedType == type
                    ) {
                        withAnimation(.snappy(duration: 0.2)) {
                            viewModel.toggleType(type)
                        }
                    }
                }
            }
        }
        .padding(.top, LeaflySpacing.xs)
    }

    // MARK: - Guardar

    private var saveSection: some View {
        VStack(spacing: LeaflySpacing.sm) {
            if let saveError = viewModel.saveError {
                Text(saveError)
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.needsWaterNow)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
                    .transition(.opacity)
            }

            Button {
                Task { await save() }
            } label: {
                HStack(spacing: LeaflySpacing.xs) {
                    if viewModel.isSaving {
                        ProgressView()
                            .tint(LeaflyColor.onPrimary)
                    }

                    Text(viewModel.isSaving ? "Guardando..." : "Guardar planta")
                }
            }
            .buttonStyle(LeaflyPrimaryButtonStyle())
            .disabled(viewModel.isSaving)
        }
        .frame(maxWidth: LeaflySpacing.contentMaxWidth)
        .padding(.horizontal, LeaflySpacing.screenHorizontal)
        .padding(.top, LeaflySpacing.md)
        .padding(.bottom, LeaflySpacing.xs)
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(
                stops: [
                    .init(color: LeaflyColor.backgroundBottom.opacity(0), location: 0),
                    .init(color: LeaflyColor.backgroundBottom.opacity(0.92), location: 0.20),
                    .init(color: LeaflyColor.backgroundBottom, location: 0.35),
                    .init(color: LeaflyColor.backgroundBottom, location: 1)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea(edges: .bottom)
        )
        .animation(.snappy(duration: 0.25), value: viewModel.saveError)
    }

    // MARK: - Acciones

    private func save() async {
        let didSave = await viewModel.save()

        if didSave {
            isNameFocused = false
            onSaved()
        } else if viewModel.nameError != nil {
            isNameFocused = true
        }
    }
}

#Preview("Añadir planta") {
    AddPlantView(
        repository: PreviewPlantRepository(),
        onCancel: {}
    )
}

// MARK: - Preview

@MainActor
private final class PreviewPlantRepository: PlantRepository {
    func fetchPlants() async throws -> [Plant] { [] }
    func add(_ plant: Plant) async throws {}
}
