//
//  WateringLogSection.swift
//  Leafly
//
//  Created by RaulF on 11/11/2026.
//

import SwiftUI

/// Sección del detalle que muestra el historial de riegos de la planta.
///
/// Las entradas se ordenan del más reciente al más antiguo y cada una
/// muestra una fecha comprensible (Hoy, Ayer o fecha). Cuando no hay
/// riegos, se muestra un estado vacío calmado sin inventar fechas.
struct WateringLogSection: View {
    let entries: [WateringLogEntry]
    let isLoading: Bool
    let errorMessage: String?
    let onRetry: @MainActor () async -> Void
    let onMarkAsWatered: @MainActor () async -> Void

    @State private var message: String?

    var body: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
            header

            if let errorMessage {
                errorCard(message: errorMessage)
            } else if entries.isEmpty {
                emptyState
            } else {
                historyList
            }

            if isLoading {
                loadingRow
            }

            if let message {
                Label(message, systemImage: "info.circle")
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(Color.white.opacity(0.85))
                    .fixedSize(horizontal: false, vertical: true)
                    .transition(.opacity)
            }
        }
        .animation(.snappy(duration: 0.25), value: entries.map(\.id))
        .animation(.snappy(duration: 0.25), value: message)
        .animation(.snappy(duration: 0.25), value: errorMessage)
    }

    // MARK: - Cabecera

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(alignment: .firstTextBaseline) {
                Text("Su historial de riegos")
                    .font(LeaflyFont.headline)
                    .foregroundStyle(Color.white)
                    .accessibilityIdentifier("watering-log-title")

                Spacer()

                if !entries.isEmpty {
                    Text(entriesCountLabel)
                        .font(LeaflyFont.caption)
                        .foregroundStyle(Color.white.opacity(0.72))
                }
            }

            Text("Cada riego queda aquí anotado, del más reciente al más antiguo.")
                .font(LeaflyFont.footnote)
                .foregroundStyle(Color.white.opacity(0.82))
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private var entriesCountLabel: String {
        entries.count == 1 ? "1 riego" : "\(entries.count) riegos"
    }

    // MARK: - Historial

    private var historyList: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.xs) {
            ForEach(Array(entries.enumerated()), id: \.element.id) { index, entry in
                WateringLogEntryRow(entry: entry)
                    .accessibilityIdentifier("watering-log-entry-\(index)")
            }
        }
    }

    // MARK: - Estado vacío

    private var emptyState: some View {
        LeaflyCard {
            VStack(spacing: LeaflySpacing.md) {
                ZStack {
                    Circle()
                        .fill(LeaflyColor.primarySoft)
                        .frame(width: 64, height: 64)

                    Image(systemName: "drop.fill")
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundStyle(LeaflyColor.secondary)
                }
                .accessibilityHidden(true)

                VStack(spacing: 4) {
                    Text(LeaflyCopy.Empty.WateringLog.title)
                        .font(LeaflyFont.title3)
                        .foregroundStyle(LeaflyColor.textPrimary)
                        .accessibilityIdentifier("watering-log-empty-title")

                    Text(LeaflyCopy.Empty.WateringLog.subtitle)
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textSecondary)
                        .multilineTextAlignment(.center)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Button {
                    Task {
                        await onMarkAsWatered()
                    }
                } label: {
                    Label(LeaflyCopy.Empty.WateringLog.button, systemImage: "drop.fill")
                }
                .buttonStyle(LeaflyPrimaryButtonStyle())
                .disabled(isLoading)
                .accessibilityIdentifier("water-first-time-button")
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: - Error de carga

    private func errorCard(message: String) -> some View {
        LeaflyCard {
            VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
                Label("Historial de riegos", systemImage: "exclamationmark.circle.fill")
                    .font(LeaflyFont.headline)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text(message)
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)

                Button(LeaflyCopy.Error.PhotoHistory.retry) {
                    Task {
                        await onRetry()
                    }
                }
                .buttonStyle(LeaflyQuietButtonStyle())
            }
        }
    }

    // MARK: - Cargando

    private var loadingRow: some View {
        HStack(spacing: LeaflySpacing.xs) {
            ProgressView()
                .tint(.white)

            Text("Cargando historial...")
                .font(LeaflyFont.footnote)
                .foregroundStyle(Color.white.opacity(0.88))
        }
        .transition(.opacity)
    }
}

// MARK: - Entrada del historial

private struct WateringLogEntryRow: View {
    let entry: WateringLogEntry

    private var dateLabel: String {
        PlantPhotoDateLabel.label(for: entry.wateredAt)
    }

    var body: some View {
        HStack(spacing: LeaflySpacing.md) {
            Circle()
                .fill(LeaflyColor.primary)
                .frame(width: 10, height: 10)

            VStack(alignment: .leading, spacing: 2) {
                Text(dateLabel)
                    .font(LeaflyFont.callout)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text("Regado")
                    .font(LeaflyFont.caption)
                    .foregroundStyle(LeaflyColor.textSecondary)
            }

            Spacer(minLength: 0)
        }
        .padding(.vertical, LeaflySpacing.xs)
        .padding(.horizontal, LeaflySpacing.sm)
        .background(
            RoundedRectangle(cornerRadius: LeaflyRadius.control, style: .continuous)
                .fill(Color.white.opacity(0.50))
        )
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Riego: \(dateLabel)")
    }
}

#Preview("Historial de riegos") {
    LeaflyScreen {
        WateringLogSection(
            entries: [
                WateringLogEntry(plantID: UUID(), wateredAt: .now),
                WateringLogEntry(
                    plantID: UUID(),
                    wateredAt: Calendar.current.date(byAdding: .day, value: -1, to: .now) ?? .now
                ),
                WateringLogEntry(
                    plantID: UUID(),
                    wateredAt: Calendar.current.date(byAdding: .day, value: -5, to: .now) ?? .now
                )
            ],
            isLoading: false,
            errorMessage: nil,
            onRetry: {},
            onMarkAsWatered: {}
        )
        .padding(LeaflySpacing.screenHorizontal)
    }
}

#Preview("Historial vacío") {
    LeaflyScreen {
        WateringLogSection(
            entries: [],
            isLoading: false,
            errorMessage: nil,
            onRetry: {},
            onMarkAsWatered: {}
        )
        .padding(LeaflySpacing.screenHorizontal)
    }
}
