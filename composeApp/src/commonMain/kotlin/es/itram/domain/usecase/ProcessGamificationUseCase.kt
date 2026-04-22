package es.itram.domain.usecase

import es.itram.domain.model.Achievement
import es.itram.domain.model.GamificationConfig
import es.itram.domain.model.GamificationState
import es.itram.domain.model.PetAction
import es.itram.domain.model.RewardEvent

/**
 * Pure use-case that applies gamification rules to a [GamificationState] given a [PetAction].
 * Has no side effects – does not access any repository.
 * Handles: streak updates, XP/coin rewards (with cooldown), level-ups, and achievement evaluation.
 */
class ProcessGamificationUseCase(
    private val config: GamificationConfig = GamificationConfig,
) {
    operator fun invoke(
        state: GamificationState,
        action: PetAction,
        nowEpochMillis: Long,
    ): Pair<GamificationState, List<RewardEvent>> {
        val events = mutableListOf<RewardEvent>()
        var newState = state

        // 1. Update daily streak
        val nowEpochDay = GamificationState.epochDay(nowEpochMillis)
        when {
            nowEpochDay == state.lastActiveEpochDay -> { /* same day – no change */ }
            nowEpochDay == state.lastActiveEpochDay + 1 -> {
                newState = newState.copy(dailyStreak = state.dailyStreak + 1, lastActiveEpochDay = nowEpochDay)
                events.add(RewardEvent.StreakUpdated(newState.dailyStreak, isNewDay = true))
            }
            else -> {
                newState = newState.copy(dailyStreak = 1, lastActiveEpochDay = nowEpochDay)
                events.add(RewardEvent.StreakUpdated(1, isNewDay = true))
            }
        }

        // 2. Cooldown check: rate-limit XP/coin rewards for repeated feed actions.
        //    lastFeedEpochMillis == -1L means "never fed" → no cooldown.
        val isOnCooldown = action == PetAction.FEED &&
            state.lastFeedEpochMillis >= 0L &&
            (nowEpochMillis - state.lastFeedEpochMillis < config.FEED_COOLDOWN_MILLIS)

        // 3. Award XP and coins if not on cooldown
        if (!isOnCooldown) {
            val xpGain = when (action) {
                PetAction.FEED -> config.XP_PER_FEED
                PetAction.TICK -> config.XP_PER_TICK
            }
            val coinGain = when (action) {
                PetAction.FEED -> config.COINS_PER_FEED
                PetAction.TICK -> config.COINS_PER_TICK
            }
            val newXp = newState.xp + xpGain
            val newCoins = newState.coins + coinGain
            events.add(RewardEvent.XpGained(xpGain, newXp))
            events.add(RewardEvent.CoinsGained(coinGain, newCoins))
            newState = newState.copy(xp = newXp, coins = newCoins)
        }

        // 4. Update feed-specific counters (regardless of cooldown)
        if (action == PetAction.FEED) {
            newState = newState.copy(
                lastFeedEpochMillis = nowEpochMillis,
                totalFeedCount = newState.totalFeedCount + 1,
            )
        }

        // 5. Level-up check from action XP
        val levelFromActionXp = config.levelForXp(newState.xp)
        if (levelFromActionXp > newState.level) {
            for (l in (newState.level + 1)..levelFromActionXp) {
                events.add(RewardEvent.LevelUp(l))
            }
            newState = newState.copy(level = levelFromActionXp)
        }

        // 6. Achievement evaluation (two-pass: action rewards may trigger level-based achievements)
        val firstPassAchievements = evaluateAchievements(newState, state.unlockedAchievements)
        if (firstPassAchievements.isNotEmpty()) {
            var achievementXp = 0
            var achievementCoins = 0
            for (achievement in firstPassAchievements) {
                events.add(RewardEvent.AchievementUnlocked(achievement))
                achievementXp += achievement.xpReward
                achievementCoins += achievement.coinReward
            }
            val xpAfterAchievements = newState.xp + achievementXp
            val coinsAfterAchievements = newState.coins + achievementCoins
            if (achievementXp > 0) events.add(RewardEvent.XpGained(achievementXp, xpAfterAchievements))
            if (achievementCoins > 0) events.add(RewardEvent.CoinsGained(achievementCoins, coinsAfterAchievements))

            val levelAfterAchievements = config.levelForXp(xpAfterAchievements)
            if (levelAfterAchievements > newState.level) {
                for (l in (newState.level + 1)..levelAfterAchievements) {
                    events.add(RewardEvent.LevelUp(l))
                }
            }
            val unlockedSoFar = state.unlockedAchievements + firstPassAchievements
            newState = newState.copy(
                xp = xpAfterAchievements,
                coins = coinsAfterAchievements,
                level = levelAfterAchievements,
                unlockedAchievements = unlockedSoFar,
            )

            // Second pass: catch any achievements newly enabled by achievement-XP level-ups
            val secondPassAchievements = evaluateAchievements(newState, unlockedSoFar)
            if (secondPassAchievements.isNotEmpty()) {
                var bonusXp = 0
                var bonusCoins = 0
                for (achievement in secondPassAchievements) {
                    events.add(RewardEvent.AchievementUnlocked(achievement))
                    bonusXp += achievement.xpReward
                    bonusCoins += achievement.coinReward
                }
                val finalXp = newState.xp + bonusXp
                val finalCoins = newState.coins + bonusCoins
                if (bonusXp > 0) events.add(RewardEvent.XpGained(bonusXp, finalXp))
                if (bonusCoins > 0) events.add(RewardEvent.CoinsGained(bonusCoins, finalCoins))
                val finalLevel = config.levelForXp(finalXp)
                if (finalLevel > newState.level) {
                    for (l in (newState.level + 1)..finalLevel) {
                        events.add(RewardEvent.LevelUp(l))
                    }
                }
                newState = newState.copy(
                    xp = finalXp,
                    coins = finalCoins,
                    level = finalLevel,
                    unlockedAchievements = unlockedSoFar + secondPassAchievements,
                )
            }
        }

        return newState to events
    }

    private fun evaluateAchievements(
        state: GamificationState,
        alreadyUnlocked: Set<Achievement>,
    ): List<Achievement> = buildList {
        if (Achievement.FIRST_FEED !in alreadyUnlocked && state.totalFeedCount >= 1) add(Achievement.FIRST_FEED)
        if (Achievement.WELL_FED !in alreadyUnlocked && state.totalFeedCount >= 10) add(Achievement.WELL_FED)
        if (Achievement.REACH_LEVEL_5 !in alreadyUnlocked && state.level >= 5) add(Achievement.REACH_LEVEL_5)
        if (Achievement.STREAK_3 !in alreadyUnlocked && state.dailyStreak >= 3) add(Achievement.STREAK_3)
        if (Achievement.STREAK_7 !in alreadyUnlocked && state.dailyStreak >= 7) add(Achievement.STREAK_7)
    }
}
