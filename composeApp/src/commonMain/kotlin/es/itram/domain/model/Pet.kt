package es.itram.domain.model

enum class PetSpecies(val displayName: String) {
    DOG("Perro"),
    CAT("Gato"),
    DRAGON("Dragon")
}

data class Stats(
    val hunger: Int,
    val happiness: Int,
    val energy: Int,
    val hygiene: Int,
    val health: Int,
) {
    init {
        require(hunger in 0..100)
        require(happiness in 0..100)
        require(energy in 0..100)
        require(hygiene in 0..100)
        require(health in 0..100)
    }

    fun withHungerDelta(delta: Int): Stats {
        val next = (hunger + delta).coerceIn(0, 100)
        return copy(hunger = next)
    }

    fun withHealthDelta(delta: Int): Stats {
        val next = (health + delta).coerceIn(0, 100)
        return copy(health = next)
    }

    fun withHappinessDelta(delta: Int): Stats {
        val next = (happiness + delta).coerceIn(0, 100)
        return copy(happiness = next)
    }

    fun withEnergyDelta(delta: Int): Stats {
        val next = (energy + delta).coerceIn(0, 100)
        return copy(energy = next)
    }

    fun withHygieneDelta(delta: Int): Stats {
        val next = (hygiene + delta).coerceIn(0, 100)
        return copy(hygiene = next)
    }
}

data class Pet(
    val id: String,
    val name: String,
    val species: PetSpecies,
    val stats: Stats,
    val createdAtEpochMillis: Long,
    val criticalHungerStreak: Int = 0,
    val lastTickEpochMillis: Long = 0,
)

