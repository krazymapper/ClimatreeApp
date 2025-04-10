package com.example.touuri.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaultOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class TreeHealthService {
    private val labeler: ImageLabeler by lazy {
        ImageLabeling.getClient(
            defaultOptions.Builder()
                .setConfidenceThreshold(0.5f)
                .build()
        )
    }

    private val isProcessing = AtomicBoolean(false)

    suspend fun analyzeHealth(bitmap: Bitmap): Result<TreeHealthAnalysis> = withContext(Dispatchers.IO) {
        if (!isProcessing.compareAndSet(false, true)) {
            return@withContext Result.failure(Exception("Une analyse est déjà en cours"))
        }

        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labels = labeler.process(image).await()
            
            val healthIndicators = labels.filter { label ->
                label.text.contains("disease", ignoreCase = true) ||
                label.text.contains("fungus", ignoreCase = true) ||
                label.text.contains("pest", ignoreCase = true) ||
                label.text.contains("dead", ignoreCase = true) ||
                label.text.contains("healthy", ignoreCase = true) ||
                label.text.contains("leaf", ignoreCase = true) ||
                label.text.contains("bark", ignoreCase = true)
            }

            val healthScore = calculateHealthScore(healthIndicators)
            val recommendations = generateRecommendations(healthIndicators, healthScore)

            Result.success(
                TreeHealthAnalysis(
                    healthScore = healthScore,
                    indicators = healthIndicators.map { it.text },
                    recommendations = recommendations
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            isProcessing.set(false)
        }
    }

    private fun calculateHealthScore(indicators: List<ImageLabel>): Int {
        var score = 100
        indicators.forEach { label ->
            when {
                label.text.contains("disease", ignoreCase = true) -> score -= 30
                label.text.contains("fungus", ignoreCase = true) -> score -= 25
                label.text.contains("pest", ignoreCase = true) -> score -= 20
                label.text.contains("dead", ignoreCase = true) -> score -= 40
                label.text.contains("healthy", ignoreCase = true) -> score += 10
            }
        }
        return score.coerceIn(0, 100)
    }

    private fun generateRecommendations(indicators: List<ImageLabel>, healthScore: Int): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (healthScore < 50) {
            recommendations.add("⚠️ L'arbre semble en mauvaise santé. Consultez un arboriculteur.")
        } else if (healthScore < 70) {
            recommendations.add("⚠️ L'arbre montre des signes de stress. Surveillez son évolution.")
        }

        indicators.forEach { label ->
            when {
                label.text.contains("disease", ignoreCase = true) -> 
                    recommendations.add("💊 Traitement recommandé : Appliquez un fongicide adapté")
                label.text.contains("fungus", ignoreCase = true) -> 
                    recommendations.add("🍄 Traitement recommandé : Nettoyez les zones infectées")
                label.text.contains("pest", ignoreCase = true) -> 
                    recommendations.add("🐛 Traitement recommandé : Utilisez un insecticide biologique")
                label.text.contains("dead", ignoreCase = true) -> 
                    recommendations.add("⚠️ Évaluation nécessaire : Consultez un professionnel")
            }
        }

        if (healthScore > 80) {
            recommendations.add("✅ L'arbre est en bonne santé. Continuez à le surveiller régulièrement.")
        }

        return recommendations
    }
}

data class TreeHealthAnalysis(
    val healthScore: Int,
    val indicators: List<String>,
    val recommendations: List<String>
) 