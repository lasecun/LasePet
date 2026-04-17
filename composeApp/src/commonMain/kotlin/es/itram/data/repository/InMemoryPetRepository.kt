package es.itram.data.repository

import es.itram.domain.model.PetEvent
import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class InMemoryPetRepository : PetRepository {
    private var pet: Pet? = null
    private val events = mutableListOf<PetEvent>()

    override fun getPet(): Pet? = pet

    override fun savePet(pet: Pet) {
        this.pet = pet
    }

    override fun saveEvent(event: PetEvent) {
        events += event
    }

    override fun getRecentEvents(petId: String, limit: Int): List<PetEvent> {
        return events
            .asReversed()
            .asSequence()
            .filter { it.petId == petId }
            .take(limit)
            .toList()
    }
}

