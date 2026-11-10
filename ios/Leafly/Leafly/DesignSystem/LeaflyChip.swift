//
//  LeaflyChip.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Chip de selección: muy redondeado, con icono y texto.
struct LeaflyChip: View {
    let title: String
    let systemImage: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: LeaflySpacing.xs) {
                Image(systemName: systemImage)
                    .font(.system(size: 14, weight: .semibold))
                Text(title)
                    .font(LeaflyFont.callout)
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
            }
            .foregroundStyle(isSelected ? LeaflyColor.onPrimary : LeaflyColor.secondary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .padding(.horizontal, LeaflySpacing.sm)
            .background(
                RoundedRectangle(cornerRadius: LeaflyRadius.chip, style: .continuous)
                    .fill(isSelected ? LeaflyColor.primary : LeaflyColor.surface)
                    .shadow(
                        color: Color.black.opacity(isSelected ? 0.14 : 0.06),
                        radius: 10,
                        x: 0,
                        y: 6
                    )
            )
        }
        .buttonStyle(.plain)
        .animation(.snappy(duration: 0.20), value: isSelected)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

#Preview("Chips Leafly") {
    LeaflyScreen {
        VStack(spacing: LeaflySpacing.sm) {
            LeaflyChip(title: "Monstera", systemImage: "leaf.fill", isSelected: true) {}
            LeaflyChip(title: "Cactus", systemImage: "cactus", isSelected: false) {}
        }
        .padding(LeaflySpacing.screenHorizontal)
    }
}
