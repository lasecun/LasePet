package es.itram.domain.model

enum class HealthState(val displayName: String) {
    NORMAL("Normal"),
    ALERT("Alerta"),
    CRITICAL("Critico"),
}

