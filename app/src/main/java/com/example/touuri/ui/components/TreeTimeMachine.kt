package com.example.touuri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.touuri.data.*
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeTimeMachine(
    treeId: String,
    currentHeight: Double?,
    currentDiameter: Double?,
    currentHealth: Int,
    growthService: TreeGrowthService,
    dao: TreeHistoryDao
) {
    var history by remember { mutableStateOf<List<TreeHistory>>(emptyList()) }
    var prediction by remember { mutableStateOf<GrowthPrediction?>(null) }
    val scope = rememberCoroutineScope()
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    LaunchedEffect(treeId) {
        // Load history
        dao.getTreeHistory(treeId).collectLatest { newHistory ->
            history = newHistory
        }

        // Get prediction
        scope.launch {
            prediction = growthService.predictGrowth(treeId, currentHeight, currentDiameter)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Current State
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "État actuel",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoBox(
                            title = "Hauteur",
                            value = "${currentHeight ?: "N/A"} m",
                            icon = Icons.Default.Height
                        )
                        InfoBox(
                            title = "Diamètre",
                            value = "${currentDiameter ?: "N/A"} cm",
                            icon = Icons.Default.Straighten
                        )
                        InfoBox(
                            title = "Santé",
                            value = "$currentHealth%",
                            icon = Icons.Default.MedicalServices
                        )
                    }
                }
            }
        }

        // Growth Prediction
        prediction?.let { pred ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Prédiction dans 5 ans",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoBox(
                                title = "Hauteur future",
                                value = "${pred.heightIn5Years?.let { "%.1f".format(it) } ?: "N/A"} m",
                                icon = Icons.Default.TrendingUp
                            )
                            InfoBox(
                                title = "Diamètre futur",
                                value = "${pred.diameterIn5Years?.let { "%.1f".format(it) } ?: "N/A"} cm",
                                icon = Icons.Default.TrendingUp
                            )
                            InfoBox(
                                title = "Santé future",
                                value = "${pred.healthIn5Years}%",
                                icon = Icons.Default.TrendingUp,
                                color = when {
                                    pred.healthIn5Years < 50 -> Color.Red
                                    pred.healthIn5Years < 70 -> Color(0xFFFFA500)
                                    else -> Color.Green
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Recommandations",
                            style = MaterialTheme.typography.titleMedium
                        )
                        pred.recommendations.forEach { recommendation ->
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // History
        item {
            Text(
                text = "Historique",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(history) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = entry.timestamp.format(formatter),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoBox(
                            title = "Hauteur",
                            value = "${entry.height ?: "N/A"} m",
                            icon = Icons.Default.Height
                        )
                        InfoBox(
                            title = "Diamètre",
                            value = "${entry.diameter ?: "N/A"} cm",
                            icon = Icons.Default.Straighten
                        )
                        InfoBox(
                            title = "Santé",
                            value = "${entry.healthScore}%",
                            icon = Icons.Default.MedicalServices,
                            color = when {
                                entry.healthScore < 50 -> Color.Red
                                entry.healthScore < 70 -> Color(0xFFFFA500)
                                else -> Color.Green
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
} 