package com.example.touuri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.touuri.data.TreeImpact
import com.example.touuri.data.TreeImpactService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeImpactView(
    species: String,
    height: Double?,
    diameter: Double?,
    age: Int?
) {
    val impactService = remember { TreeImpactService() }
    val impact = remember(species, height, diameter, age) {
        impactService.calculateImpact(species, height, diameter, age)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Impact Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Impact de l'arbre",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ImpactBox(
                            title = "Carbone",
                            value = "${impact.carbonSequestration.toInt()} kg",
                            icon = Icons.Default.Forest,
                            color = Color(0xFF4CAF50)
                        )
                        ImpactBox(
                            title = "Oxygène",
                            value = "${impact.oxygenProduction.toInt()} kg/j",
                            icon = Icons.Default.Air,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }
        }

        // Environmental Impact
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Impact environnemental",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ImpactDetail(
                        title = "Réduction des eaux pluviales",
                        value = "${impact.stormwaterReduction.toInt()} L/an",
                        icon = Icons.Default.WaterDrop,
                        color = Color(0xFF00BCD4)
                    )
                    
                    ImpactDetail(
                        title = "Économies d'énergie",
                        value = "${impact.energySavings.toInt()} kWh/an",
                        icon = Icons.Default.Eco,
                        color = Color(0xFFFFC107)
                    )
                }
            }
        }

        // Economic Impact
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Impact économique",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ImpactDetail(
                        title = "Valeur immobilière",
                        value = "+${impact.propertyValueIncrease.toInt()}€",
                        icon = Icons.Default.AttachMoney,
                        color = Color(0xFF8BC34A)
                    )
                }
            }
        }

        // Recommendations
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recommandations",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    impact.recommendations.forEach { recommendation ->
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
}

@Composable
private fun ImpactBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@Composable
private fun ImpactDetail(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = color
            )
        }
    }
} 