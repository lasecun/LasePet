package es.itram.domain.usecase

import es.itram.domain.model.HygieneState

class GetHygieneStateUseCase {
    operator fun invoke(hygiene: Int): HygieneState {
        require(hygiene in 0..100) { "hygiene must be in 0..100" }
        return when {
            hygiene < 40 -> HygieneState.CRITICAL
            hygiene < 60 -> HygieneState.ALERT
            else -> HygieneState.NORMAL
        }
    }
}

