//
//  LeaflyMark.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Marca suave de Leafly: hoja sobre un círculo luminoso.
struct LeaflyMark: View {
    var size: CGFloat = 96

    var body: some View {
        ZStack {
            Circle()
                .fill(Color.white.opacity(0.18))
                .frame(width: size * 1.35, height: size * 1.35)
                .blur(radius: 2)

            Circle()
                .fill(
                    LinearGradient(
                        colors: [Color.white, LeaflyColor.primarySoft],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: size, height: size)
                .shadow(color: Color.black.opacity(0.12), radius: 20, x: 0, y: 12)

            Image(systemName: "leaf.fill")
                .font(.system(size: size * 0.40, weight: .semibold))
                .foregroundStyle(LeaflyColor.secondary)
        }
        .accessibilityHidden(true)
    }
}

#Preview("Marca Leafly") {
    LeaflyScreen {
        LeaflyMark()
    }
}
