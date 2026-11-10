//
//  Typography.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Tipografía de Leafly.
/// Usa la fuente del sistema en su variante redondeada: amable, legible y cercana.
/// Se apoya en estilos dinámicos para respetar el tamaño de texto del sistema.
enum LeaflyFont {

    /// Títulos grandes de bienvenida.
    static let display = Font.system(.largeTitle, design: .rounded, weight: .bold)
    /// Título principal de una pantalla.
    static let title = Font.system(.title2, design: .rounded, weight: .bold)
    /// Título de card o sección destacada.
    static let title3 = Font.system(.title3, design: .rounded, weight: .semibold)
    /// Encabezado de sección.
    static let headline = Font.system(.headline, design: .rounded, weight: .semibold)
    /// Texto de cuerpo.
    static let body = Font.system(.body, design: .rounded, weight: .regular)
    /// Texto de cuerpo con énfasis suave.
    static let bodyMedium = Font.system(.body, design: .rounded, weight: .medium)
    /// Texto secundario.
    static let callout = Font.system(.callout, design: .rounded, weight: .medium)
    /// Texto de apoyo.
    static let footnote = Font.system(.footnote, design: .rounded, weight: .regular)
    /// Etiquetas pequeñas en mayúsculas.
    static let caption = Font.system(.caption, design: .rounded, weight: .semibold)
    /// Texto de botones.
    static let button = Font.system(.headline, design: .rounded, weight: .semibold)
}
