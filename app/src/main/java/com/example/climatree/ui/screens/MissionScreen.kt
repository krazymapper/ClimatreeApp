package com.example.climatree.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.climatree.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionScreen(
    missionService: TreeMissionService,
    authService: AuthService,
    onMissionClick: (TreeMission) -> Unit,
    modifier: Modifier = Modifier
) {
    var missions by remember { mutableStateOf<List<TreeMission>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateMissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // Dans une vraie app, on récupérerait la position actuelle
            val location = GeoPoint(0.0, 0.0) // Placeholder
            missions = missionService.getActiveMissions(location).getOrThrow()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Missions de Cartographie") },
                actions = {
                    IconButton(onClick = { showCreateMissionDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Créer une mission")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Erreur: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(missions) { mission ->
                        MissionCard(
                            mission = mission,
                            onClick = { onMissionClick(mission) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateMissionDialog) {
        CreateMissionDialog(
            onDismiss = { showCreateMissionDialog = false },
            onCreate = { mission ->
                missionService.createMission(mission)
                showCreateMissionDialog = false
            }
        )
    }
}

@Composable
fun MissionCard(
    mission: TreeMission,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = mission.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mission.description)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progression
            LinearProgressIndicator(
                progress = mission.currentCount.toFloat() / mission.targetCount,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${mission.currentCount}/${mission.targetCount} arbres",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${mission.participants.size} participants",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Récompenses
            if (mission.rewards.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Récompenses:",
                    style = MaterialTheme.typography.titleMedium
                )
                mission.rewards.forEach { reward ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (reward.type) {
                                RewardType.BADGE -> Icons.Default.MilitaryTech
                                RewardType.POINTS -> Icons.Default.Star
                                RewardType.SPECIAL_TITLE -> Icons.Default.EmojiEvents
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = reward.description)
                    }
                }
            }
            
            // Échéance
            mission.deadline?.let { deadline ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Échéance: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(deadline)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CreateMissionDialog(
    onDismiss: () -> Unit,
    onCreate: (TreeMission) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetCount by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Date?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Créer une mission") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = targetCount,
                    onValueChange = { targetCount = it },
                    label = { Text("Nombre d'arbres cible") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Rayon de la zone (mètres)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        TreeMission(
                            title = title,
                            description = description,
                            area = GeoPoint(0.0, 0.0), // À remplacer par la position actuelle
                            radius = radius.toDoubleOrNull() ?: 0.0,
                            targetCount = targetCount.toIntOrNull() ?: 0,
                            deadline = deadline
                        )
                    )
                }
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
} 