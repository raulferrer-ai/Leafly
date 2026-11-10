//
//  ButtonStyles.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Botón principal: pill verde, texto blanco y sombra suave.
struct LeaflyPrimaryButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(LeaflyFont.button)
            .foregroundStyle(LeaflyColor.onPrimary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 18)
            .padding(.horizontal, LeaflySpacing.lg)
            .background(
                Capsule(style: .continuous)
                    .fill(LeaflyColor.primary)
                    .opacity(isEnabled ? 1 : 0.55)
                    .shadow(
                        color: LeaflyColor.primary.opacity(isEnabled ? 0.30 : 0),
                        radius: 18,
                        x: 0,
                        y: 10
                    )
            )
            .scaleEffect(configuration.isPressed ? 0.98 : 1)
            .animation(.snappy(duration: 0.18), value: configuration.isPressed)
    }
}

/// Botón secundario y silencioso: texto verde sobre superficie suave.
struct LeaflyQuietButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(LeaflyFont.callout)
            .foregroundStyle(LeaflyColor.secondary)
            .padding(.vertical, LeaflySpacing.sm)
            .padding(.horizontal, LeaflySpacing.md)
            .background(
                Capsule(style: .continuous)
                    .fill(Color.white.opacity(configuration.isPressed ? 0.85 : 0.65))
            )
            .scaleEffect(configuration.isPressed ? 0.98 : 1)
            .animation(.snappy(duration: 0.18), value: configuration.isPressed)
    }
}

/// Botón redondo para acciones discretas sobre el degradado (por ejemplo, cancelar).
struct LeaflyCircularButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .semibold, design: .rounded))
            .foregroundStyle(LeaflyColor.secondary)
            .frame(width: 44, height: 44)
            .background(
                Circle()
                    .fill(Color.white.opacity(configuration.isPressed ? 0.80 : 0.92))
                    .shadow(color: Color.black.opacity(0.10), radius: 12, x: 0, y: 6)
            )
            .scaleEffect(configuration.isPressed ? 0.94 : 1)
            .animation(.snappy(duration: 0.18), value: configuration.isPressed)
    }
}
