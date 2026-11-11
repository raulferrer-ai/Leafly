//
//  PlantPhotoHistorySection.swift
//  Leafly
//
//  Created by RaulF on 10/11/2026.
//

import PhotosUI
import SwiftUI

/// Sección del detalle que muestra y amplía el historial visual de la planta.
///
/// Reutiliza el procesado de imagen, la cámara y la galería del alta. La
/// sección solo se ocupa de elegir y preparar la foto; guardarla y actualizar
/// la planta es responsabilidad del ViewModel.
struct PlantPhotoHistorySection: View {
    let photos: [PlantPhoto]
    let isSaving: Bool
    let onAddPhoto: @MainActor (Data) async -> Bool

    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var isShowingGallery = false
    @State private var isShowingCamera = false
    @State private var isChoosingSource = false
    @State private var isProcessing = false
    @State private var message: String?

    var body: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
            header

            if photos.isEmpty {
                emptyState
            } else {
                historyStrip
                addPhotoButton
            }

            if isProcessing {
                processingRow
            }

            if let message {
                Label(message, systemImage: "info.circle")
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(Color.white.opacity(0.85))
                    .fixedSize(horizontal: false, vertical: true)
                    .transition(.opacity)
            }
        }
        .animation(.snappy(duration: 0.25), value: photos.map(\.id))
        .animation(.snappy(duration: 0.25), value: message)
        .photosPicker(
            isPresented: $isShowingGallery,
            selection: $selectedPhotoItem,
            matching: .images,
            photoLibrary: .shared()
        )
        .onChange(of: selectedPhotoItem) { _, item in
            guard let item else { return }
            Task { await loadPhoto(from: item) }
        }
        .sheet(isPresented: $isShowingCamera) {
            CameraPicker { data in
                Task { await applyCapturedPhoto(data) }
            }
            .ignoresSafeArea()
        }
        .confirmationDialog(
            "Añadir una foto",
            isPresented: $isChoosingSource,
            titleVisibility: .visible
        ) {
            if CameraAccess.isAvailable {
                Button("Hacer una foto") {
                    Task { await startCamera() }
                }
            }

            Button("Elegir de la galería") {
                isShowingGallery = true
            }

            Button("Cancelar", role: .cancel) {}
        } message: {
            Text("Elige cómo quieres guardar este momento.")
        }
    }

    // MARK: - Cabecera

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(alignment: .firstTextBaseline) {
                Text("Su historia")
                    .font(LeaflyFont.headline)
                    .foregroundStyle(Color.white)
                    .accessibilityIdentifier("photo-history-title")

                Spacer()

                if !photos.isEmpty {
                    Text(photosCountLabel)
                        .font(LeaflyFont.caption)
                        .foregroundStyle(Color.white.opacity(0.72))
                }
            }

            Text("Las fotos que guardes quedarán aquí, de la más reciente a la más antigua.")
                .font(LeaflyFont.footnote)
                .foregroundStyle(Color.white.opacity(0.82))
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private var photosCountLabel: String {
        photos.count == 1 ? "1 foto" : "\(photos.count) fotos"
    }

    // MARK: - Historial

    private var historyStrip: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(alignment: .top, spacing: LeaflySpacing.sm) {
                ForEach(Array(photos.enumerated()), id: \.element.id) { index, photo in
                    PlantPhotoHistoryItem(photo: photo)
                        .accessibilityIdentifier("photo-history-item-\(index)")
                }
            }
            .padding(.vertical, 2)
        }
    }

    private var addPhotoButton: some View {
        Button {
            isChoosingSource = true
        } label: {
            Label("Añadir otra foto", systemImage: "plus")
        }
        .buttonStyle(LeaflyQuietButtonStyle())
        .disabled(isProcessing || isSaving)
        .accessibilityIdentifier("add-photo-button")
    }

    // MARK: - Estado vacío

    private var emptyState: some View {
        LeaflyCard {
            VStack(spacing: LeaflySpacing.md) {
                ZStack {
                    Circle()
                        .fill(LeaflyColor.primarySoft)
                        .frame(width: 64, height: 64)

                    Image(systemName: "photo.on.rectangle.angled")
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundStyle(LeaflyColor.secondary)
                }
                .accessibilityHidden(true)

                VStack(spacing: 4) {
                    Text("Aún no hay fotos")
                        .font(LeaflyFont.title3)
                        .foregroundStyle(LeaflyColor.textPrimary)
                        .accessibilityIdentifier("photo-history-empty-title")

                    Text("Cuando quieras, guarda una foto y podrás ver cómo crece con el tiempo.")
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textSecondary)
                        .multilineTextAlignment(.center)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Button {
                    isChoosingSource = true
                } label: {
                    Label("Añadir la primera foto", systemImage: "camera")
                }
                .buttonStyle(LeaflyPrimaryButtonStyle())
                .disabled(isProcessing)
                .accessibilityIdentifier("add-first-photo-button")
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: - Preparando la foto

    private var processingRow: some View {
        HStack(spacing: LeaflySpacing.xs) {
            ProgressView()
                .tint(.white)

            Text("Preparando la foto...")
                .font(LeaflyFont.footnote)
                .foregroundStyle(Color.white.opacity(0.88))
        }
        .transition(.opacity)
    }

    // MARK: - Acciones

    /// Pide la cámara con calma: si no hay permiso, se puede seguir con la
    /// galería o salir sin añadir foto.
    private func startCamera() async {
        message = nil

        guard await CameraAccess.requestAuthorization() else {
            message = "Sin permiso de cámara. Puedes elegir una foto de la galería o salir sin añadir foto."
            return
        }

        isShowingCamera = true
    }

    private func loadPhoto(from item: PhotosPickerItem) async {
        isProcessing = true
        defer {
            isProcessing = false
            selectedPhotoItem = nil
        }

        guard let data = try? await item.loadTransferable(type: Data.self),
              let processed = PlantPhotoProcessor.processedJPEGData(from: data) else {
            message = "No hemos podido usar esa foto. Prueba con otra."
            return
        }

        await save(processed)
    }

    private func applyCapturedPhoto(_ data: Data) async {
        guard let processed = PlantPhotoProcessor.processedJPEGData(from: data) else {
            message = "No hemos podido usar esa foto. Inténtalo de nuevo."
            return
        }

        isProcessing = true
        defer { isProcessing = false }

        await save(processed)
    }

    private func save(_ data: Data) async {
        let didSave = await onAddPhoto(data)
        message = didSave ? nil : "No hemos podido guardar la foto. Inténtalo de nuevo."
    }
}

// MARK: - Foto del historial

private struct PlantPhotoHistoryItem: View {
    let photo: PlantPhoto

    private var dateLabel: String {
        PlantPhotoDateLabel.label(for: photo.addedAt)
    }

    var body: some View {
        VStack(spacing: 6) {
            if let image = PlantPhotoDecoder.image(from: photo.data) {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 136, height: 136)
                    .clipShape(
                        RoundedRectangle(
                            cornerRadius: LeaflyRadius.control,
                            style: .continuous
                        )
                    )
                    .overlay(
                        RoundedRectangle(
                            cornerRadius: LeaflyRadius.control,
                            style: .continuous
                        )
                        .stroke(Color.white.opacity(0.55), lineWidth: 1)
                    )
            } else {
                RoundedRectangle(cornerRadius: LeaflyRadius.control, style: .continuous)
                    .fill(Color.white.opacity(0.20))
                    .frame(width: 136, height: 136)
                    .overlay(
                        Image(systemName: "photo")
                            .font(.system(size: 22, weight: .semibold))
                            .foregroundStyle(Color.white.opacity(0.80))
                    )
            }

            Text(dateLabel)
                .font(LeaflyFont.caption)
                .foregroundStyle(Color.white.opacity(0.88))
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Foto añadida: \(dateLabel)")
    }
}
