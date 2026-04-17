package es.itram.domain.usecase

import es.itram.domain.model.HungerState

class GetHungerStateUseCase {
    operator fun invoke(hunger: Int): HungerState {
        require(hunger in 0..100) { "hunger must be in 0..100" }
        return when {
            hunger >= 80 -> HungerState.CRITICAL
            hunger >= 60 -> HungerState.ALERT
            else -> HungerState.NORMAL
        }
    }
}

