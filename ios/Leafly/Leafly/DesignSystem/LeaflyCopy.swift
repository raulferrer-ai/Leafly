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

        /// Búsqueda o filtro sin resultados.
        enum SearchResults {
            static let title = "No se encontraron plantas"
            static let subtitle = "Prueba con otro nombre o quita los filtros."
            static let button = "Limpiar búsqueda"
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

    // MARK: - Ajustes de notificaciones

    enum Notifications {
        static let title = "Ajustes de riego"
        static let subtitle = "Elige a qué hora quieres que te avisemos cuando una planta necesite agua."

        /// Estado del permiso.
        enum Permission {
            static let title = "Permiso de notificaciones"
            static let granted = "Los avisos están activados."
            static let denied = "Los avisos están desactivados."
            static let notDetermined = "Aún no se ha decidido sobre los avisos."
            static let openSettings = "Abrir ajustes del sistema"
        }

        /// Hora preferida.
        enum Time {
            static let title = "Hora del aviso"
            static let subtitle = "Te avisaremos a esta hora cuando una planta necesite riego."
        }
    }
}
