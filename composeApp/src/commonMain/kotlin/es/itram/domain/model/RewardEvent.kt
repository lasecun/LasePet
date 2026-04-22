package es.itram.domain.model

sealed class RewardEvent {
    data class XpGained(val amount: Int, val totalXp: Int) : RewardEvent()
    data class CoinsGained(val amount: Int, val totalCoins: Int) : RewardEvent()
    data class LevelUp(val newLevel: Int) : RewardEvent()
    data class AchievementUnlocked(val achievement: Achievement) : RewardEvent()
    data class StreakUpdated(val days: Int, val isNewDay: Boolean) : RewardEvent()
}
