package es.itram.domain.usecase

import es.itram.domain.model.HealthState

class GetHealthStateUseCase {
    operator fun invoke(health: Int): HealthState {
        require(health in 0..100) { "health must be in 0..100" }
        return when {
            health < 40 -> HealthState.CRITICAL
            health < 60 -> HealthState.ALERT
            else -> HealthState.NORMAL
        }
    }
}

