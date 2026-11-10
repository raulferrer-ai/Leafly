//
//  PlantType.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation

/// Tipos básicos de planta disponibles durante el onboarding.
/// El tipo es opcional: el usuario puede añadir su planta solo con un nombre.
nonisolated enum PlantType: String, CaseIterable, Identifiable, Sendable {
    case monstera
    case ficus
    case pothos
    case cactus
    case succulent
    case fern
    case orchid

    var id: String { rawValue }

    /// Nombre cercano que se muestra en la interfaz.
    var displayName: String {
        switch self {
        case .monstera: "Monstera"
        case .ficus: "Ficus"
        case .pothos: "Potos"
        case .cactus: "Cactus"
        case .succulent: "Suculenta"
        case .fern: "Helecho"
        case .orchid: "Orquídea"
        }
    }

    /// Icono suave que acompaña al tipo.
    var symbolName: String {
        switch self {
        case .monstera: "leaf.fill"
        case .ficus: "tree.fill"
        case .pothos: "leaf"
        case .cactus: "sun.max.fill"
        case .succulent: "camera.macro"
        case .fern: "laurel.leading"
        case .orchid: "sparkles"
        }
    }
}
