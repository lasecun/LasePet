package es.itram.data.repository

import app.cash.sqldelight.db.SqlDriver
import es.itram.db.LasePetDatabase
import es.itram.domain.model.Pet
import es.itram.domain.model.PetSpecies
import es.itram.domain.model.Stats
import es.itram.domain.repository.PetRepository

class SqlDelightPetRepository(
    driver: SqlDriver,
) : PetRepository {
    private val database = LasePetDatabase(driver)
    private val queries = database.petStateQueries

    override fun getPet(): Pet? {
        return queries.selectState(
            mapper = { _, petId, name, species, hunger, happiness, energy, hygiene, health, createdAt, streak ->
                Pet(
                    id = petId,
                    name = name,
                    species = PetSpecies.valueOf(species),
                    stats = Stats(
                        hunger = hunger.toInt(),
                        happiness = happiness.toInt(),
                        energy = energy.toInt(),
                        hygiene = hygiene.toInt(),
                        health = health.toInt(),
                    ),
                    createdAtEpochMillis = createdAt,
                    criticalHungerStreak = streak.toInt(),
                )
            },
        ).executeAsOneOrNull()
    }

    override fun savePet(pet: Pet) {
        queries.upsertState(
            pet_id = pet.id,
            name = pet.name,
            species = pet.species.name,
            hunger = pet.stats.hunger.toLong(),
            happiness = pet.stats.happiness.toLong(),
            energy = pet.stats.energy.toLong(),
            hygiene = pet.stats.hygiene.toLong(),
            health = pet.stats.health.toLong(),
            created_at_epoch_millis = pet.createdAtEpochMillis,
            critical_hunger_streak = pet.criticalHungerStreak.toLong(),
        )
    }
}

