package es.itram.domain.usecase

import es.itram.domain.model.FoodType
import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class FeedPetUseCase(
    private val petRepository: PetRepository,
) {
    operator fun invoke(foodType: FoodType = FoodType.MEAL): Pet? {
        val pet = petRepository.getPet() ?: return null
        val updated = pet.copy(stats = pet.stats.withHungerDelta(-foodType.hungerReduction))
        petRepository.savePet(updated)
        return updated
    }
}
