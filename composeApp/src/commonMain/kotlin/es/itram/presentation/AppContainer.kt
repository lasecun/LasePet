package es.itram.presentation

import es.itram.data.repository.InMemoryPetRepository
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetHungerStateUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.TickStatsUseCase

object AppContainer {
    private val petRepository = InMemoryPetRepository()

    fun createPetViewModel(): PetViewModel {
        return PetViewModel(
            createPetUseCase = CreatePetUseCase(petRepository),
            getPetStatusUseCase = GetPetStatusUseCase(petRepository),
            tickStatsUseCase = TickStatsUseCase(petRepository),
            feedPetUseCase = FeedPetUseCase(petRepository),
            getHungerStateUseCase = GetHungerStateUseCase(),
        )
    }
}
