package es.itram.domain.usecase

import es.itram.domain.model.HungerState
import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class TickStatsUseCase(
    private val petRepository: PetRepository,
    private val hungerDelta: Int = 5,
    private val energyDelta: Int = -2,
    private val hygieneDelta: Int = -2,
    private val healthRecovery: Int = 3,
    private val maxHungerForHealthRecovery: Int = 40,
    private val minEnergyForHealthRecovery: Int = 60,
    private val minHygieneForHealthRecovery: Int = 60,
    private val criticalStreakForHealthPenalty: Int = 3,
    private val healthPenalty: Int = 10,
    private val happinessPenalty: Int = 5,
    private val getHungerStateUseCase: GetHungerStateUseCase = GetHungerStateUseCase(),
) {
    operator fun invoke(nowEpochMillis: Long = 0): Pet? {
        val pet = petRepository.getPet() ?: return null

        val tickUpdatedStats = pet.stats
            .withHungerDelta(hungerDelta)
            .withEnergyDelta(energyDelta)
            .withHygieneDelta(hygieneDelta)
        val isCritical = getHungerStateUseCase(tickUpdatedStats.hunger) == HungerState.CRITICAL
        val nextCriticalStreak = if (isCritical) pet.criticalHungerStreak + 1 else 0

        val hasCriticalPenalty = nextCriticalStreak >= criticalStreakForHealthPenalty
        val penaltyAdjustedStats = if (hasCriticalPenalty) {
            tickUpdatedStats
                .withHealthDelta(-healthPenalty)
                .withHappinessDelta(-happinessPenalty)
        } else {
            tickUpdatedStats
        }

        val canRecoverHealth = !hasCriticalPenalty &&
            penaltyAdjustedStats.hunger <= maxHungerForHealthRecovery &&
            penaltyAdjustedStats.energy >= minEnergyForHealthRecovery &&
            penaltyAdjustedStats.hygiene >= minHygieneForHealthRecovery

        val nextStats = if (canRecoverHealth) {
            penaltyAdjustedStats.withHealthDelta(healthRecovery)
        } else {
            penaltyAdjustedStats
        }

        val updated = pet.copy(
            stats = nextStats,
            criticalHungerStreak = nextCriticalStreak,
            lastTickEpochMillis = if (nowEpochMillis > 0) nowEpochMillis else pet.lastTickEpochMillis,
        )
        petRepository.savePet(updated)
        return updated
    }
}
