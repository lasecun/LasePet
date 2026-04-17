package es.itram.domain.usecase

import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class SleepPetUseCase(
    private val petRepository: PetRepository,
    private val energyBoost: Int = 25,
    private val hungerIncrease: Int = 5,
) {
    operator fun invoke(): Pet? {
        val pet = petRepository.getPet() ?: return null
        val updatedStats = pet.stats
            .withEnergyDelta(energyBoost)
            .withHungerDelta(hungerIncrease)
        val updated = pet.copy(stats = updatedStats)
        petRepository.savePet(updated)
        return updated
    }
}

