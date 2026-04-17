package es.itram.presentation

import es.itram.domain.model.HungerState

data class PetUiState(
    val hasPet: Boolean = false,
    val petName: String = "",
    val speciesName: String = "",
    val hunger: Int = 0,
    val happiness: Int = 0,
    val health: Int = 0,
    val hungerState: HungerState? = null,
    val errorMessage: String? = null,
)
