package com.example.climatree.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.climatree.data.TreeARExperienceService
import com.example.climatree.data.TreeImpact
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import kotlinx.coroutines.launch

@Composable
fun TreeARExperience(
    species: String,
    height: Double?,
    diameter: Double?,
    age: Int?,
    impact: TreeImpact,
    showFuture: Boolean = false,
    futureHeight: Double? = null,
    futureDiameter: Double? = null,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var arSession by remember { mutableStateOf<Session?>(null) }
    var arExperience by remember { mutableStateOf<TreeARExperienceService?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // Vérifier si ARCore est disponible
            if (!ArCoreApk.getInstance().checkAvailability(context).isSupported) {
                throw Exception("ARCore n'est pas disponible sur cet appareil")
            }

            // Initialiser la session AR
            arSession = Session(context).apply {
                configure(
                    Config(this).apply {
                        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    }
                )
            }

            // Initialiser le service AR
            arExperience = TreeARExperienceService(context).apply {
                scope.launch {
                    initialize()
                    createTreeExperience(
                        species = species,
                        height = height,
                        diameter = diameter,
                        impact = impact,
                        showFuture = showFuture,
                        futureHeight = futureHeight,
                        futureDiameter = futureDiameter
                    )
                }
            }

            isLoading = false
        } catch (e: Exception) {
            error = e.message ?: "Erreur inconnue"
            onError(error!!)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            arSession?.close()
            arExperience?.cleanup()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                AndroidView(
                    factory = { ctx ->
                        ArSceneView(ctx).apply {
                            this.session = arSession
                            setupSession()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        try {
                            arSession?.let { session ->
                                if (session.resume()) {
                                    view.resume()
                                }
                            }
                            arExperience?.render(view.arFrame)
                        } catch (e: Exception) {
                            Log.e("TreeARExperience", "Erreur de rendu AR", e)
                        }
                    }
                )

                // Contrôles AR
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bouton pour basculer entre présent et futur
                    if (showFuture) {
                        Button(
                            onClick = {
                                arExperience?.createTreeExperience(
                                    species = species,
                                    height = height,
                                    diameter = diameter,
                                    impact = impact,
                                    showFuture = !showFuture,
                                    futureHeight = futureHeight,
                                    futureDiameter = futureDiameter
                                )
                            }
                        ) {
                            Text(if (showFuture) "Voir le présent" else "Voir le futur")
                        }
                    }

                    // Informations sur l'impact
                    Card(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Impact environnemental",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Séquestration CO₂: ${impact.carbonSequestration} kg/an")
                            Text("Production O₂: ${impact.oxygenProduction} kg/an")
                            Text("Valeur économique: ${impact.propertyValueIncrease}€")
                        }
                    }
                }
            }
        }
    }
} 