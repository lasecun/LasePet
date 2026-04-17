package es.itram.domain.usecase

import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class PlayWithPetUseCase(
    private val petRepository: PetRepository,
    private val happinessBoost: Int = 10,
    private val energyCost: Int = 10,
) {
    operator fun invoke(): Pet? {
        val pet = petRepository.getPet() ?: return null
        val updatedStats = pet.stats
            .withHappinessDelta(happinessBoost)
            .withEnergyDelta(-energyCost)
        val updated = pet.copy(stats = updatedStats)
        petRepository.savePet(updated)
        return updated
    }
}

