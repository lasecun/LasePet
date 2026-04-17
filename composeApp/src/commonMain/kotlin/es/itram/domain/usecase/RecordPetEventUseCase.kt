package es.itram.domain.usecase

import es.itram.domain.model.PetEvent
import es.itram.domain.repository.PetRepository

class RecordPetEventUseCase(
    private val petRepository: PetRepository,
) {
    operator fun invoke(event: PetEvent) {
        petRepository.saveEvent(event)
    }
}

