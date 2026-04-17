package es.itram.domain.usecase

import es.itram.domain.model.EnergyState

class GetEnergyStateUseCase {
    operator fun invoke(energy: Int): EnergyState {
        require(energy in 0..100) { "energy must be in 0..100" }
        return when {
            energy < 40 -> EnergyState.CRITICAL
            energy < 60 -> EnergyState.ALERT
            else -> EnergyState.NORMAL
        }
    }
}

