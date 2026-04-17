package es.itram

import es.itram.data.repository.InMemoryPetRepository
import es.itram.domain.model.FoodType
import es.itram.domain.model.HappinessState
import es.itram.domain.model.HungerState
import es.itram.domain.model.PetSpecies
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.CleanPetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetHappinessStateUseCase
import es.itram.domain.usecase.GetHungerStateUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.PlayWithPetUseCase
import es.itram.domain.usecase.SleepPetUseCase
import es.itram.domain.usecase.TickStatsUseCase
import es.itram.presentation.PetViewModel
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

    @Test
    fun hungerState_mapsThresholdsCorrectly() {
        val getHungerState = GetHungerStateUseCase()

        assertEquals(HungerState.NORMAL, getHungerState(59))
        assertEquals(HungerState.ALERT, getHungerState(60))
        assertEquals(HungerState.ALERT, getHungerState(79))
        assertEquals(HungerState.CRITICAL, getHungerState(80))
    }

    @Test
    fun happinessState_mapsThresholdsCorrectly() {
        val getHappinessState = GetHappinessStateUseCase()

        assertEquals(HappinessState.CRITICAL, getHappinessState(39))
        assertEquals(HappinessState.ALERT, getHappinessState(40))
        assertEquals(HappinessState.ALERT, getHappinessState(59))
        assertEquals(HappinessState.NORMAL, getHappinessState(60))
    }

    @Test
    fun tick_reducesHealth_afterConsecutiveCriticalHunger() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(
            petRepository = repository,
            hungerDelta = 30,
            criticalStreakForHealthPenalty = 2,
            healthPenalty = 10,
        )

        createPet(name = "Luna", species = PetSpecies.CAT)

        tickStats() // 50 -> normal
        tickStats() // 80 -> critical streak 1
        tickStats() // 100 -> critical streak 2 -> health penalty

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(100, pet.stats.hunger)
        assertEquals(80, pet.stats.health)
        assertEquals(65, pet.stats.happiness)
        assertEquals(2, pet.criticalHungerStreak)
    }

    @Test
    fun feed_resetsCriticalHungerStreak_whenLeavingCritical() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(
            petRepository = repository,
            hungerDelta = 60,
            criticalStreakForHealthPenalty = 2,
            healthPenalty = 10,
        )
        val feedPet = FeedPetUseCase(repository)

        createPet(name = "Bolt", species = PetSpecies.DOG)

        tickStats() // 80 -> critical streak 1
        feedPet(FoodType.FEAST) // 60 -> alert -> streak reset
        tickStats() // 100 -> critical streak 1 (no health penalty)

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(90, pet.stats.health)
        assertEquals(1, pet.criticalHungerStreak)
    }

    @Test
    fun tick_floorsHappinessAt0_underLongCriticalStreak() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(
            petRepository = repository,
            hungerDelta = 100,
            criticalStreakForHealthPenalty = 1,
            healthPenalty = 0,
            happinessPenalty = 30,
        )

        createPet(name = "Nox", species = PetSpecies.DRAGON)

        repeat(4) { tickStats() }

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(0, pet.stats.happiness)
    }

    @Test
    fun play_increasesHappiness_andReducesEnergy() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val playWithPet = PlayWithPetUseCase(repository)

        createPet(name = "Kira", species = PetSpecies.CAT)
        playWithPet()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(80, pet.stats.happiness)
        assertEquals(60, pet.stats.energy)
    }

    @Test
    fun clean_increasesHygiene_andCapsAt100() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val cleanPet = CleanPetUseCase(repository, hygieneBoost = 50, happinessBoost = 0)

        createPet(name = "Toby", species = PetSpecies.DOG)
        cleanPet()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(100, pet.stats.hygiene)
    }

    @Test
    fun sleep_increasesEnergy_andHunger() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val sleepPet = SleepPetUseCase(repository)

        createPet(name = "Mora", species = PetSpecies.CAT)
        sleepPet()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(95, pet.stats.energy)
        assertEquals(25, pet.stats.hunger)
    }

    @Test
    fun sleep_capsEnergyAt100() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val sleepPet = SleepPetUseCase(repository, energyBoost = 50, hungerIncrease = 0)

        createPet(name = "Lio", species = PetSpecies.DOG)
        sleepPet()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(100, pet.stats.energy)
    }

    @Test
    fun tick_reducesEnergyAndHygiene_andFloorsAt0() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(
            petRepository = repository,
            hungerDelta = 0,
            energyDelta = -40,
            hygieneDelta = -50,
        )

        createPet(name = "Duna", species = PetSpecies.CAT)

        tickStats()
        tickStats()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(0, pet.stats.energy)
        assertEquals(0, pet.stats.hygiene)
    }

    @Test
    fun tick_recoversHealth_whenPetIsWellCared() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(
            petRepository = repository,
            hungerDelta = 0,
            energyDelta = 0,
            hygieneDelta = 0,
            healthRecovery = 4,
        )

        createPet(name = "Nala", species = PetSpecies.CAT)
        tickStats()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(94, pet.stats.health)
    }

    @Test
    fun tick_doesNotRecoverHealth_whenHungerIsHigh() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(
            petRepository = repository,
            hungerDelta = 30,
            energyDelta = 0,
            hygieneDelta = 0,
            healthRecovery = 4,
        )

        createPet(name = "Rolo", species = PetSpecies.DOG)
        tickStats()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(90, pet.stats.health)
    }

    @Test
    fun tick_recovery_capsHealthAt100() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(
            petRepository = repository,
            hungerDelta = 0,
            energyDelta = 0,
            hygieneDelta = 0,
            healthRecovery = 6,
        )

        createPet(name = "Iris", species = PetSpecies.DRAGON)
        tickStats()
        tickStats()

        val pet = repository.getPet()
        assertNotNull(pet)
        assertEquals(100, pet.stats.health)
    }

    @Test
    fun viewModel_tick_showsHealthRecoveryMessage_whenHealthIncreases() {
        val repository = InMemoryPetRepository()
        val viewModel = PetViewModel(
            createPetUseCase = CreatePetUseCase(repository),
            getPetStatusUseCase = GetPetStatusUseCase(repository),
            tickStatsUseCase = TickStatsUseCase(
                petRepository = repository,
                hungerDelta = 0,
                energyDelta = 0,
                hygieneDelta = 0,
                healthRecovery = 3,
            ),
            feedPetUseCase = FeedPetUseCase(repository),
            playWithPetUseCase = PlayWithPetUseCase(repository),
            cleanPetUseCase = CleanPetUseCase(repository),
            sleepPetUseCase = SleepPetUseCase(repository),
            getHungerStateUseCase = GetHungerStateUseCase(),
            getHappinessStateUseCase = GetHappinessStateUseCase(),
        )

        viewModel.createPet(name = "Mika", species = PetSpecies.CAT)
        viewModel.tick()

        assertEquals("Recupera +3 salud", viewModel.uiState.healthRecoveryMessage)
    }
}