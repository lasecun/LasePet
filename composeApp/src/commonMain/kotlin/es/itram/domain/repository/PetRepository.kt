package es.itram.domain.repository

import es.itram.domain.model.PetEvent
import es.itram.domain.model.Pet

interface PetRepository {
    fun getPet(): Pet?
    fun savePet(pet: Pet)
    fun saveEvent(event: PetEvent)
    fun getRecentEvents(petId: String, limit: Int = 5): List<PetEvent>
}

