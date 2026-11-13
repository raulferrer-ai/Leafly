//
//  WateringStatus.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import Foundation

/// Estado de riego de una planta, calculado a partir de cuándo se regó por última vez.
///
/// Regla simple y compartida con la UI:
/// - 0–4 días → bien
/// - 5–6 días → conviene regar pronto
/// - 7 días o más → riego urgente
nonisolated enum WateringStatus: Equatable, Sendable, CaseIterable {
    case healthy
    case needsWaterSoon
    case needsWaterNow

    /// Días desde el último riego a partir de los cuales conviene regar pronto.
    static let soonThresholdDays = 5

    /// Días desde el último riego a partir de los cuales el riego es urgente.
    static let urgentThresholdDays = 7

    init(daysSinceWatering days: Int) {
        switch max(0, days) {
        case ..<Self.soonThresholdDays:
            self = .healthy
        case ..<Self.urgentThresholdDays:
            self = .needsWaterSoon
        default:
            self = .needsWaterNow
        }
    }
}
