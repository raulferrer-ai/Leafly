//
//  PlantPhotoProcessor.swift
//  Leafly
//
//  Created by RaulF on 09/11/2026.
//

import UIKit

/// Prepara una foto para guardarla junto a una planta.
///
/// Reduce el lado más largo a `maxDimension` píxeles y la comprime en JPEG.
/// Así la base de datos y la memoria se mantienen ligeras, y la foto se ve
/// nítida en la lista y en el detalle.
nonisolated enum PlantPhotoProcessor {

    /// Lado más largo permitido, en píxeles.
    static let maxDimension: CGFloat = 1_400

    /// Calidad JPEG: suficiente para una foto de planta, sin pesar de más.
    static let compressionQuality: CGFloat = 0.8

    /// Procesa los datos originales (cámara o galería) y devuelve un JPEG listo.
    nonisolated static func processedJPEGData(from data: Data) -> Data? {
        guard let image = UIImage(data: data) else { return nil }
        return downscaled(image).jpegData(compressionQuality: compressionQuality)
    }

    /// Reduce la imagen si supera el tamaño máximo. Mantiene la proporción.
    nonisolated private static func downscaled(_ image: UIImage) -> UIImage {
        let longestSide = max(image.size.width, image.size.height)
        guard longestSide > maxDimension, longestSide > 0 else { return image }

        let scale = maxDimension / longestSide
        let targetSize = CGSize(
            width: max(1, (image.size.width * scale).rounded()),
            height: max(1, (image.size.height * scale).rounded())
        )

        let format = UIGraphicsImageRendererFormat.default()
        format.scale = 1
        format.opaque = true

        return UIGraphicsImageRenderer(size: targetSize, format: format).image { _ in
            image.draw(in: CGRect(origin: .zero, size: targetSize))
        }
    }
}
