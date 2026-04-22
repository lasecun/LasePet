package es.itram.domain.usecase

import es.itram.domain.model.FoodType
import es.itram.domain.model.Pet
import es.itram.domain.model.PetAction
import es.itram.domain.model.RewardEvent
import es.itram.domain.repository.PetRepository

class FeedPetUseCase(
    private val petRepository: PetRepository,
    private val processGamification: ProcessGamificationUseCase = ProcessGamificationUseCase(),
    private val nowEpochMillis: () -> Long = { 0L },
) {
    operator fun invoke(foodType: FoodType = FoodType.MEAL): Pair<Pet?, List<RewardEvent>> {
        val pet = petRepository.getPet() ?: return null to emptyList()
        val updatedStats = pet.copy(stats = pet.stats.withHungerDelta(-foodType.hungerReduction))
        val (newGamification, rewards) = processGamification(pet.gamification, PetAction.FEED, nowEpochMillis())
        val updated = updatedStats.copy(gamification = newGamification)
        petRepository.savePet(updated)
        return updated to rewards
    }
}

