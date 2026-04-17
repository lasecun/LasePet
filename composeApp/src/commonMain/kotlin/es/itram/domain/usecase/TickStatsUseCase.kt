package es.itram.domain.usecase

import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class TickStatsUseCase(
    private val petRepository: PetRepository,
    private val hungerDelta: Int = 5,
) {
    operator fun invoke(): Pet? {
        val pet = petRepository.getPet() ?: return null
        val updated = pet.copy(stats = pet.stats.withHungerDelta(hungerDelta))
        petRepository.savePet(updated)
        return updated
    }
}

