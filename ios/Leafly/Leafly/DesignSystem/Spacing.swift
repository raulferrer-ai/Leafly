//
//  Spacing.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Espaciados base. Mucha respiración entre elementos.
enum LeaflySpacing {
    static let xxs: CGFloat = 4
    static let xs: CGFloat = 8
    static let sm: CGFloat = 12
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 48

    /// Margen horizontal cómodo para el contenido principal.
    static let screenHorizontal: CGFloat = 24
    /// Ancho máximo del contenido en pantallas grandes.
    static let contentMaxWidth: CGFloat = 560
}

/// Radios de las formas de Leafly. Todo redondeado y orgánico.
enum LeaflyRadius {
    static let card: CGFloat = 28
    static let control: CGFloat = 22
    static let field: CGFloat = 18
    static let chip: CGFloat = 16
}
