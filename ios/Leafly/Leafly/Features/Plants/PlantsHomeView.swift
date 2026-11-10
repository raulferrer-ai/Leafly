//
//  PlantsHomeView.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI
import SwiftData

/// Pantalla a la que se llega tras guardar la primera planta.
/// Por ahora muestra las plantas guardadas; la feature de lista completa llegará después.
struct PlantsHomeView: View {
    @Query(sort: \PlantEntity.createdAt, order: .forward)
    private var plants: [PlantEntity]

    var body: some View {
        LeaflyScreen {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: LeaflySpacing.lg) {
                    header

                    VStack(spacing: LeaflySpacing.sm) {
                        ForEach(plants) { plant in
                            PlantCard(plant: plant)
                        }
                    }
                }
                .frame(maxWidth: LeaflySpacing.contentMaxWidth)
                .padding(.horizontal, LeaflySpacing.screenHorizontal)
                .padding(.top, LeaflySpacing.xxl)
                .padding(.bottom, LeaflySpacing.xl)
                .frame(maxWidth: .infinity)
            }
        }
    }

    private var header: some View {
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
    }

    private var plantsSubtitle: String {
        plants.count == 1
            ? "Una planta a tu cuidado."
            : "\(plants.count) plantas a tu cuidado."
    }
}

// MARK: - Card de planta

private struct PlantCard: View {
    let plant: PlantEntity

    var body: some View {
        LeaflyCard(padding: LeaflySpacing.md) {
            HStack(spacing: LeaflySpacing.md) {
                avatar

                VStack(alignment: .leading, spacing: 3) {
                    Text(plant.name)
                        .font(LeaflyFont.title3)
                        .foregroundStyle(LeaflyColor.textPrimary)
                        .lineLimit(2)

                    if let type = plant.plantType {
                        Text(type.displayName)
                            .font(LeaflyFont.footnote)
                            .foregroundStyle(LeaflyColor.textSecondary)
                    }

                    Text("Añadida el \(formattedCreationDate)")
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textTertiary)
                }

                Spacer(minLength: 0)
            }
        }
        .accessibilityElement(children: .combine)
    }

    private var formattedCreationDate: String {
        plant.createdAt.formatted(
            .dateTime
                .day()
                .month(.wide)
                .locale(Locale(identifier: "es_ES"))
        )
    }

    private var avatar: some View {
        ZStack {
            Circle()
                .fill(LeaflyColor.primarySoft)
                .frame(width: 58, height: 58)

            Image(systemName: plant.plantType?.symbolName ?? "leaf.fill")
                .font(.system(size: 22, weight: .semibold))
                .foregroundStyle(LeaflyColor.secondary)
        }
        .accessibilityHidden(true)
    }
}

#Preview("Mis plantas") {
    PlantsHomeView()
        .modelContainer(for: PlantEntity.self, inMemory: true)
}
