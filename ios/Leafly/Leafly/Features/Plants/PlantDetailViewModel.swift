//
//  PlantDetailViewModel.swift
//  Leafly
//
//  Created by RaulF on 07/11/2026.
//

import Foundation

/// Estado y lógica de la pantalla de detalle.
///
/// La vista no calcula nada: el ViewModel expone el estado de riego y el
/// resumen ya resueltos, y se encarga de persistir el riego.
@MainActor
@Observable
final class PlantDetailViewModel {

    /// Planta que se está mostrando. Se actualiza al marcar el riego.
    private(set) var plant: Plant

    /// Evita dobles pulsaciones mientras se guarda el riego.
    private(set) var isSaving = false

    /// Mensaje calmado cuando el riego no se pudo guardar.
    private(set) var errorMessage: String?

    /// Historial de fotos de la planta, de más reciente a más antigua.
    private(set) var photos: [PlantPhoto] = []

    /// `true` mientras se carga el historial por primera vez.
    private(set) var isLoadingPhotos = false

    /// `true` mientras se guarda una foto nueva en el historial.
    private(set) var isAddingPhoto = false

    /// Mensaje calmado cuando el historial no se pudo cargar o guardar.
    private(set) var photoErrorMessage: String?

    private let repository: any PlantRepository
    private let reminders: any WateringReminderScheduling
    private let now: () -> Date

    init(
        plant: Plant,
        repository: any PlantRepository,
        reminders: any WateringReminderScheduling,
        now: @escaping () -> Date = Date.init
    ) {
        self.plant = plant
        self.repository = repository
        self.reminders = reminders
        self.now = now
    }

    /// Estado de riego actual de la planta.
    var status: WateringStatus {
        plant.wateringStatus(on: now())
    }

    /// Texto cercano con la última vez que se regó (o cuándo se añadió).
    var summary: String {
        plant.wateringSummary(on: now())
    }

    /// Marca la planta como regada ahora mismo y lo persiste.
    ///
    /// - Returns: `true` si el riego se guardó correctamente.
    @discardableResult
    func markAsWatered() async -> Bool {
        guard !isSaving else { return false }

        isSaving = true
        errorMessage = nil
        defer { isSaving = false }

        var wateredPlant = plant
        wateredPlant.lastWateredAt = now()

        do {
            try await repository.update(wateredPlant)
            plant = wateredPlant

            // Ya está al día: su recordatorio pendiente deja de tener sentido.
            await reminders.cancelReminder(for: wateredPlant.id)
            return true
        } catch {
            errorMessage = "No hemos podido guardar el riego. Inténtalo de nuevo."
            return false
        }
    }

    // MARK: - Historial de fotos

    /// Carga el historial de fotos de la planta.
    ///
    /// Si algo falla, se muestra un mensaje calmado y la planta sigue siendo
    /// utilizable; el historial simplemente se queda como estaba.
    func loadPhotoHistory() async {
        guard !isLoadingPhotos else { return }

        isLoadingPhotos = true
        photoErrorMessage = nil
        defer { isLoadingPhotos = false }

        do {
            let fetched = try await repository.fetchPhotos(for: plant.id)
            photos = fetched.sorted { $0.addedAt > $1.addedAt }
        } catch {
            photoErrorMessage = "No hemos podido cargar las fotos. Inténtalo de nuevo."
        }
    }

    /// Añade una foto ya procesada al historial y la convierte en la principal.
    ///
    /// - Returns: `true` si la foto se guardó correctamente.
    @discardableResult
    func addPhoto(_ data: Data) async -> Bool {
        guard !isAddingPhoto else { return false }

        isAddingPhoto = true
        photoErrorMessage = nil
        defer { isAddingPhoto = false }

        let photo = PlantPhoto(
            plantID: plant.id,
            addedAt: now(),
            data: data
        )

        do {
            try await repository.addPhoto(photo)

            // La foto recién añadida es la más reciente.
            photos.insert(photo, at: 0)
            plant.photoData = data
            return true
        } catch {
            photoErrorMessage = "No hemos podido guardar la foto. Inténtalo de nuevo."
            return false
        }
    }

    /// Vuelve a cargar los datos de la planta desde el repositorio.
    /// Se usa tras editar la planta para reflejar los cambios en la UI.
    func reloadPlant() async {
        do {
            let plants = try await repository.fetchPlants()
            if let updated = plants.first(where: { $0.id == plant.id }) {
                plant = updated
            }
        } catch {
            errorMessage = "No hemos podido cargar los cambios. Inténtalo de nuevo."
        }
    }
}
