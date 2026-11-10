//
//  WelcomeView.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import SwiftUI

/// Pantalla de bienvenida del onboarding.
struct WelcomeView: View {
    let onAddPlant: () -> Void

    @State private var hasAppeared = false

    var body: some View {
        LeaflyScreen {
            ScrollView(showsIndicators: false) {
                VStack(spacing: LeaflySpacing.xl) {
                    greeting
                    benefits
                }
                .frame(maxWidth: LeaflySpacing.contentMaxWidth)
                .padding(.horizontal, LeaflySpacing.screenHorizontal)
                .padding(.top, LeaflySpacing.xxl)
                .padding(.bottom, LeaflySpacing.lg)
                .frame(maxWidth: .infinity)
                .opacity(hasAppeared ? 1 : 0)
                .offset(y: hasAppeared ? 0 : 14)
            }
            .safeAreaInset(edge: .bottom, spacing: 0) {
                callToAction
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.55)) {
                hasAppeared = true
            }
        }
    }

    // MARK: - Secciones

    private var greeting: some View {
        VStack(spacing: LeaflySpacing.md) {
            LeaflyMark()

            Text("BIENVENIDO A LEAFLY")
                .font(LeaflyFont.caption)
                .tracking(1.6)
                .foregroundStyle(Color.white.opacity(0.75))

            Text("Tu jardín,\ncon calma")
                .font(LeaflyFont.display)
                .foregroundStyle(Color.white)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)

            Text("Un lugar tranquilo para cuidar tus plantas, día a día.")
                .font(LeaflyFont.body)
                .foregroundStyle(Color.white.opacity(0.90))
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private var benefits: some View {
        LeaflyCard(padding: LeaflySpacing.md) {
            HStack(spacing: LeaflySpacing.md) {
                Image(systemName: "leaf.fill")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(LeaflyColor.secondary)
                    .frame(width: 48, height: 48)
                    .background(
                        Circle()
                            .fill(LeaflyColor.primarySoft)
                    )
                    .accessibilityHidden(true)

                VStack(alignment: .leading, spacing: 3) {
                    Text("Riegos, cuidados y recuerdos")
                        .font(LeaflyFont.headline)
                        .foregroundStyle(LeaflyColor.textPrimary)
                        .fixedSize(horizontal: false, vertical: true)

                    Text("Lo esencial para que crezca sana, sin complicarte.")
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer(minLength: 0)
            }
        }
    }

    private var callToAction: some View {
        VStack(spacing: LeaflySpacing.xs) {
            Button("Añadir mi primera planta", action: onAddPlant)
                .buttonStyle(LeaflyPrimaryButtonStyle())

            Text("Solo necesitas un nombre. El resto puede esperar.")
                .font(LeaflyFont.footnote)
                .foregroundStyle(LeaflyColor.textSecondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: LeaflySpacing.contentMaxWidth)
        .padding(.horizontal, LeaflySpacing.screenHorizontal)
        .padding(.top, LeaflySpacing.lg)
        .padding(.bottom, LeaflySpacing.xs)
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(
                stops: [
                    .init(color: LeaflyColor.backgroundBottom.opacity(0), location: 0),
                    .init(color: LeaflyColor.backgroundBottom.opacity(0.85), location: 0.35),
                    .init(color: LeaflyColor.backgroundBottom, location: 0.65),
                    .init(color: LeaflyColor.backgroundBottom, location: 1)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea(edges: .bottom)
        )
    }
}

#Preview("Bienvenida") {
    WelcomeView(onAddPlant: {})
}
