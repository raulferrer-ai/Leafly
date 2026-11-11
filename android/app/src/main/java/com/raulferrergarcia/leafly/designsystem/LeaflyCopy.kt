package com.raulferrergarcia.leafly.designsystem

/**
 * Copy centralizada para estados vacíos y de error.
 *
 * Evita strings mágicos dispersos por la UI y garantiza que el tono sea
 * siempre tranquilo, humano y en español, siguiendo el design system.
 */
object LeaflyCopy {

    // MARK: - Estados vacíos

    object Empty {

        /** Pantalla de lista cuando no hay plantas. */
        object Plants {
            val title = "Tu jardín está esperando"
            val subtitle = "Añade una planta para empezar a cuidarla con calma."
            val button = "Añadir mi primera planta"
        }

        /** Historial de fotos de una planta sin imágenes aún. */
        object PhotoHistory {
            val title = "Aún no hay fotos"
            val subtitle = "Cuando quieras, guarda una foto y podrás ver cómo crece con el tiempo."
            val button = "Añadir la primera foto"
        }
    }

    // MARK: - Errores

    object Error {

        /** Fallo al cargar la lista de plantas. */
        object Plants {
            val title = "No hemos podido cargar tus plantas"
            val message = "Inténtalo de nuevo en un momento."
            val retry = "Reintentar"
        }

        /** Fallo al cargar o guardar el historial de fotos. */
        object PhotoHistory {
            val message = "No hemos podido cargar las fotos. Inténtalo de nuevo."
            val retry = "Reintentar"
        }

        /** Fallo al marcar una planta como regada. */
        object Watering {
            val message = "No hemos podido guardar el riego. Inténtalo de nuevo."
        }

        /** Fallo al guardar una foto nueva en el historial. */
        object AddPhoto {
            val message = "No hemos podido guardar la foto. Inténtalo de nuevo."
        }
    }
}
