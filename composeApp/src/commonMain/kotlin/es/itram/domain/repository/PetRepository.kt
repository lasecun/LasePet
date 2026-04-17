package es.itram.domain.repository

import es.itram.domain.model.Pet

interface PetRepository {
    fun getPet(): Pet?
    fun savePet(pet: Pet)
}

