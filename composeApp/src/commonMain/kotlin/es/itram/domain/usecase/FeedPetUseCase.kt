package es.itram.domain.usecase

import es.itram.domain.model.FoodType
import es.itram.domain.model.HungerState
import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class FeedPetUseCase(
    private val petRepository: PetRepository,
    private val getHungerStateUseCase: GetHungerStateUseCase = GetHungerStateUseCase(),
) {
    operator fun invoke(foodType: FoodType = FoodType.MEAL): Pet? {
        val pet = petRepository.getPet() ?: return null
        val stats = pet.stats.withHungerDelta(-foodType.hungerReduction)
        val isCritical = getHungerStateUseCase(stats.hunger) == HungerState.CRITICAL
        val updated = pet.copy(
            stats = stats,
            criticalHungerStreak = if (isCritical) pet.criticalHungerStreak else 0,
        )
        petRepository.savePet(updated)
        return updated
    }
}
