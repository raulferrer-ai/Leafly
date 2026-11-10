//
//  PlantAvatar.swift
//  Leafly
//
//  Created by RaulF on 09/11/2026.
//

import SwiftUI
import UIKit

/// Avatar circular de una planta: muestra su foto si la tiene y, si no,
/// el icono de su tipo. Se comparte entre la lista, el detalle y el alta.
struct PlantAvatar: View {
    let plant: Plant
    var size: CGFloat = 58

    var body: some View {
        ZStack {
            Circle()
                .fill(LeaflyColor.primarySoft)

            if let photoData = plant.photoData,
               let image = PlantPhotoDecoder.image(from: photoData) {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .frame(width: size, height: size)
                    .clipShape(Circle())
            } else {
                Image(systemName: plant.type?.symbolName ?? "leaf.fill")
                    .font(.system(size: size * 0.38, weight: .semibold))
                    .foregroundStyle(LeaflyColor.secondary)
            }
        }
        .frame(width: size, height: size)
        .accessibilityHidden(true)
    }
}

/// Decodifica las fotos una sola vez y las mantiene en memoria mientras la app vive.
@MainActor
enum PlantPhotoDecoder {
    private static let cache: NSCache<NSData, UIImage> = {
        let cache = NSCache<NSData, UIImage>()
        cache.countLimit = 24
        return cache
    }()

    static func image(from data: Data) -> UIImage? {
        let key = data as NSData

        if let cached = cache.object(forKey: key) {
            return cached
        }

        guard let image = UIImage(data: data) else { return nil }
        cache.setObject(image, forKey: key)
        return image
    }
}
