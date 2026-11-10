//
//  CameraAccess.swift
//  Leafly
//
//  Created by RaulF on 10/11/2026.
//

import AVFoundation
import UIKit

/// Acceso calmado a la cámara, compartido por el alta y el historial de fotos.
///
/// Nunca lanza errores: si no hay cámara o se deniega el permiso, devuelve
/// `false` para que la persona pueda seguir con la galería o sin foto.
@MainActor
enum CameraAccess {

    /// Solo se ofrece la cámara si el dispositivo puede tomar fotos.
    static var isAvailable: Bool {
        UIImagePickerController.isSourceTypeAvailable(.camera)
    }

    /// Pide permiso si hace falta y devuelve si se puede abrir la cámara.
    static func requestAuthorization() async -> Bool {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            return true
        case .notDetermined:
            return await AVCaptureDevice.requestAccess(for: .video)
        default:
            return false
        }
    }
}
