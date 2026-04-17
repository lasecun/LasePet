package es.itram.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import es.itram.domain.model.FoodType
import es.itram.domain.model.PetSpecies
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.CleanPetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetHappinessStateUseCase
import es.itram.domain.usecase.GetHungerStateUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.PlayWithPetUseCase
import es.itram.domain.usecase.SleepPetUseCase
import es.itram.domain.usecase.TickStatsUseCase

class PetViewModel(
    private val createPetUseCase: CreatePetUseCase,
    private val getPetStatusUseCase: GetPetStatusUseCase,
    private val tickStatsUseCase: TickStatsUseCase,
    private val feedPetUseCase: FeedPetUseCase,
    private val playWithPetUseCase: PlayWithPetUseCase,
    private val cleanPetUseCase: CleanPetUseCase,
    private val sleepPetUseCase: SleepPetUseCase,
    private val getHungerStateUseCase: GetHungerStateUseCase,
    private val getHappinessStateUseCase: GetHappinessStateUseCase,
) {
    var uiState by mutableStateOf(PetUiState())
        private set

    init {
        refreshUiState(errorMessage = null)
    }

    fun createPet(name: String, species: PetSpecies) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            refreshUiState(errorMessage = "Escribe un nombre para tu mascota")
            return
        }
        createPetUseCase(cleanName, species)
        refreshUiState(errorMessage = null)
    }

    fun tick() {
        tickStatsUseCase()
        refreshUiState(errorMessage = null)
    }

    fun feed(foodType: FoodType = FoodType.MEAL) {
        feedPetUseCase(foodType)
        refreshUiState(errorMessage = null)
    }

    fun play() {
        playWithPetUseCase()
        refreshUiState(errorMessage = null)
    }

    fun clean() {
        cleanPetUseCase()
        refreshUiState(errorMessage = null)
    }

    fun sleep() {
        sleepPetUseCase()
        refreshUiState(errorMessage = null)
    }

    private fun refreshUiState(errorMessage: String?) {
        val pet = getPetStatusUseCase()
        uiState = if (pet == null) {
            PetUiState(errorMessage = errorMessage)
        } else {
            PetUiState(
                hasPet = true,
                petName = pet.name,
                speciesName = pet.species.displayName,
                hunger = pet.stats.hunger,
                happiness = pet.stats.happiness,
                energy = pet.stats.energy,
                hygiene = pet.stats.hygiene,
                health = pet.stats.health,
                hungerState = getHungerStateUseCase(pet.stats.hunger),
                happinessState = getHappinessStateUseCase(pet.stats.happiness),
                errorMessage = errorMessage,
            )
        }
    }
}
