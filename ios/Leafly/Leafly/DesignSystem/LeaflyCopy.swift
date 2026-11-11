//
//  LeaflyCopy.swift
//  Leafly
//
//  Copy centralizada para estados vacíos y de error.
//
//  Evita strings mágicos dispersos por la UI y garantiza que el tono sea
//  siempre tranquilo, humano y en español, siguiendo el design system.
//

import Foundation

enum LeaflyCopy {

    // MARK: - Estados vacíos

    enum Empty {

        /// Pantalla de lista cuando no hay plantas.
        enum Plants {
            static let title = "Tu jardín está esperando"
            static let subtitle = "Añade una planta para empezar a cuidarla con calma."
            static let button = "Añadir mi primera planta"
        }

        /// Historial de fotos de una planta sin imágenes aún.
        enum PhotoHistory {
            static let title = "Aún no hay fotos"
            static let subtitle = "Cuando quieras, guarda una foto y podrás ver cómo crece con el tiempo."
            static let button = "Añadir la primera foto"
        }
    }

    // MARK: - Errores

    enum Error {

        /// Fallo al cargar la lista de plantas.
        enum Plants {
            static let title = "No hemos podido cargar tus plantas"
            static let message = "Inténtalo de nuevo en un momento."
            static let retry = "Reintentar"
        }

        /// Fallo al cargar o guardar el historial de fotos.
        enum PhotoHistory {
            static let message = "No hemos podido cargar las fotos. Inténtalo de nuevo."
            static let retry = "Reintentar"
        }

        /// Fallo al marcar una planta como regada.
        enum Watering {
            static let message = "No hemos podido guardar el riego. Inténtalo de nuevo."
        }

        /// Fallo al guardar una foto nueva en el historial.
        enum AddPhoto {
            static let message = "No hemos podido guardar la foto. Inténtalo de nuevo."
        }
    }
}
