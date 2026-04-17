package es.itram.presentation

import es.itram.domain.model.EnergyState
import es.itram.domain.model.HealthState
import es.itram.domain.model.HappinessState
import es.itram.domain.model.HungerState
import es.itram.domain.model.HygieneState

data class PetUiState(
    val hasPet: Boolean = false,
    val petName: String = "",
    val speciesName: String = "",
    val hunger: Int = 0,
    val happiness: Int = 0,
    val energy: Int = 0,
    val hygiene: Int = 0,
    val health: Int = 0,
    val hungerState: HungerState? = null,
    val happinessState: HappinessState? = null,
    val energyState: EnergyState? = null,
    val hygieneState: HygieneState? = null,
    val healthState: HealthState? = null,
    val healthRecoveryMessage: String? = null,
    val errorMessage: String? = null,
)
