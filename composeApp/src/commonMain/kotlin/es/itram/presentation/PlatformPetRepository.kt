package es.itram.presentation

import androidx.compose.runtime.Composable
import es.itram.domain.repository.PetRepository

@Composable
expect fun rememberPlatformPetRepository(): PetRepository

