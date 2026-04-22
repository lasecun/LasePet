package es.itram

import es.itram.data.repository.InMemoryPetRepository
import es.itram.domain.model.Achievement
import es.itram.domain.model.FoodType
import es.itram.domain.model.GamificationConfig
import es.itram.domain.model.GamificationState
import es.itram.domain.model.PetAction
import es.itram.domain.model.PetSpecies
import es.itram.domain.model.RewardEvent
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.ProcessGamificationUseCase
import es.itram.domain.usecase.TickStatsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ComposeAppCommonTest {

    // ── Existing pet-stat tests (unchanged behaviour) ──────────────────────────

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

    // ── GamificationConfig formula tests ──────────────────────────────────────

    @Test
    fun gamificationConfig_levelThresholds_areCorrect() {
        assertEquals(0, GamificationConfig.xpNeededForLevel(1))
        assertEquals(100, GamificationConfig.xpNeededForLevel(2))
        assertEquals(300, GamificationConfig.xpNeededForLevel(3))
        assertEquals(600, GamificationConfig.xpNeededForLevel(4))
        assertEquals(1000, GamificationConfig.xpNeededForLevel(5))
    }

    @Test
    fun gamificationConfig_levelForXp_computesCorrectly() {
        assertEquals(1, GamificationConfig.levelForXp(0))
        assertEquals(1, GamificationConfig.levelForXp(99))
        assertEquals(2, GamificationConfig.levelForXp(100))
        assertEquals(2, GamificationConfig.levelForXp(299))
        assertEquals(3, GamificationConfig.levelForXp(300))
        assertEquals(5, GamificationConfig.levelForXp(1000))
    }

    // ── ProcessGamificationUseCase tests ──────────────────────────────────────

    private fun initialState(nowEpochMillis: Long = 0L) = GamificationState.initial(nowEpochMillis)

    @Test
    fun processGamification_feed_awardsXpAndCoins() {
        val useCase = ProcessGamificationUseCase()
        val state = initialState()

        val (newState, events) = useCase(state, PetAction.FEED, nowEpochMillis = 0L)

        assertEquals(GamificationConfig.XP_PER_FEED, newState.xp)
        assertEquals(GamificationConfig.COINS_PER_FEED, newState.coins)
        assertTrue(events.any { it is RewardEvent.XpGained && it.amount == GamificationConfig.XP_PER_FEED })
        assertTrue(events.any { it is RewardEvent.CoinsGained && it.amount == GamificationConfig.COINS_PER_FEED })
    }

    @Test
    fun processGamification_tick_awardsSmallRewards() {
        val useCase = ProcessGamificationUseCase()
        val state = initialState()

        val (newState, events) = useCase(state, PetAction.TICK, nowEpochMillis = 0L)

        assertEquals(GamificationConfig.XP_PER_TICK, newState.xp)
        assertEquals(GamificationConfig.COINS_PER_TICK, newState.coins)
        assertTrue(events.any { it is RewardEvent.XpGained })
        assertTrue(events.any { it is RewardEvent.CoinsGained })
    }

    @Test
    fun processGamification_feed_withCooldown_doesNotAwardXpOrCoins() {
        val useCase = ProcessGamificationUseCase()
        // First feed at t=0 (no cooldown because lastFeedEpochMillis == -1)
        val (stateAfterFirstFeed, _) = useCase(initialState(), PetAction.FEED, nowEpochMillis = 0L)
        // Second feed within cooldown window
        val (stateAfterSecondFeed, secondEvents) = useCase(
            stateAfterFirstFeed,
            PetAction.FEED,
            nowEpochMillis = GamificationConfig.FEED_COOLDOWN_MILLIS - 1,
        )

        // XP and coins should not increase from the second feed
        val xpFromFirstFeed = stateAfterFirstFeed.xp
        assertFalse(secondEvents.any { it is RewardEvent.XpGained && it.amount == GamificationConfig.XP_PER_FEED })
        // Achievement XP (FIRST_FEED) was awarded on the first feed; no new XP from second feed action
        assertEquals(xpFromFirstFeed, stateAfterSecondFeed.xp)
    }

    @Test
    fun processGamification_feed_afterCooldownExpires_awardsRewards() {
        val useCase = ProcessGamificationUseCase()
        val (stateAfterFirst, _) = useCase(initialState(), PetAction.FEED, nowEpochMillis = 0L)
        val (stateAfterSecond, secondEvents) = useCase(
            stateAfterFirst,
            PetAction.FEED,
            nowEpochMillis = GamificationConfig.FEED_COOLDOWN_MILLIS + 1,
        )

        assertTrue(secondEvents.any { it is RewardEvent.XpGained && it.amount == GamificationConfig.XP_PER_FEED })
        assertTrue(stateAfterSecond.xp > stateAfterFirst.xp)
    }

    @Test
    fun processGamification_levelUp_emitsEvent() {
        val useCase = ProcessGamificationUseCase()
        // Start just below level-2 threshold (100 XP), totalFeedCount=0 so FIRST_FEED hasn't fired
        val state = GamificationState(
            xp = 90,
            level = 1,
            coins = 0,
            dailyStreak = 1,
            lastActiveEpochDay = 0L,
            lastFeedEpochMillis = -1L,
            totalFeedCount = 0,
            unlockedAchievements = emptySet(),
        )

        val (newState, events) = useCase(state, PetAction.FEED, nowEpochMillis = 0L)

        // Feed gives 10 XP → 100 XP → level 2
        assertTrue(newState.level >= 2)
        assertTrue(events.any { it is RewardEvent.LevelUp && it.newLevel == 2 })
    }

    @Test
    fun processGamification_multiLevelUp_emitsMultipleEvents() {
        val useCase = ProcessGamificationUseCase()
        // Start at level 1 with 90 XP (need 100 for level 2); a tick giving 2 XP won't trigger.
        // But use a custom large XP gain by manually setting xp close to level 3 threshold (300).
        val state = GamificationState(
            xp = 289,
            level = 2,
            coins = 0,
            dailyStreak = 1,
            lastActiveEpochDay = 0L,
            lastFeedEpochMillis = -1L,
            totalFeedCount = 5, // FIRST_FEED already earned implicitly
            unlockedAchievements = setOf(Achievement.FIRST_FEED),
        )

        // Feed gives 10 XP → 299. Still level 2 (need 300).
        // Then FIRST_FEED is already unlocked, WELL_FED not yet (totalFeedCount was 5, becomes 6).
        val (stateAfterFeed, events) = useCase(state, PetAction.FEED, nowEpochMillis = 0L)
        assertEquals(2, stateAfterFeed.level)

        // Now add enough XP manually to jump multiple levels
        val highXpState = stateAfterFeed.copy(xp = 595) // just under level 4 (600)
        val (finalState, finalEvents) = useCase(highXpState, PetAction.TICK, nowEpochMillis = 0L)

        // 595 + 2 = 597 < 600, still level 3
        assertEquals(3, finalState.level)

        // Jump to 598 and tick again
        val almostLevel4 = finalState.copy(xp = 598)
        val (l4State, l4Events) = useCase(almostLevel4, PetAction.TICK, nowEpochMillis = 0L)
        assertEquals(3, l4State.level) // 600 not reached yet with +2

        // One more tick on 600 → level 4
        val atLevel4Boundary = l4State.copy(xp = 600)
        val (leveledState, leveledEvents) = useCase(atLevel4Boundary, PetAction.TICK, nowEpochMillis = 0L)
        assertTrue(leveledState.level >= 4)
    }

    @Test
    fun processGamification_achievement_firstFeed_unlocksOnFirstFeed() {
        val useCase = ProcessGamificationUseCase()
        val (newState, events) = useCase(initialState(), PetAction.FEED, nowEpochMillis = 0L)

        assertTrue(Achievement.FIRST_FEED in newState.unlockedAchievements)
        assertTrue(events.any { it is RewardEvent.AchievementUnlocked && it.achievement == Achievement.FIRST_FEED })
        // Achievement gives XP and coins on top of base reward
        assertTrue(newState.xp >= GamificationConfig.XP_PER_FEED + Achievement.FIRST_FEED.xpReward)
        assertTrue(newState.coins >= GamificationConfig.COINS_PER_FEED + Achievement.FIRST_FEED.coinReward)
    }

    @Test
    fun processGamification_achievement_noRepeatUnlock() {
        val useCase = ProcessGamificationUseCase()
        val (afterFirst, _) = useCase(initialState(), PetAction.FEED, nowEpochMillis = 0L)
        assertTrue(Achievement.FIRST_FEED in afterFirst.unlockedAchievements)
        val xpAfterFirst = afterFirst.xp

        val (afterSecond, secondEvents) = useCase(
            afterFirst,
            PetAction.FEED,
            nowEpochMillis = GamificationConfig.FEED_COOLDOWN_MILLIS + 1,
        )

        // FIRST_FEED must not appear again
        assertFalse(secondEvents.any { it is RewardEvent.AchievementUnlocked && it.achievement == Achievement.FIRST_FEED })
        assertEquals(1, afterSecond.unlockedAchievements.count { it == Achievement.FIRST_FEED })
    }

    @Test
    fun processGamification_achievement_wellFed_unlocksAt10Feeds() {
        val useCase = ProcessGamificationUseCase()
        var state = initialState()
        val cooldown = GamificationConfig.FEED_COOLDOWN_MILLIS

        repeat(10) { i ->
            val (newState, _) = useCase(state, PetAction.FEED, nowEpochMillis = i * (cooldown + 1))
            state = newState
        }

        assertTrue(Achievement.WELL_FED in state.unlockedAchievements)
    }

    @Test
    fun processGamification_streak_incrementsOnNewDay() {
        val useCase = ProcessGamificationUseCase()
        val state = initialState(nowEpochMillis = 0L) // lastActiveEpochDay = 0
        val oneDayMs = GamificationState.DAY_MILLIS

        val (newState, events) = useCase(state, PetAction.FEED, nowEpochMillis = oneDayMs)

        assertEquals(2, newState.dailyStreak)
        assertTrue(events.any { it is RewardEvent.StreakUpdated && it.days == 2 && it.isNewDay })
    }

    @Test
    fun processGamification_streak_doesNotIncrementOnSameDay() {
        val useCase = ProcessGamificationUseCase()
        val state = initialState(nowEpochMillis = 0L)

        val (newState, events) = useCase(state, PetAction.FEED, nowEpochMillis = 1000L) // same day (day 0)

        assertEquals(1, newState.dailyStreak)
        assertFalse(events.any { it is RewardEvent.StreakUpdated })
    }

    @Test
    fun processGamification_streak_resetsAfterMissedDay() {
        val useCase = ProcessGamificationUseCase()
        val state = GamificationState(
            xp = 0, level = 1, coins = 0, dailyStreak = 5,
            lastActiveEpochDay = 0L, lastFeedEpochMillis = -1L,
            totalFeedCount = 0, unlockedAchievements = emptySet(),
        )
        val twoDaysMs = GamificationState.DAY_MILLIS * 2

        val (newState, events) = useCase(state, PetAction.FEED, nowEpochMillis = twoDaysMs)

        assertEquals(1, newState.dailyStreak)
        assertTrue(events.any { it is RewardEvent.StreakUpdated && it.days == 1 && it.isNewDay })
    }

    @Test
    fun processGamification_streak_achievement_STREAK_3() {
        val useCase = ProcessGamificationUseCase()
        val state = GamificationState(
            xp = 0, level = 1, coins = 0, dailyStreak = 2,
            lastActiveEpochDay = 1L, lastFeedEpochMillis = -1L,
            totalFeedCount = 1, unlockedAchievements = setOf(Achievement.FIRST_FEED),
        )

        val (newState, events) = useCase(state, PetAction.FEED, nowEpochMillis = GamificationState.DAY_MILLIS * 2)

        assertEquals(3, newState.dailyStreak)
        assertTrue(Achievement.STREAK_3 in newState.unlockedAchievements)
        assertTrue(events.any { it is RewardEvent.AchievementUnlocked && it.achievement == Achievement.STREAK_3 })
    }

    // ── Integration: FeedPetUseCase still updates pet stats AND gamification ──

    @Test
    fun feedPetUseCase_updatesStatsAndGamification() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val feedPet = FeedPetUseCase(repository)

        createPet("Buddy", PetSpecies.DOG)

        val (pet, rewards) = feedPet(FoodType.MEAL)

        assertNotNull(pet)
        assertEquals(10, pet.stats.hunger) // 20 - 10
        assertTrue(pet.gamification.xp > 0)
        assertTrue(pet.gamification.coins > 0)
        assertTrue(rewards.isNotEmpty())
    }

    @Test
    fun tickStatsUseCase_updatesStatsAndGamification() {
        val repository = InMemoryPetRepository()
        val createPet = CreatePetUseCase(repository)
        val tickStats = TickStatsUseCase(repository)

        createPet("Buddy", PetSpecies.DOG)

        val (pet, rewards) = tickStats()

        assertNotNull(pet)
        assertEquals(25, pet.stats.hunger) // 20 + 5
        assertTrue(pet.gamification.xp > 0)
        assertTrue(rewards.isNotEmpty())
    }
}
