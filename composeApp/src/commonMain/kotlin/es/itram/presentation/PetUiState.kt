package es.itram.presentation

import es.itram.domain.model.Achievement
import es.itram.domain.model.RewardEvent

data class PetUiState(
    val hasPet: Boolean = false,
    val petName: String = "",
    val speciesName: String = "",
    val hunger: Int = 0,
    val errorMessage: String? = null,
    // Gamification
    val level: Int = 1,
    /** XP accumulated within the current level. */
    val xpProgress: Int = 0,
    /** Total XP needed to advance from the current level to the next. */
    val xpForNextLevel: Int = 100,
    val coins: Int = 0,
    val dailyStreak: Int = 0,
    /** Reward events produced by the most recent player action. Cleared on next action. */
    val recentRewards: List<RewardEvent> = emptyList(),
    val unlockedAchievements: Set<Achievement> = emptySet(),
)


