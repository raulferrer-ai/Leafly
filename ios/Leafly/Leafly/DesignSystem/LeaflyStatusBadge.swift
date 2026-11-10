//
//  LeaflyStatusBadge.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import SwiftUI

/// Chip de estado no interactivo: fondo suave, icono pequeño y texto.
/// Se usa para mostrar el estado de riego de una planta de un vistazo.
struct LeaflyStatusBadge: View {
    let title: String
    let color: Color
    var systemImage: String?

    var body: some View {
        HStack(spacing: 5) {
            if let systemImage {
                Image(systemName: systemImage)
                    .font(.system(size: 11, weight: .semibold))
            } else {
                Circle()
                    .fill(color)
                    .frame(width: 7, height: 7)
            }

            Text(title)
                .font(LeaflyFont.caption)
        }
        .foregroundStyle(color)
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(
            Capsule(style: .continuous)
                .fill(color.opacity(0.14))
        )
        .accessibilityElement(children: .combine)
    }
}

#Preview("Estados de riego") {
    LeaflyScreen {
        VStack(spacing: LeaflySpacing.sm) {
            LeaflyStatusBadge(
                title: "Al día",
                color: LeaflyColor.healthy,
                systemImage: "checkmark.circle.fill"
            )
            LeaflyStatusBadge(
                title: "Riega pronto",
                color: LeaflyColor.needsWaterSoon,
                systemImage: "drop.circle.fill"
            )
            LeaflyStatusBadge(
                title: "Necesita agua",
                color: LeaflyColor.needsWaterNow,
                systemImage: "exclamationmark.circle.fill"
            )
        }
        .padding(LeaflySpacing.screenHorizontal)
    }
}
