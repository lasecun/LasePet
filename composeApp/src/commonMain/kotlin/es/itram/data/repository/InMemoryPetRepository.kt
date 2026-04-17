package es.itram.data.repository

import es.itram.domain.model.Pet
import es.itram.domain.repository.PetRepository

class InMemoryPetRepository : PetRepository {
    private var pet: Pet? = null

    override fun getPet(): Pet? = pet

    override fun savePet(pet: Pet) {
        this.pet = pet
    }
}

