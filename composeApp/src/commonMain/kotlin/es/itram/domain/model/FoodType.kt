package es.itram.domain.model

enum class FoodType(
    val displayName: String,
    val hungerReduction: Int,
) {
    SNACK("Snack", 5),
    MEAL("Comida", 10),
    FEAST("Banquete", 20),
}

