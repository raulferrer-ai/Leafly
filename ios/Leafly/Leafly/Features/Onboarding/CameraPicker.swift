//
//  CameraPicker.swift
//  Leafly
//
//  Created by RaulF on 09/11/2026.
//

import SwiftUI
import UIKit

/// Cámara nativa envuelta para SwiftUI.
///
/// Solo se presenta cuando `UIImagePickerController` confirma que hay cámara.
/// Al cancelar no se llama a `onCapture`, así una foto ya elegida se conserva.
struct CameraPicker: UIViewControllerRepresentable {
    /// Recibe el JPEG original de la foto tomada, listo para procesar.
    let onCapture: (Data) -> Void

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.cameraCaptureMode = .photo
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(
        _ uiViewController: UIImagePickerController,
        context: Context
    ) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onCapture: onCapture)
    }

    @MainActor
    final class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        private let onCapture: (Data) -> Void

        init(onCapture: @escaping (Data) -> Void) {
            self.onCapture = onCapture
        }

        func imagePickerController(
            _ picker: UIImagePickerController,
            didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
        ) {
            if let image = info[.originalImage] as? UIImage,
               let data = image.jpegData(compressionQuality: 1) {
                onCapture(data)
            }
            picker.dismiss(animated: true)
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            picker.dismiss(animated: true)
        }
    }
}
