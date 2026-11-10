//
//  PlantPhotoProcessorTests.swift
//  LeaflyTests
//
//  Created by RaulF on 09/11/2026.
//

import XCTest
import UIKit
@testable import Leafly

@MainActor
final class PlantPhotoProcessorTests: XCTestCase {

    func testDownscalesLargePhotosAndKeepsProportions() throws {
        let original = makeImage(width: 3_000, height: 1_500)
        let originalData = try XCTUnwrap(original.jpegData(compressionQuality: 1))

        let processedData = try XCTUnwrap(
            PlantPhotoProcessor.processedJPEGData(from: originalData)
        )
        let processed = try XCTUnwrap(UIImage(data: processedData))

        XCTAssertEqual(
            max(processed.size.width, processed.size.height),
            PlantPhotoProcessor.maxDimension,
            accuracy: 1
        )
        XCTAssertEqual(processed.size.width / processed.size.height, 2, accuracy: 0.02)
        XCTAssertLessThan(processedData.count, originalData.count)
    }

    func testKeepsSmallPhotosAtTheirSize() throws {
        let original = makeImage(width: 400, height: 300)
        let originalData = try XCTUnwrap(original.jpegData(compressionQuality: 1))

        let processedData = try XCTUnwrap(
            PlantPhotoProcessor.processedJPEGData(from: originalData)
        )
        let processed = try XCTUnwrap(UIImage(data: processedData))

        XCTAssertEqual(processed.size.width, 400, accuracy: 1)
        XCTAssertEqual(processed.size.height, 300, accuracy: 1)
    }

    func testInvalidDataReturnsNil() {
        let invalidData = Data("esto no es una imagen".utf8)

        XCTAssertNil(PlantPhotoProcessor.processedJPEGData(from: invalidData))
    }

    private func makeImage(width: CGFloat, height: CGFloat) -> UIImage {
        let format = UIGraphicsImageRendererFormat.default()
        format.scale = 1

        return UIGraphicsImageRenderer(
            size: CGSize(width: width, height: height),
            format: format
        ).image { context in
            UIColor.systemGreen.setFill()
            context.fill(CGRect(x: 0, y: 0, width: width, height: height))
        }
    }
}
