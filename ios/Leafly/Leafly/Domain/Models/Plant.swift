//
//  Plant.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation

/// Modelo de dominio de una planta.
/// Independiente de SwiftData para mantener el dominio limpio y testeable.
nonisolated struct Plant: Identifiable, Equatable, Sendable {
    let id: UUID
    var name: String
    var type: PlantType?
    var createdAt: Date

    init(id: UUID = UUID(), name: String, type: PlantType? = nil, createdAt: Date = .now) {
        self.id = id
        self.name = name
        self.type = type
        self.createdAt = createdAt
    }
}
