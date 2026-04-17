package es.itram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.itram.domain.model.FoodType
import es.itram.domain.model.HungerState
import es.itram.domain.model.PetSpecies
import es.itram.presentation.AppContainer
import es.itram.presentation.rememberPlatformPetRepository

@Composable
@Preview
fun App() {
    val petRepository = rememberPlatformPetRepository()
    val viewModel = remember(petRepository) { AppContainer.createPetViewModel(petRepository) }
    val uiState = viewModel.uiState
    var petNameInput by rememberSaveable { mutableStateOf("") }
    var selectedSpeciesName by rememberSaveable { mutableStateOf(PetSpecies.DOG.name) }
    val selectedSpecies = PetSpecies.valueOf(selectedSpeciesName)
    var selectedFoodName by rememberSaveable { mutableStateOf(FoodType.MEAL.name) }
    val selectedFood = FoodType.valueOf(selectedFoodName)

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("PocketPal", style = MaterialTheme.typography.headlineMedium)

            if (!uiState.hasPet) {
                Text("Crea tu primera mascota", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = petNameInput,
                    onValueChange = { petNameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    singleLine = true,
                )

                Text("Especie")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PetSpecies.entries.forEach { species ->
                        val isSelected = species == selectedSpecies
                        val buttonModifier = Modifier.weight(1f)
                        if (isSelected) {
                            Button(
                                onClick = { selectedSpeciesName = species.name },
                                modifier = buttonModifier,
                            ) {
                                Text(species.displayName)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { selectedSpeciesName = species.name },
                                modifier = buttonModifier,
                            ) {
                                Text(species.displayName)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.createPet(name = petNameInput, species = selectedSpecies)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Crear mascota")
                }
            } else {
                Text(
                    text = "Mascota: ${uiState.petName} (${uiState.speciesName})",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Hambre: ${uiState.hunger}/100",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Felicidad: ${uiState.happiness}/100",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Salud: ${uiState.health}/100",
                    style = MaterialTheme.typography.titleMedium,
                )

                val hungerState = uiState.hungerState
                if (hungerState != null) {
                    val stateColor = when (hungerState) {
                        HungerState.NORMAL -> MaterialTheme.colorScheme.primary
                        HungerState.ALERT -> MaterialTheme.colorScheme.tertiary
                        HungerState.CRITICAL -> MaterialTheme.colorScheme.error
                    }
                    Text(
                        text = "Estado: ${hungerState.displayName}",
                        color = stateColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Text("Comida")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FoodType.entries.forEach { foodType ->
                        val isSelected = foodType == selectedFood
                        val buttonModifier = Modifier.weight(1f)
                        val label = "${foodType.displayName} (-${foodType.hungerReduction})"
                        if (isSelected) {
                            Button(
                                onClick = { selectedFoodName = foodType.name },
                                modifier = buttonModifier,
                            ) {
                                Text(label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { selectedFoodName = foodType.name },
                                modifier = buttonModifier,
                            ) {
                                Text(label)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.feed(selectedFood) }) {
                        Text("Dar de comer")
                    }
                    Button(onClick = { viewModel.tick() }) {
                        Text("Avanzar tiempo (+5 hambre)")
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}