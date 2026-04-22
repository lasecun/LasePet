package es.itram.presentation

import es.itram.currentTimeMillis
import es.itram.data.repository.InMemoryPetRepository
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.ProcessGamificationUseCase
import es.itram.domain.usecase.TickStatsUseCase

object AppContainer {
    private val petRepository = InMemoryPetRepository()
    private val processGamification = ProcessGamificationUseCase()

    fun createPetViewModel(): PetViewModel {
        return PetViewModel(
            createPetUseCase = CreatePetUseCase(petRepository, ::currentTimeMillis),
            getPetStatusUseCase = GetPetStatusUseCase(petRepository),
            tickStatsUseCase = TickStatsUseCase(
                petRepository,
                processGamification,
                nowEpochMillis = ::currentTimeMillis,
            ),
            feedPetUseCase = FeedPetUseCase(
                petRepository,
                processGamification,
                nowEpochMillis = ::currentTimeMillis,
            ),
        )
    }
}

