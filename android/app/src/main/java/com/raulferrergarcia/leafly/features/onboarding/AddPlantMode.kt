package com.raulferrergarcia.leafly.features.onboarding

import androidx.annotation.StringRes
import com.raulferrergarcia.leafly.R

/**
 * Contexto en el que se abre la pantalla de añadir o editar planta.
 * Cambia solo los textos, no el comportamiento.
 */
enum class AddPlantMode(
    @StringRes val eyebrowRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @StringRes val cancelContentDescriptionRes: Int,
) {
    /** Primera planta, dentro del onboarding. */
    FIRST_PLANT(
        eyebrowRes = R.string.add_plant_eyebrow,
        titleRes = R.string.add_plant_title,
        subtitleRes = R.string.add_plant_subtitle,
        cancelContentDescriptionRes = R.string.add_plant_cancel,
    ),

    /** Planta adicional, desde la lista del jardín. */
    ANOTHER_PLANT(
        eyebrowRes = R.string.add_plant_another_eyebrow,
        titleRes = R.string.add_plant_another_title,
        subtitleRes = R.string.add_plant_another_subtitle,
        cancelContentDescriptionRes = R.string.add_plant_cancel_another,
    ),

    /** Edición de una planta existente. */
    EDIT_PLANT(
        eyebrowRes = R.string.edit_plant_eyebrow,
        titleRes = R.string.edit_plant_title,
        subtitleRes = R.string.edit_plant_subtitle,
        cancelContentDescriptionRes = R.string.edit_plant_cancel,
    ),
}
