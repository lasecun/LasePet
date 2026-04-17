package es.itram.presentation

data class PetUiState(
    val hasPet: Boolean = false,
    val petName: String = "",
    val speciesName: String = "",
    val hunger: Int = 0,
    val errorMessage: String? = null,
)

