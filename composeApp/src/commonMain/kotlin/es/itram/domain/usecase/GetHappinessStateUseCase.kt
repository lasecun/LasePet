package es.itram.domain.usecase

import es.itram.domain.model.HappinessState

class GetHappinessStateUseCase {
    operator fun invoke(happiness: Int): HappinessState {
        require(happiness in 0..100) { "happiness must be in 0..100" }
        return when {
            happiness < 40 -> HappinessState.CRITICAL
            happiness < 60 -> HappinessState.ALERT
            else -> HappinessState.NORMAL
        }
    }
}

