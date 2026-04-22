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
}

data class Pet(
    val id: String,
    val name: String,
    val species: PetSpecies,
    val stats: Stats,
    val createdAtEpochMillis: Long,
    val gamification: GamificationState,
)

