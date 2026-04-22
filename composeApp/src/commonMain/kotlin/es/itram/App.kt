package es.itram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import es.itram.domain.model.PetSpecies
import es.itram.domain.model.RewardEvent
import es.itram.presentation.AppContainer
import es.itram.presentation.PetUiState

@Composable
@Preview
fun App() {
    val viewModel = remember { AppContainer.createPetViewModel() }
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

                GamificationPanel(uiState)

                Text(
                    text = "Hambre: ${uiState.hunger}/100",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

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

                RecentRewardsSection(uiState.recentRewards)
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

@Composable
private fun GamificationPanel(uiState: PetUiState) {
    val xpFraction = if (uiState.xpForNextLevel > 0) {
        uiState.xpProgress.toFloat() / uiState.xpForNextLevel.toFloat()
    } else {
        1f
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Nivel ${uiState.level}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "🪙 ${uiState.coins}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "🔥 ${uiState.dailyStreak} ${if (uiState.dailyStreak == 1) "día" else "días"}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LinearProgressIndicator(
            progress = { xpFraction },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${uiState.xpProgress} / ${uiState.xpForNextLevel} XP",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End),
        )
        if (uiState.unlockedAchievements.isNotEmpty()) {
            Text(
                text = "Logros: ${uiState.unlockedAchievements.size}/${es.itram.domain.model.Achievement.entries.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun RecentRewardsSection(rewards: List<RewardEvent>) {
    if (rewards.isEmpty()) return

    val levelUps = rewards.filterIsInstance<RewardEvent.LevelUp>()
    val achievementsUnlocked = rewards.filterIsInstance<RewardEvent.AchievementUnlocked>()
    val totalXpGained = rewards.filterIsInstance<RewardEvent.XpGained>().sumOf { it.amount }
    val totalCoinsGained = rewards.filterIsInstance<RewardEvent.CoinsGained>().sumOf { it.amount }
    val streakUpdated = rewards.filterIsInstance<RewardEvent.StreakUpdated>().firstOrNull()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(2.dp))
        levelUps.forEach { levelUp ->
            Text(
                text = "🎉 ¡Subiste al nivel ${levelUp.newLevel}!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        achievementsUnlocked.forEach { event ->
            Text(
                text = "🏆 ¡Logro desbloqueado: ${event.achievement.displayName}!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        if (streakUpdated != null && streakUpdated.isNewDay) {
            Text(
                text = "📅 Racha actualizada: ${streakUpdated.days} ${if (streakUpdated.days == 1) "día" else "días"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        val rewardParts = buildList {
            if (totalXpGained > 0) add("+$totalXpGained XP")
            if (totalCoinsGained > 0) add("+$totalCoinsGained 🪙")
        }
        if (rewardParts.isNotEmpty()) {
            Text(
                text = rewardParts.joinToString("  "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

