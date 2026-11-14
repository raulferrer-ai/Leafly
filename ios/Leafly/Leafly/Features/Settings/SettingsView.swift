//
//  SettingsView.swift
//  Leafly
//
//  Pantalla de ajustes de notificaciones de riego.
//

import SwiftUI
import UserNotifications

/// Pantalla de ajustes del usuario.
///
/// Permite ver el estado del permiso de notificaciones y elegir la hora
/// preferida de los recordatorios de riego.
struct SettingsView: View {
    private let viewModel: SettingsViewModel

    @State private var showingHourPicker = false

    init(viewModel: SettingsViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        NavigationStack {
            LeaflyScreen {
                ScrollView(showsIndicators: false) {
                    VStack(alignment: .leading, spacing: LeaflySpacing.lg) {
                        header
                        permissionSection
                        timeSection
                    }
                    .frame(maxWidth: LeaflySpacing.contentMaxWidth)
                    .padding(.horizontal, LeaflySpacing.screenHorizontal)
                    .padding(.top, LeaflySpacing.xxl)
                    .padding(.bottom, LeaflySpacing.xl)
                    .frame(maxWidth: .infinity)
                }
            }
            .navigationTitle(LeaflyCopy.Notifications.title)
            .navigationBarTitleDisplayMode(.inline)
            .task {
                await viewModel.load()
            }
        }
    }

    // MARK: - Cabecera

    private var header: some View {
        VStack(alignment: .leading, spacing: LeaflySpacing.xs) {
            Text(LeaflyCopy.Notifications.title)
                .font(LeaflyFont.title)
                .foregroundStyle(LeaflyColor.textPrimary)

            Text(LeaflyCopy.Notifications.subtitle)
                .font(LeaflyFont.body)
                .foregroundStyle(LeaflyColor.textSecondary)
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    // MARK: - Permiso

    private var permissionSection: some View {
        LeaflyCard {
            VStack(alignment: .leading, spacing: LeaflySpacing.md) {
                Text(LeaflyCopy.Notifications.Permission.title)
                    .font(LeaflyFont.headline)
                    .foregroundStyle(LeaflyColor.textPrimary)

                HStack(alignment: .center, spacing: LeaflySpacing.sm) {
                    Image(systemName: permissionIcon)
                        .font(.system(size: 16, weight: .regular))
                        .foregroundStyle(permissionColor)

                    Text(permissionText)
                        .font(LeaflyFont.body)
                        .foregroundStyle(LeaflyColor.textPrimary)
                }

                if viewModel.authorizationStatus == .denied {
                    Button(LeaflyCopy.Notifications.Permission.openSettings) {
                        viewModel.openSystemSettings()
                    }
                    .buttonStyle(LeaflyQuietButtonStyle())
                    .accessibilityLabel(LeaflyCopy.Notifications.Permission.openSettings)
                }
            }
        }
    }

    private var permissionIcon: String {
        switch viewModel.authorizationStatus {
        case .authorized: "checkmark.circle.fill"
        case .denied: "exclamationmark.circle.fill"
        case .notDetermined: "questionmark.circle"
        default: "circle"
        }
    }

    private var permissionColor: Color {
        switch viewModel.authorizationStatus {
        case .authorized: LeaflyColor.healthy
        case .denied: LeaflyColor.needsWaterNow
        case .notDetermined: LeaflyColor.textTertiary
        default: LeaflyColor.textTertiary
        }
    }

    private var permissionText: String {
        switch viewModel.authorizationStatus {
        case .authorized: LeaflyCopy.Notifications.Permission.granted
        case .denied: LeaflyCopy.Notifications.Permission.denied
        case .notDetermined: LeaflyCopy.Notifications.Permission.notDetermined
        default: LeaflyCopy.Notifications.Permission.notDetermined
        }
    }

    // MARK: - Hora

    private var timeSection: some View {
        LeaflyCard {
            VStack(alignment: .leading, spacing: LeaflySpacing.md) {
                Text(LeaflyCopy.Notifications.Time.title)
                    .font(LeaflyFont.headline)
                    .foregroundStyle(LeaflyColor.textPrimary)

                Text(LeaflyCopy.Notifications.Time.subtitle)
                    .font(LeaflyFont.footnote)
                    .foregroundStyle(LeaflyColor.textSecondary)

                Button {
                    showingHourPicker = true
                } label: {
                    HStack {
                        Text(timeLabel)
                            .font(LeaflyFont.title3)
                            .foregroundStyle(LeaflyColor.textPrimary)

                        Spacer()

                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(LeaflyColor.textTertiary)
                    }
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Cambiar la hora del aviso")
                .accessibilityHint(timeLabel)
            }
        }
        .sheet(isPresented: $showingHourPicker) {
            hourPickerSheet
        }
    }

    private var timeLabel: String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_ES")
        formatter.timeStyle = .short

        var components = DateComponents()
        components.hour = viewModel.preferences.hour
        components.minute = viewModel.preferences.minute

        guard let date = Calendar.current.date(from: components) else {
            return "\(viewModel.preferences.hour):00"
        }

        return formatter.string(from: date)
    }

    // MARK: - Selector de hora

    private var hourPickerSheet: some View {
        NavigationStack {
            LeaflyScreen {
                VStack(spacing: LeaflySpacing.lg) {
                    DatePicker(
                        "",
                        selection: Binding(
                            get: { pickerDate },
                            set: { newDate in
                                let calendar = Calendar.current
                                viewModel.setPreferredHour(
                                    calendar.component(.hour, from: newDate),
                                    minute: calendar.component(.minute, from: newDate)
                                )
                            }
                        ),
                        displayedComponents: .hourAndMinute
                    )
                    .datePickerStyle(.wheel)
                    .labelsHidden()
                    .frame(maxHeight: 200)

                    Text("Se usará esta hora para los próximos avisos de riego.")
                        .font(LeaflyFont.footnote)
                        .foregroundStyle(LeaflyColor.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .padding(LeaflySpacing.screenHorizontal)
            }
            .navigationTitle(LeaflyCopy.Notifications.Time.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Listo") {
                        showingHourPicker = false
                    }
                }
            }
        }
    }

    private var pickerDate: Date {
        var components = DateComponents()
        components.hour = viewModel.preferences.hour
        components.minute = viewModel.preferences.minute
        return Calendar.current.date(from: components) ?? .now
    }
}

#Preview("Ajustes") {
    SettingsView(
        viewModel: SettingsViewModel(
            center: FakeNotificationCenterForPreview(),
            reminders: NoopWateringReminders()
        )
    )
}

// MARK: - Preview doubles

@MainActor
private final class FakeNotificationCenterForPreview: LocalNotificationCenter {
    func requestAuthorization() async -> Bool { true }
    func currentAuthorizationStatus() async -> UNAuthorizationStatus { .authorized }
    func pendingIdentifiers(withPrefix prefix: String) async -> [String] { [] }
    func add(_ reminder: WateringReminder) async {}
    func removePending(identifiers: [String]) async {}
}
