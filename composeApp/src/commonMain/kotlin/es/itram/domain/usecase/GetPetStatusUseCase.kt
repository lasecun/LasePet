package es.itram.domain.usecase

import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class GetPetStatusUseCase(
    private val petRepository: PetRepository,
) {
    operator fun invoke(): Pet? = petRepository.getPet()
}

