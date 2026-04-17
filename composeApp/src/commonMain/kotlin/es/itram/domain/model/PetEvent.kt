package es.itram.domain.model

enum class PetEventType(val label: String) {
    CREATED("Mascota creada"),
    FEED("Ha comido"),
    PLAY("Ha jugado"),
    CLEAN("Se ha limpiado"),
    SLEEP("Ha dormido"),
    TICK("Paso del tiempo"),
}

data class PetEvent(
    val petId: String,
    val type: PetEventType,
)

