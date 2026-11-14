//
//  AddPlantViewModel.swift
//  Leafly
//
//  Created by RaulF on 06/11/2026.
//

import Foundation

/// Estado y lógica de la pantalla de añadir y editar planta.
@MainActor
@Observable
final class AddPlantViewModel {

    var name: String = ""
    var selectedType: PlantType?
    /// Foto elegida para la planta. Se mantiene solo en memoria hasta guardar.
    private(set) var photoData: Data?

    /// Planta que se está editando, si hay una. `nil` en modo alta.
    private(set) var editingPlant: Plant?

    private(set) var nameError: String?
    private(set) var saveError: String?
    private(set) var isSaving = false

    private let repository: any PlantRepository

    init(repository: any PlantRepository) {
        self.repository = repository
    }

    /// El tipo es opcional: tocarlo de nuevo lo deselecciona.
    func toggleType(_ type: PlantType) {
        selectedType = selectedType == type ? nil : type
    }

    func clearType() {
        selectedType = nil
    }

    /// Guarda en memoria la foto elegida. Solo se persiste al guardar la planta.
    func setPhotoData(_ data: Data?) {
        photoData = data
    }

    /// Quita la foto elegida; la planta se guardará sin imagen.
    func removePhoto() {
        photoData = nil
    }

    /// El error desaparece en cuanto la persona empieza a escribir.
    func clearNameError() {
        guard nameError != nil else { return }
        nameError = nil
    }

    /// Precarga los datos de una planta existente para editarla.
    func load(_ plant: Plant) {
        name = plant.name
        selectedType = plant.type
        photoData = plant.photoData
        editingPlant = plant
    }

    @discardableResult
    func save() async -> Bool {
        let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)

        guard !trimmedName.isEmpty else {
            nameError = "Ponle un nombre a tu planta para poder guardarla."
            return false
        }

        guard !isSaving else { return false }

        isSaving = true
        saveError = nil
        defer { isSaving = false }

        // Modo edición: actualizar la planta existente preservando id y fechas.
        if let editingPlant {
            var updatedPlant = editingPlant
            updatedPlant.name = trimmedName
            updatedPlant.type = selectedType
            updatedPlant.photoData = photoData

            do {
                try await repository.update(updatedPlant)
                return true
            } catch {
                saveError = "No hemos podido guardar los cambios. Inténtalo de nuevo."
                return false
            }
        }

        // Modo alta: crear nueva planta.
        let plant = Plant(name: trimmedName, type: selectedType, photoData: photoData)

        do {
            try await repository.add(plant)
            return true
        } catch {
            saveError = "No hemos podido guardar tu planta. Inténtalo de nuevo."
            return false
        }
    }
}
