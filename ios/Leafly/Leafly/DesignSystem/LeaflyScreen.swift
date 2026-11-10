//
//  LeaflyScreen.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Fondo común de la app: degradado verde suave con formas orgánicas muy difusas.
struct LeaflyBackground: View {
    var body: some View {
        ZStack {
            LeaflyColor.backgroundGradient

            Circle()
                .fill(Color.white.opacity(0.14))
                .frame(width: 340, height: 340)
                .blur(radius: 60)
                .offset(x: -130, y: -260)

            Circle()
                .fill(LeaflyColor.primarySoft.opacity(0.30))
                .frame(width: 280, height: 280)
                .blur(radius: 70)
                .offset(x: 150, y: 180)
        }
        .ignoresSafeArea()
        .accessibilityHidden(true)
    }
}

/// Contenedor de pantalla: fondo de Leafly + contenido.
struct LeaflyScreen<Content: View>: View {
    private let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        ZStack {
            LeaflyBackground()
            content
        }
    }
}

#Preview("Fondo Leafly") {
    LeaflyScreen {
        Text("Hola, jardín")
            .font(LeaflyFont.title)
            .foregroundStyle(.white)
    }
}
