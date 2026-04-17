package es.itram.domain.usecase

import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class CleanPetUseCase(
    private val petRepository: PetRepository,
    private val hygieneBoost: Int = 20,
    private val happinessBoost: Int = 5,
) {
    operator fun invoke(): Pet? {
        val pet = petRepository.getPet() ?: return null
        val updatedStats = pet.stats
            .withHygieneDelta(hygieneBoost)
            .withHappinessDelta(happinessBoost)
        val updated = pet.copy(stats = updatedStats)
        petRepository.savePet(updated)
        return updated
    }
}

