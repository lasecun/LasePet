package es.itram.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import es.itram.domain.model.FoodType
import es.itram.domain.model.GamificationConfig
import es.itram.domain.model.PetSpecies
import es.itram.domain.model.RewardEvent
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.TickStatsUseCase

class PetViewModel(
    private val createPetUseCase: CreatePetUseCase,
    private val getPetStatusUseCase: GetPetStatusUseCase,
    private val tickStatsUseCase: TickStatsUseCase,
    private val feedPetUseCase: FeedPetUseCase,
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
        val (_, rewards) = tickStatsUseCase()
        refreshUiState(errorMessage = null, recentRewards = rewards)
    }

    fun feed(foodType: FoodType = FoodType.MEAL) {
        val (_, rewards) = feedPetUseCase(foodType)
        refreshUiState(errorMessage = null, recentRewards = rewards)
    }

    private fun refreshUiState(
        errorMessage: String?,
        recentRewards: List<RewardEvent> = emptyList(),
    ) {
        val pet = getPetStatusUseCase()
        uiState = if (pet == null) {
            PetUiState(errorMessage = errorMessage)
        } else {
            val g = pet.gamification
            val xpIntoLevel = g.xp - GamificationConfig.xpNeededForLevel(g.level)
            PetUiState(
                hasPet = true,
                petName = pet.name,
                speciesName = pet.species.displayName,
                hunger = pet.stats.hunger,
                errorMessage = errorMessage,
                level = g.level,
                xpProgress = xpIntoLevel,
                xpForNextLevel = GamificationConfig.xpSpanForLevel(g.level),
                coins = g.coins,
                dailyStreak = g.dailyStreak,
                recentRewards = recentRewards,
                unlockedAchievements = g.unlockedAchievements,
            )
        }
    }
}

