package es.itram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.itram.domain.model.EnergyState
import es.itram.domain.model.FoodType
import es.itram.domain.model.HealthState
import es.itram.domain.model.HappinessState
import es.itram.domain.model.HungerState
import es.itram.domain.model.HygieneState
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

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .safeContentPadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeaderCard(petName = if (uiState.hasPet) uiState.petName else null)

            if (!uiState.hasPet) {
                CreatePetCard(
                    petNameInput = petNameInput,
                    onNameChange = { petNameInput = it },
                    selectedSpecies = selectedSpecies,
                    onSpeciesSelected = { selectedSpeciesName = it.name },
                    onCreatePet = { viewModel.createPet(name = petNameInput, species = selectedSpecies) },
                )
            } else {
                StatsCard(uiState)
                StatesCard(uiState)
                FoodActionsCard(onFeed = viewModel::feed)
                CareActionsCard(
                    onPlay = viewModel::play,
                    onClean = viewModel::clean,
                    onSleep = viewModel::sleep,
                    onTick = viewModel::tick,
                )
                DiaryCard(events = uiState.recentEvents)
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(petName: String?) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PocketPal", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = petName?.let { "Cuidando de $it" } ?: "Tu mascota virtual",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(imageVector = Icons.Filled.Pets, contentDescription = null)
        }
    }
}

@Composable
private fun CreatePetCard(
    petNameInput: String,
    onNameChange: (String) -> Unit,
    selectedSpecies: PetSpecies,
    onSpeciesSelected: (PetSpecies) -> Unit,
    onCreatePet: () -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Crea tu primera mascota", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = petNameInput,
                onValueChange = onNameChange,
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
                        Button(onClick = { onSpeciesSelected(species) }, modifier = buttonModifier) {
                            Text(species.displayName)
                        }
                    } else {
                        OutlinedButton(onClick = { onSpeciesSelected(species) }, modifier = buttonModifier) {
                            Text(species.displayName)
                        }
                    }
                }
            }
            Button(onClick = onCreatePet, modifier = Modifier.fillMaxWidth()) {
                Text("Crear mascota")
            }
        }
    }
}

@Composable
private fun StatsCard(uiState: es.itram.presentation.PetUiState) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Mascota: ${uiState.petName} (${uiState.speciesName})",
                style = MaterialTheme.typography.titleMedium,
            )
            Text("Hambre: ${uiState.hunger}/100")
            Text("Felicidad: ${uiState.happiness}/100")
            Text("Energia: ${uiState.energy}/100")
            Text("Higiene: ${uiState.hygiene}/100")
            Text("Salud: ${uiState.health}/100", fontWeight = FontWeight.SemiBold)
            if (uiState.healthRecoveryMessage != null) {
                Text(
                    text = uiState.healthRecoveryMessage,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StatesCard(uiState: es.itram.presentation.PetUiState) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StateRow("Hambre", uiState.hungerState?.displayName, colorFor(uiState.hungerState))
            StateRow("Felicidad", uiState.happinessState?.displayName, colorFor(uiState.happinessState))
            StateRow("Energia", uiState.energyState?.displayName, colorFor(uiState.energyState))
            StateRow("Higiene", uiState.hygieneState?.displayName, colorFor(uiState.hygieneState))
            StateRow("Salud", uiState.healthState?.displayName, colorFor(uiState.healthState))
        }
    }
}

@Composable
private fun StateRow(label: String, value: String?, color: Color) {
    if (value == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Text(value, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FoodActionsCard(onFeed: (FoodType) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle(icon = Icons.Filled.Restaurant, title = "Comida")
            FoodType.entries.forEach { foodType ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(foodType.displayName, style = MaterialTheme.typography.titleSmall)
                        Text("Reduce hambre en ${foodType.hungerReduction}", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(onClick = { onFeed(foodType) }) {
                        Icon(
                            imageVector = foodIcon(foodType),
                            contentDescription = "Usar ${foodType.displayName}",
                        )
                    }
                }
            }
        }
    }
}

private fun foodIcon(foodType: FoodType): ImageVector {
    return when (foodType) {
        FoodType.SNACK -> Icons.Filled.Fastfood
        FoodType.MEAL -> Icons.Filled.Restaurant
        FoodType.FEAST -> Icons.Filled.DinnerDining
    }
}

@Composable
private fun CareActionsCard(
    onPlay: () -> Unit,
    onClean: () -> Unit,
    onSleep: () -> Unit,
    onTick: () -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle(icon = Icons.Filled.Schedule, title = "Acciones")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconActionButton(Icons.Filled.SportsEsports, "Jugar", onPlay, Modifier.weight(1f))
                IconActionButton(Icons.Filled.CleaningServices, "Limpiar", onClean, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconActionButton(Icons.Filled.Hotel, "Dormir", onSleep, Modifier.weight(1f))
                IconActionButton(Icons.Filled.Schedule, "Tick", onTick, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IconActionButton(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text)
        }
    }
}

@Composable
private fun DiaryCard(events: List<String>) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionTitle(icon = Icons.AutoMirrored.Filled.List, title = "Diario reciente")
            if (events.isEmpty()) {
                Text("Sin eventos todavia")
            } else {
                events.forEach { event ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("  $event")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null)
        Text("  $title", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun colorFor(state: Any?): Color {
    return when (state) {
        HungerState.CRITICAL, HappinessState.CRITICAL, EnergyState.CRITICAL, HygieneState.CRITICAL, HealthState.CRITICAL -> MaterialTheme.colorScheme.error
        HungerState.ALERT, HappinessState.ALERT, EnergyState.ALERT, HygieneState.ALERT, HealthState.ALERT -> MaterialTheme.colorScheme.tertiary
        HungerState.NORMAL, HappinessState.NORMAL, EnergyState.NORMAL, HygieneState.NORMAL, HealthState.NORMAL -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
}