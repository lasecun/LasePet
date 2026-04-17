package es.itram.domain.usecase

import es.itram.domain.model.HungerState
import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class TickStatsUseCase(
    private val petRepository: PetRepository,
    private val hungerDelta: Int = 5,
    private val energyDelta: Int = -2,
    private val hygieneDelta: Int = -2,
    private val criticalStreakForHealthPenalty: Int = 3,
    private val healthPenalty: Int = 10,
    private val happinessPenalty: Int = 5,
    private val getHungerStateUseCase: GetHungerStateUseCase = GetHungerStateUseCase(),
) {
    operator fun invoke(): Pet? {
        val pet = petRepository.getPet() ?: return null

        val tickUpdatedStats = pet.stats
            .withHungerDelta(hungerDelta)
            .withEnergyDelta(energyDelta)
            .withHygieneDelta(hygieneDelta)
        val isCritical = getHungerStateUseCase(tickUpdatedStats.hunger) == HungerState.CRITICAL
        val nextCriticalStreak = if (isCritical) pet.criticalHungerStreak + 1 else 0

        val nextStats = if (nextCriticalStreak >= criticalStreakForHealthPenalty) {
            tickUpdatedStats
                .withHealthDelta(-healthPenalty)
                .withHappinessDelta(-happinessPenalty)
        } else {
            tickUpdatedStats
        }

        val updated = pet.copy(
            stats = nextStats,
            criticalHungerStreak = nextCriticalStreak,
        )
        petRepository.savePet(updated)
        return updated
    }
}

