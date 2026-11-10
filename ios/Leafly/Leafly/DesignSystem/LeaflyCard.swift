//
//  LeaflyCard.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Card flotante de Leafly: superficie blanco roto, radio grande y sombra muy suave.
struct LeaflyCard<Content: View>: View {
    private let padding: CGFloat
    private let content: Content

    init(padding: CGFloat = LeaflySpacing.lg, @ViewBuilder content: () -> Content) {
        self.padding = padding
        self.content = content()
    }

    var body: some View {
        content
            .padding(padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: LeaflyRadius.card, style: .continuous)
                    .fill(LeaflyColor.surface)
                    .shadow(color: Color.black.opacity(0.10), radius: 26, x: 0, y: 14)
            )
    }
}

#Preview("Card Leafly") {
    LeaflyScreen {
        LeaflyCard {
            VStack(alignment: .leading, spacing: LeaflySpacing.sm) {
                Text("Monstera")
                    .font(LeaflyFont.title3)
                    .foregroundStyle(LeaflyColor.textPrimary)
                Text("Regada hace 2 días")
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)
            }
        }
        .padding(LeaflySpacing.screenHorizontal)
    }
}
