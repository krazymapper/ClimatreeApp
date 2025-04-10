package com.example.touuri.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaultOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class TreeRecognitionService {
    private val labeler: ImageLabeler by lazy {
        ImageLabeling.getClient(
            defaultOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build()
        )
    }

    private val isProcessing = AtomicBoolean(false)

    suspend fun recognizeTree(bitmap: Bitmap): Result<TreeRecognitionResult> = withContext(Dispatchers.IO) {
        if (!isProcessing.compareAndSet(false, true)) {
            return@withContext Result.failure(Exception("Une reconnaissance est déjà en cours"))
        }

        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labels = labeler.process(image).await()
            
            val treeLabels = labels.filter { label ->
                label.text.contains("tree", ignoreCase = true) ||
                label.text.contains("plant", ignoreCase = true) ||
                label.text.contains("forest", ignoreCase = true)
            }

            if (treeLabels.isEmpty()) {
                Result.failure(Exception("Aucun arbre détecté dans l'image"))
            } else {
                val mainLabel = treeLabels.maxByOrNull { it.confidence }
                Result.success(
                    TreeRecognitionResult(
                        species = mainLabel?.text ?: "Arbre non identifié",
                        confidence = mainLabel?.confidence ?: 0f,
                        allLabels = treeLabels.map { it.text }
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            isProcessing.set(false)
        }
    }
}

data class TreeRecognitionResult(
    val species: String,
    val confidence: Float,
    val allLabels: List<String>
) 