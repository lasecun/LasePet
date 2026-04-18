package es.itram.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import es.itram.domain.model.FoodType
import es.itram.domain.model.HungerState
import es.itram.domain.model.EnergyState
import es.itram.domain.model.HygieneState
import es.itram.domain.model.PetEvent
import es.itram.domain.model.PetEventType
import es.itram.domain.model.PetSpecies
import es.itram.domain.usecase.CreatePetUseCase
import es.itram.domain.usecase.CleanPetUseCase
import es.itram.domain.usecase.FeedPetUseCase
import es.itram.domain.usecase.GetEnergyStateUseCase
import es.itram.domain.usecase.GetHealthStateUseCase
import es.itram.domain.usecase.GetHappinessStateUseCase
import es.itram.domain.usecase.GetHungerStateUseCase
import es.itram.domain.usecase.GetHygieneStateUseCase
import es.itram.domain.usecase.GetPetStatusUseCase
import es.itram.domain.usecase.GetRecentPetEventsUseCase
import es.itram.domain.usecase.PlayWithPetUseCase
import es.itram.domain.usecase.RecordPetEventUseCase
import es.itram.domain.usecase.SleepPetUseCase
import es.itram.domain.usecase.TickStatsUseCase
import es.itram.notification.NotificationService
import es.itram.util.AppConfig
import es.itram.util.currentEpochMillis
import es.itram.util.isDebugBuild

class PetViewModel(
    private val createPetUseCase: CreatePetUseCase,
    private val getPetStatusUseCase: GetPetStatusUseCase,
    private val tickStatsUseCase: TickStatsUseCase,
    private val feedPetUseCase: FeedPetUseCase,
    private val playWithPetUseCase: PlayWithPetUseCase,
    private val cleanPetUseCase: CleanPetUseCase,
    private val sleepPetUseCase: SleepPetUseCase,
    private val recordPetEventUseCase: RecordPetEventUseCase,
    private val getRecentPetEventsUseCase: GetRecentPetEventsUseCase,
    private val getHungerStateUseCase: GetHungerStateUseCase,
    private val getHappinessStateUseCase: GetHappinessStateUseCase,
    private val getEnergyStateUseCase: GetEnergyStateUseCase,
    private val getHygieneStateUseCase: GetHygieneStateUseCase,
    private val getHealthStateUseCase: GetHealthStateUseCase,
    private val notificationService: NotificationService? = null,
) {
    var uiState by mutableStateOf(PetUiState())
        private set

    val tickIntervalMs: Long
        get() = if (isDebugBuild) AppConfig.TICK_INTERVAL_DEBUG_MS else AppConfig.TICK_INTERVAL_PROD_MS

    init {
        refreshUiState(errorMessage = null)
    }

    /** Calcula cuántos ticks se han perdido desde la última vez y los aplica al abrir la app. */
    fun catchUpTicks() {
        val pet = getPetStatusUseCase() ?: return
        val now = currentEpochMillis()
        val lastTick = pet.lastTickEpochMillis.takeIf { it > 0 } ?: now
        val elapsed = now - lastTick
        val missedTicks = (elapsed / AppConfig.TICK_INTERVAL_PROD_MS)
            .toInt()
            .coerceIn(0, AppConfig.MAX_CATCHUP_TICKS)
        if (missedTicks > 0) {
            repeat(missedTicks) { tickInternal(sendNotifications = false) }
            refreshUiState(errorMessage = null, healthRecoveryMessage = null)
        }
    }

    fun createPet(name: String, species: PetSpecies) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            refreshUiState(errorMessage = "Escribe un nombre para tu mascota")
            return
        }
        val pet = createPetUseCase(cleanName, species)
        recordPetEventUseCase(PetEvent(petId = pet.id, type = PetEventType.CREATED))
        refreshUiState(errorMessage = null)
    }

    fun tick() {
        val previousHealth = getPetStatusUseCase()?.stats?.health
        val pet = tickInternal(sendNotifications = true)
        val currentHealth = getPetStatusUseCase()?.stats?.health
        val recoveredAmount = if (previousHealth != null && currentHealth != null) {
            (currentHealth - previousHealth).coerceAtLeast(0)
        } else {
            0
        }
        val recoveryMessage = if (recoveredAmount > 0) "Recupera +$recoveredAmount salud" else null
        refreshUiState(errorMessage = null, healthRecoveryMessage = recoveryMessage)
    }

    private fun tickInternal(sendNotifications: Boolean): es.itram.domain.model.Pet? {
        val prevStats = getPetStatusUseCase()?.stats
        val pet = tickStatsUseCase(nowEpochMillis = currentEpochMillis())
        if (pet != null) {
            recordPetEventUseCase(PetEvent(petId = pet.id, type = PetEventType.TICK))
        }
        if (sendNotifications && pet != null && prevStats != null) {
            checkAndNotify(pet, prevStats)
        }
        return pet
    }

    private fun checkAndNotify(
        pet: es.itram.domain.model.Pet,
        prevStats: es.itram.domain.model.Stats,
    ) {
        val newHungerState = getHungerStateUseCase(pet.stats.hunger)
        val prevHungerState = getHungerStateUseCase(prevStats.hunger)
        if (newHungerState == HungerState.CRITICAL && prevHungerState != HungerState.CRITICAL) {
            notificationService?.sendHungerAlert(pet.name)
        }
        val newEnergyState = getEnergyStateUseCase(pet.stats.energy)
        val prevEnergyState = getEnergyStateUseCase(prevStats.energy)
        if (newEnergyState == EnergyState.CRITICAL && prevEnergyState != EnergyState.CRITICAL) {
            notificationService?.sendEnergyAlert(pet.name)
        }
        val newHygieneState = getHygieneStateUseCase(pet.stats.hygiene)
        val prevHygieneState = getHygieneStateUseCase(prevStats.hygiene)
        if (newHygieneState == HygieneState.CRITICAL && prevHygieneState != HygieneState.CRITICAL) {
            notificationService?.sendHygieneAlert(pet.name)
        }
    }

    fun feed(foodType: FoodType = FoodType.MEAL) {
        val pet = feedPetUseCase(foodType)
        if (pet != null) {
            recordPetEventUseCase(PetEvent(petId = pet.id, type = PetEventType.FEED))
        }
        refreshUiState(errorMessage = null, healthRecoveryMessage = null)
    }

    fun play() {
        val pet = playWithPetUseCase()
        if (pet != null) {
            recordPetEventUseCase(PetEvent(petId = pet.id, type = PetEventType.PLAY))
        }
        refreshUiState(errorMessage = null, healthRecoveryMessage = null)
    }

    fun clean() {
        val pet = cleanPetUseCase()
        if (pet != null) {
            recordPetEventUseCase(PetEvent(petId = pet.id, type = PetEventType.CLEAN))
        }
        refreshUiState(errorMessage = null, healthRecoveryMessage = null)
    }

    fun sleep() {
        val pet = sleepPetUseCase()
        if (pet != null) {
            recordPetEventUseCase(PetEvent(petId = pet.id, type = PetEventType.SLEEP))
        }
        refreshUiState(errorMessage = null, healthRecoveryMessage = null)
    }

    private fun refreshUiState(errorMessage: String?, healthRecoveryMessage: String? = null) {
        val pet = getPetStatusUseCase()
        uiState = if (pet == null) {
            PetUiState(errorMessage = errorMessage, healthRecoveryMessage = healthRecoveryMessage)
        } else {
            val recentEvents = getRecentPetEventsUseCase(pet.id)
                .map { it.type.label }
            PetUiState(
                hasPet = true,
                petName = pet.name,
                speciesName = pet.species.displayName,
                hunger = pet.stats.hunger,
                happiness = pet.stats.happiness,
                energy = pet.stats.energy,
                hygiene = pet.stats.hygiene,
                health = pet.stats.health,
                hungerState = getHungerStateUseCase(pet.stats.hunger),
                happinessState = getHappinessStateUseCase(pet.stats.happiness),
                energyState = getEnergyStateUseCase(pet.stats.energy),
                hygieneState = getHygieneStateUseCase(pet.stats.hygiene),
                healthState = getHealthStateUseCase(pet.stats.health),
                healthRecoveryMessage = healthRecoveryMessage,
                recentEvents = recentEvents,
                errorMessage = errorMessage,
            )
        }
    }
}
