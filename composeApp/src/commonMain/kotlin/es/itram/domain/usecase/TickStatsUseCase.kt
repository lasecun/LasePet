package es.itram.domain.usecase

import es.itram.domain.model.HungerState
import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class TickStatsUseCase(
    private val petRepository: PetRepository,
    private val hungerDelta: Int = 5,
    private val criticalStreakForHealthPenalty: Int = 3,
    private val healthPenalty: Int = 10,
    private val happinessPenalty: Int = 5,
    private val getHungerStateUseCase: GetHungerStateUseCase = GetHungerStateUseCase(),
) {
    operator fun invoke(): Pet? {
        val pet = petRepository.getPet() ?: return null

        val hungerUpdatedStats = pet.stats.withHungerDelta(hungerDelta)
        val isCritical = getHungerStateUseCase(hungerUpdatedStats.hunger) == HungerState.CRITICAL
        val nextCriticalStreak = if (isCritical) pet.criticalHungerStreak + 1 else 0

        val nextStats = if (nextCriticalStreak >= criticalStreakForHealthPenalty) {
            hungerUpdatedStats
                .withHealthDelta(-healthPenalty)
                .withHappinessDelta(-happinessPenalty)
        } else {
            hungerUpdatedStats
        }

        val updated = pet.copy(
            stats = nextStats,
            criticalHungerStreak = nextCriticalStreak,
        )
        petRepository.savePet(updated)
        return updated
    }
}

