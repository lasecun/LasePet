package es.itram.domain.model

enum class Achievement(
    val displayName: String,
    val description: String,
    val xpReward: Int,
    val coinReward: Int,
) {
    FIRST_FEED(
        displayName = "Primer bocado",
        description = "Alimenta a tu mascota por primera vez",
        xpReward = 50,
        coinReward = 20,
    ),
    WELL_FED(
        displayName = "Bien nutrido",
        description = "Alimenta a tu mascota 10 veces",
        xpReward = 100,
        coinReward = 50,
    ),
    REACH_LEVEL_5(
        displayName = "Nivel 5",
        description = "Alcanza el nivel 5 con tu mascota",
        xpReward = 200,
        coinReward = 100,
    ),
    STREAK_3(
        displayName = "Racha de 3 días",
        description = "Mantén una racha de cuidado de 3 días consecutivos",
        xpReward = 150,
        coinReward = 75,
    ),
    STREAK_7(
        displayName = "Racha semanal",
        description = "Mantén una racha de cuidado de 7 días consecutivos",
        xpReward = 300,
        coinReward = 150,
    ),
}
