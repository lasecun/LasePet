package es.itram.presentation

import es.itram.domain.repository.PetRepository
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.CleanPetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetEnergyStateUseCase
import es.itram.domain.usecase.GetHealthStateUseCase
import es.itram.domain.usecase.GetHappinessStateUseCase
import es.itram.domain.usecase.GetHungerStateUseCase
import es.itram.domain.usecase.GetHygieneStateUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.GetRecentPetEventsUseCase
import es.itram.domain.usecase.PlayWithPetUseCase
import es.itram.domain.usecase.RecordPetEventUseCase
import es.itram.domain.usecase.SleepPetUseCase
import es.itram.domain.usecase.TickStatsUseCase

object AppContainer {
    fun createPetViewModel(petRepository: PetRepository): PetViewModel {
        return PetViewModel(
            createPetUseCase = CreatePetUseCase(petRepository),
            getPetStatusUseCase = GetPetStatusUseCase(petRepository),
            tickStatsUseCase = TickStatsUseCase(petRepository),
            feedPetUseCase = FeedPetUseCase(petRepository),
            playWithPetUseCase = PlayWithPetUseCase(petRepository),
            cleanPetUseCase = CleanPetUseCase(petRepository),
            sleepPetUseCase = SleepPetUseCase(petRepository),
            recordPetEventUseCase = RecordPetEventUseCase(petRepository),
            getRecentPetEventsUseCase = GetRecentPetEventsUseCase(petRepository),
            getHungerStateUseCase = GetHungerStateUseCase(),
            getHappinessStateUseCase = GetHappinessStateUseCase(),
            getEnergyStateUseCase = GetEnergyStateUseCase(),
            getHygieneStateUseCase = GetHygieneStateUseCase(),
            getHealthStateUseCase = GetHealthStateUseCase(),
        )
    }
}
