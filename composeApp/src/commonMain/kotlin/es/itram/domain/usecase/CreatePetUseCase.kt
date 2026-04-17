package es.itram.domain.usecase

import es.itram.domain.model.Pet
import es.itram.domain.model.PetSpecies
import es.itram.domain.model.Stats
import es.itram.domain.repository.PetRepository
import kotlin.random.Random

class CreatePetUseCase(
    private val petRepository: PetRepository,
    private val currentTimeMillis: () -> Long = { 0L },
) {
    operator fun invoke(name: String, species: PetSpecies): Pet {
        val pet = Pet(
            id = Random.nextLong().toString(),
            name = name,
            species = species,
            stats = Stats(
                hunger = 20,
                happiness = 70,
                energy = 70,
                hygiene = 70,
                health = 90,
            ),
            createdAtEpochMillis = currentTimeMillis(),
        )
        petRepository.savePet(pet)
        return pet
    }
}
