//
//  PlantDetailPlaceholderView.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import SwiftUI

/// Pantalla de detalle provisional.
/// La feature de detalle completa llegará después con historial y riegos.
struct PlantDetailPlaceholderView: View {
    let plant: Plant

    @Environment(\.dismiss) private var dismiss

    private var status: WateringStatus {
        plant.wateringStatus()
    }

    var body: some View {
        LeaflyScreen {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: LeaflySpacing.lg) {
                    topBar
                    header
                    statusCard
                    comingSoonCard
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

            Text(plant.name)
                .font(LeaflyFont.display)
                .foregroundStyle(Color.white)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityIdentifier("plant-detail-title")

            if let type = plant.type {
                Text(type.displayName)
                    .font(LeaflyFont.body)
                    .foregroundStyle(Color.white.opacity(0.88))
            }
        }
    }

    private var statusCard: some View {
        LeaflyCard(padding: LeaflySpacing.md) {
            HStack(spacing: LeaflySpacing.md) {
                avatar

                VStack(alignment: .leading, spacing: 4) {
                    LeaflyStatusBadge(
                        title: status.title,
                        color: status.color,
                        systemImage: status.symbolName
                    )

                    Text(plant.wateringSummary())
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textSecondary)
                }

                Spacer(minLength: 0)
            }
        }
    }

    private var comingSoonCard: some View {
        LeaflyCard {
            VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
                Label("Muy pronto", systemImage: "sparkles")
                    .font(LeaflyFont.headline)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text("Muy pronto podrás ver aquí todos sus cuidados.")
                    .font(LeaflyFont.body)
                    .foregroundStyle(LeaflyColor.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
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

#Preview("Detalle provisional") {
    NavigationStack {
        PlantDetailPlaceholderView(
            plant: Plant(name: "Monstera", type: .monstera)
        )
    }
}
