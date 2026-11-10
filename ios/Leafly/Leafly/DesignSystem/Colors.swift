//
//  Colors.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Paleta semántica de Leafly.
/// Sigue `docs/design-system.md`: verdes naturales, superficies cálidas y sombras suaves.
enum LeaflyColor {

    // MARK: - Fondos

    /// Inicio del degradado de fondo: verde medio, profundo y calmado.
    static let backgroundTop = Color(hex: 0x4E7A5E)
    /// Punto intermedio del degradado.
    static let backgroundMiddle = Color(hex: 0x93B49B)
    /// Final del degradado: verde muy claro, casi luz de mañana.
    static let backgroundBottom = Color(hex: 0xEEF4E9)

    /// Superficie de cards: blanco roto.
    static let surface = Color(hex: 0xF9F8F6)
    /// Superficie elevada: blanco puro.
    static let elevatedSurface = Color.white
    /// Fondo de campos de texto y zonas de descanso dentro de una card.
    static let fieldSurface = Color(hex: 0xF1EFE9)

    // MARK: - Verdes

    /// Verde principal: crecimiento, acciones positivas.
    static let primary = Color(hex: 0x4F8B63)
    /// Verde muy claro para chips y estados suaves.
    static let primarySoft = Color(hex: 0xE0EDDF)
    /// Verde profundo para títulos e iconos importantes.
    static let secondary = Color(hex: 0x315B44)

    // MARK: - Estados de planta

    static let healthy = Color(hex: 0x5E9C6F)
    static let needsWaterSoon = Color(hex: 0xD7A45C)
    static let needsWaterNow = Color(hex: 0xC97A62)

    // MARK: - Textos

    static let textPrimary = Color(hex: 0x2F2B26)
    static let textSecondary = Color(hex: 0x6D675E)
    static let textTertiary = Color(hex: 0xA8A198)

    // MARK: - Otros

    static let onPrimary = Color.white
    static let separator = Color(hex: 0xE8E4DC)

    // MARK: - Degradado

    static var backgroundGradient: LinearGradient {
        LinearGradient(
            colors: [backgroundTop, backgroundMiddle, backgroundBottom],
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }
}

extension Color {

    /// Crea un color a partir de un valor hexadecimal RGB (por ejemplo, `0x4F8B63`).
    init(hex: UInt32, opacity: Double = 1) {
        let red = Double((hex >> 16) & 0xFF) / 255
        let green = Double((hex >> 8) & 0xFF) / 255
        let blue = Double(hex & 0xFF) / 255

        self.init(.sRGB, red: red, green: green, blue: blue, opacity: opacity)
    }
}
