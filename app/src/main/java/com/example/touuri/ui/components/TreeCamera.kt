package com.example.touuri.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.touuri.data.TreeRecognitionResult
import com.example.touuri.data.TreeRecognitionService
import com.example.touuri.data.TreeHealthService
import com.example.touuri.data.TreeHealthAnalysis
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeCamera(
    onTreeRecognized: (TreeRecognitionResult, Bitmap) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val treeRecognitionService = remember { TreeRecognitionService() }
    val treeHealthService = remember { TreeHealthService() }
    
    var isProcessing by remember { mutableStateOf(false) }
    var lastCapturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var recognitionResult by remember { mutableStateOf<TreeRecognitionResult?>(null) }
    var healthAnalysis by remember { mutableStateOf<TreeHealthAnalysis?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Camera Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onImageCaptured = { bitmap ->
                    lastCapturedImage = bitmap
                    isProcessing = true
                    scope.launch {
                        // Recognition
                        treeRecognitionService.recognizeTree(bitmap)
                            .onSuccess { result ->
                                recognitionResult = result
                                onTreeRecognized(result, bitmap)
                            }
                            .onFailure { error ->
                                onError(error.message ?: "Erreur de reconnaissance")
                            }

                        // Health Analysis
                        treeHealthService.analyzeHealth(bitmap)
                            .onSuccess { analysis ->
                                healthAnalysis = analysis
                            }
                            .onFailure { error ->
                                // Handle health analysis error silently
                            }
                        
                        isProcessing = false
                    }
                }
            )

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Recognition and Health Results
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recognitionResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Espèce détectée : ${result.species}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Confiance : ${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            healthAnalysis?.let { analysis ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Health Score
                        Text(
                            text = "Score de santé : ${analysis.healthScore}/100",
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                analysis.healthScore < 50 -> Color.Red
                                analysis.healthScore < 70 -> Color(0xFFFFA500) // Orange
                                else -> Color.Green
                            }
                        )

                        // Recommendations
                        Text(
                            text = "Recommandations :",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        analysis.recommendations.forEach { recommendation ->
                            Text(
                                text = recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    // Add capture button
                    previewView.setOnClickListener {
                        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                            context.createTempFile("IMG_", ".jpg")
                        ).build()

                        imageCapture.takePicture(
                            outputFileOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    val bitmap = BitmapFactory.decodeFile(outputFileResults.savedUri?.path)
                                    onImageCaptured(bitmap)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    // Handle error
                                }
                            }
                        )
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
} 