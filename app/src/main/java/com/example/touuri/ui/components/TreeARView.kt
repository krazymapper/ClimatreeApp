package com.example.touuri.ui.components

import android.content.Context
import android.util.Log
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
import com.example.touuri.data.TreeImpact
import com.example.touuri.data.TreeARService
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.Renderable
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeARView(
    species: String,
    height: Double?,
    diameter: Double?,
    age: Int?,
    impact: TreeImpact,
    showFuture: Boolean = false,
    futureHeight: Double? = null,
    futureDiameter: Double? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val arService = remember { TreeARService(context) }
    var sceneView by remember { mutableStateOf<ArSceneView?>(null) }
    var session by remember { mutableStateOf<Session?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(species) {
        try {
            arService.loadModels(species)
            isLoading = false
        } catch (e: Exception) {
            error = "Erreur de chargement des modèles 3D"
            Log.e("TreeARView", "Error loading models", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sceneView?.pause()
            session?.close()
            arService.cleanup()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                ArSceneView(ctx).apply {
                    sceneView = this
                    setupSession(ctx)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (!isLoading && error == null) {
                    setupScene(view, arService, height, diameter, impact, showFuture, futureHeight, futureDiameter)
                }
            }
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        error?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun ArSceneView.setupSession(context: Context) {
    try {
        val session = Session(context)
        val config = Config(session)
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        session.configure(config)
        this.session = session
    } catch (e: Exception) {
        Log.e("TreeARView", "Error setting up AR session", e)
    }
}

private fun setupScene(
    view: ArSceneView,
    arService: TreeARService,
    height: Double?,
    diameter: Double?,
    impact: TreeImpact,
    showFuture: Boolean,
    futureHeight: Double?,
    futureDiameter: Double?
) {
    val scene = view.scene
    scene.addOnUpdateListener { frameTime ->
        val frame = view.arFrame ?: return@addOnUpdateListener
        val pointCloud = frame.acquirePointCloud()
        
        // Trouver un plan horizontal pour placer l'arbre
        val planes = frame.getUpdatedTrackables(Plane::class.java)
        for (plane in planes) {
            if (plane.trackingState == Plane.TrackingState.TRACKING &&
                plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                
                val anchor = plane.createAnchor(plane.centerPose)
                if (showFuture) {
                    arService.createFutureTreeNode(
                        scene,
                        anchor,
                        height,
                        diameter,
                        futureHeight,
                        futureDiameter,
                        impact
                    )
                } else {
                    arService.createTreeNode(
                        scene,
                        anchor,
                        height,
                        diameter,
                        impact
                    )
                }
                scene.removeOnUpdateListener(this)
                break
            }
        }
    }
} 