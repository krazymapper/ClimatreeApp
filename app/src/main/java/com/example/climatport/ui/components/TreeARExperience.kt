package com.example.climatport.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.climatree.R
import com.example.climatree.data.TreeARExperienceService
import com.example.climatree.data.TreeImpact
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.launch

@Composable
fun TreeARExperience(
    treeSpecies: String,
    treeHeight: Float,
    treeDiameter: Float,
    treeAge: Int,
    environmentalImpact: TreeARExperienceService.EnvironmentalImpact,
    futureProjection: TreeARExperienceService.FutureProjection,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showFutureView by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Box(modifier = modifier.fillMaxSize()) {
        // Vue AR
        AndroidView(
            factory = { context ->
                ArFragment().apply {
                    setOnTapArPlaneListener { hitResult, plane, motionEvent ->
                        scope.launch {
                            try {
                                // Charger le modèle 3D de l'arbre
                                val treeModel = ModelRenderable.builder()
                                    .setSource(context, R.raw.tree_model)
                                    .build()
                                    .get()
                                
                                // Créer le nœud de l'arbre
                                val treeNode = TransformableNode(arSceneView.engine)
                                treeNode.renderable = treeModel
                                treeNode.setParent(arSceneView.scene)
                                treeNode.worldPosition = hitResult.hitPose
                                
                                // Ajouter les informations environnementales
                                addEnvironmentalInfo(treeNode, environmentalImpact)
                                
                                isLoading = false
                            } catch (e: Exception) {
                                error = e.message
                            }
                        }
                    }
                }.view
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Indicateur de chargement
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Message d'erreur
        if (error != null) {
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
        
        // Contrôles AR
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Bouton pour basculer entre la vue actuelle et future
                Button(
                    onClick = { showFutureView = !showFutureView },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showFutureView) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = if (showFutureView) 
                            Icons.Default.Timer 
                        else 
                            Icons.Default.Forest,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showFutureView) 
                            "Vue Actuelle" 
                        else 
                            "Vue Future"
                    )
                }
            }
            
            // Informations environnementales
            EnvironmentalInfoCard(
                impact = environmentalImpact,
                projection = futureProjection,
                showFutureView = showFutureView
            )
        }
    }
}

@Composable
fun EnvironmentalInfoCard(
    impact: TreeARExperienceService.EnvironmentalImpact,
    projection: TreeARExperienceService.FutureProjection,
    showFutureView: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (showFutureView) 
                    "Impact Environnemental (Projection)" 
                else 
                    "Impact Environnemental Actuel",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // CO2 absorbé
            InfoRow(
                icon = Icons.Default.Air,
                label = "CO2 absorbé",
                value = if (showFutureView) 
                    "${projection.co2Projection.last()} kg" 
                else 
                    "${impact.co2Absorbed} kg",
                color = Color(0xFF4CAF50)
            )
            
            // Oxygène produit
            InfoRow(
                icon = Icons.Default.WaterDrop,
                label = "Oxygène produit",
                value = if (showFutureView) 
                    "${projection.co2Projection.last() * 0.7} kg" 
                else 
                    "${impact.oxygenProduced} kg",
                color = Color(0xFF2196F3)
            )
            
            // Eau retenue
            InfoRow(
                icon = Icons.Default.Water,
                label = "Eau retenue",
                value = if (showFutureView) 
                    "${projection.co2Projection.last() * 2.5} L" 
                else 
                    "${impact.waterRetained} L",
                color = Color(0xFF00BCD4)
            )
            
            // Réduction de température
            InfoRow(
                icon = Icons.Default.Thermostat,
                label = "Réduction température",
                value = if (showFutureView) 
                    "${projection.co2Projection.last() * 0.01}°C" 
                else 
                    "${impact.temperatureReduction}°C",
                color = Color(0xFFFF9800)
            )
            
            // Score de biodiversité
            InfoRow(
                icon = Icons.Default.Park,
                label = "Score biodiversité",
                value = if (showFutureView) 
                    "${projection.healthProjection.last()}/100" 
                else 
                    "${impact.biodiversityScore}/100",
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
} 