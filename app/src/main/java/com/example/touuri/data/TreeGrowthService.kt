package com.example.touuri.data

import android.graphics.Bitmap
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity(tableName = "tree_history")
data class TreeHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val treeId: String,
    val timestamp: LocalDateTime,
    val height: Double?,
    val diameter: Double?,
    val healthScore: Int,
    val photoPath: String?
)

@Dao
interface TreeHistoryDao {
    @Query("SELECT * FROM tree_history WHERE treeId = :treeId ORDER BY timestamp DESC")
    fun getTreeHistory(treeId: String): Flow<List<TreeHistory>>

    @Insert
    suspend fun insertHistory(history: TreeHistory)

    @Query("SELECT * FROM tree_history WHERE treeId = :treeId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHistory(treeId: String): TreeHistory?
}

@Database(entities = [TreeHistory::class], version = 1)
@TypeConverters(Converters::class)
abstract class TreeDatabase : RoomDatabase() {
    abstract fun treeHistoryDao(): TreeHistoryDao
}

class TreeGrowthService(private val dao: TreeHistoryDao) {
    suspend fun predictGrowth(treeId: String, currentHeight: Double?, currentDiameter: Double?): GrowthPrediction {
        val history = dao.getLatestHistory(treeId)
        if (history == null) {
            return GrowthPrediction(
                heightIn5Years = currentHeight?.let { it * 1.2 },
                diameterIn5Years = currentDiameter?.let { it * 1.15 },
                healthIn5Years = 80,
                recommendations = listOf(
                    "🌱 L'arbre est jeune. Surveillez sa croissance régulièrement.",
                    "💧 Assurez-vous d'un apport en eau suffisant pendant les premières années."
                )
            )
        }

        val monthsSinceLastUpdate = ChronoUnit.MONTHS.between(history.timestamp, LocalDateTime.now())
        val growthRate = calculateGrowthRate(history, currentHeight, currentDiameter, monthsSinceLastUpdate)

        return GrowthPrediction(
            heightIn5Years = currentHeight?.let { it * (1 + growthRate.heightRate * 5) },
            diameterIn5Years = currentDiameter?.let { it * (1 + growthRate.diameterRate * 5) },
            healthIn5Years = predictHealth(history.healthScore, growthRate),
            recommendations = generateGrowthRecommendations(growthRate, currentHeight, currentDiameter)
        )
    }

    private fun calculateGrowthRate(
        history: TreeHistory,
        currentHeight: Double?,
        currentDiameter: Double?,
        monthsSinceLastUpdate: Long
    ): GrowthRate {
        val heightRate = if (currentHeight != null && history.height != null) {
            (currentHeight - history.height) / (history.height * monthsSinceLastUpdate)
        } else 0.02 // Default rate

        val diameterRate = if (currentDiameter != null && history.diameter != null) {
            (currentDiameter - history.diameter) / (history.diameter * monthsSinceLastUpdate)
        } else 0.015 // Default rate

        return GrowthRate(heightRate, diameterRate)
    }

    private fun predictHealth(currentHealth: Int, growthRate: GrowthRate): Int {
        val healthChange = when {
            growthRate.heightRate > 0.03 -> 5 // Fast growth
            growthRate.heightRate < 0.01 -> -5 // Slow growth
            else -> 0
        }
        return (currentHealth + healthChange).coerceIn(0, 100)
    }

    private fun generateGrowthRecommendations(
        growthRate: GrowthRate,
        currentHeight: Double?,
        currentDiameter: Double?
    ): List<String> {
        val recommendations = mutableListOf<String>()

        when {
            growthRate.heightRate > 0.03 -> {
                recommendations.add("🌿 Croissance rapide détectée ! L'arbre se porte bien.")
                recommendations.add("✂️ Pensez à tailler les branches mortes pour maintenir une bonne structure.")
            }
            growthRate.heightRate < 0.01 -> {
                recommendations.add("⚠️ Croissance lente détectée. Vérifiez les conditions de croissance.")
                recommendations.add("🌱 Enrichissez le sol avec du compost pour stimuler la croissance.")
            }
        }

        if (currentHeight != null && currentHeight > 10.0) {
            recommendations.add("🌳 L'arbre atteint une taille mature. Surveillez sa santé.")
        }

        if (currentDiameter != null && currentDiameter > 50.0) {
            recommendations.add("🌲 Diamètre important. Assurez-vous que l'arbre a suffisamment d'espace.")
        }

        return recommendations
    }
}

data class GrowthRate(
    val heightRate: Double,
    val diameterRate: Double
)

data class GrowthPrediction(
    val heightIn5Years: Double?,
    val diameterIn5Years: Double?,
    val healthIn5Years: Int,
    val recommendations: List<String>
) 