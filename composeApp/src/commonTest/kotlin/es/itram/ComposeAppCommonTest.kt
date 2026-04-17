package es.itram

import es.itram.data.repository.InMemoryPetRepository
import es.itram.domain.model.FoodType
import es.itram.domain.model.PetSpecies
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.TickStatsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ComposeAppCommonTest {

    @Test
    fun createPet_setsInitialStats() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val getPetStatus = GetPetStatusUseCase(repository)

        createPet(name = "Nina", species = PetSpecies.CAT)
        val pet = getPetStatus()

        assertNotNull(pet)
        assertEquals("Nina", pet.name)
        assertEquals(PetSpecies.CAT, pet.species)
        assertEquals(20, pet.stats.hunger)
    }

    @Test
    fun tick_increasesHunger_andCapsAt100() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(repository, hungerDelta = 50)

        createPet(name = "Rex", species = PetSpecies.DOG)

        tickStats()
        tickStats()
        tickStats()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(100, pet.stats.hunger)
    }

    @Test
    fun feed_usesDefaultMealReduction() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val feedPet = FeedPetUseCase(repository)

        createPet(name = "Puff", species = PetSpecies.DRAGON)
        feedPet()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(10, pet.stats.hunger)
    }

    @Test
    fun feed_withFeast_reducesMoreAndFloorsAt0() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val feedPet = FeedPetUseCase(repository)

        createPet(name = "Milo", species = PetSpecies.CAT)

        feedPet(FoodType.FEAST)
        feedPet(FoodType.FEAST)

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(0, pet.stats.hunger)
    }
}