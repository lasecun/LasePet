package es.itram.domain.usecase

import es.itram.domain.model.PetEvent
import es.itram.domain.repository.PetRepository

class GetRecentPetEventsUseCase(
    private val petRepository: PetRepository,
) {
    operator fun invoke(petId: String, limit: Int = 5): List<PetEvent> {
        return petRepository.getRecentEvents(petId = petId, limit = limit)
    }
}

