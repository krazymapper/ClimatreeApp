package com.example.climatport.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import java.util.Date

data class TreeMission(
    val id: String = "",
    val title: String,
    val description: String,
    val area: GeoPoint, // Centre de la zone
    val radius: Double, // Rayon en mètres
    val targetCount: Int,
    val currentCount: Int = 0,
    val participants: List<String> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val deadline: Date? = null,
    val status: MissionStatus = MissionStatus.ACTIVE
)

data class Reward(
    val type: RewardType,
    val value: Int,
    val description: String
)

enum class RewardType {
    BADGE, POINTS, SPECIAL_TITLE
}

enum class MissionStatus {
    ACTIVE, COMPLETED, EXPIRED
}

class TreeMissionService {
    private val db = FirebaseFirestore.getInstance()
    private val missionsCollection = db.collection("missions")
    private val osmService = OsmService()
    private val exportService = ExportService()

    suspend fun createMission(mission: TreeMission): Result<TreeMission> {
        return try {
            val docRef = missionsCollection.document()
            val missionWithId = mission.copy(id = docRef.id)
            docRef.set(missionWithId).await()
            Result.success(missionWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinMission(missionId: String, userId: String): Result<Unit> {
        return try {
            val missionRef = missionsCollection.document(missionId)
            db.runTransaction { transaction ->
                val mission = transaction.get(missionRef).toObject(TreeMission::class.java)!!
                if (userId !in mission.participants) {
                    val updatedParticipants = mission.participants + userId
                    transaction.update(missionRef, "participants", updatedParticipants)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitTreeToMission(
        missionId: String,
        tree: TreeData,
        userId: String
    ): Result<Unit> {
        return try {
            val missionRef = missionsCollection.document(missionId)
            db.runTransaction { transaction ->
                val mission = transaction.get(missionRef).toObject(TreeMission::class.java)!!
                
                // Vérifier si l'arbre est dans la zone de la mission
                if (isInMissionArea(tree.location, mission.area, mission.radius)) {
                    // Ajouter l'arbre à OSM
                    osmService.uploadTree(tree)
                    
                    // Mettre à jour le compteur de la mission
                    val updatedCount = mission.currentCount + 1
                    transaction.update(missionRef, "currentCount", updatedCount)
                    
                    // Si la mission est complétée, mettre à jour son statut
                    if (updatedCount >= mission.targetCount) {
                        transaction.update(missionRef, "status", MissionStatus.COMPLETED)
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMissionProgress(missionId: String): Result<MissionProgress> {
        return try {
            val mission = missionsCollection.document(missionId).get().await()
                .toObject(TreeMission::class.java)!!
            
            // Générer le fichier CSV des arbres cartographiés
            val trees = getTreesInMissionArea(mission.area, mission.radius)
            val csvContent = exportService.generateCsv(trees)
            
            Result.success(
                MissionProgress(
                    currentCount = mission.currentCount,
                    targetCount = mission.targetCount,
                    participants = mission.participants.size,
                    csvContent = csvContent
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveMissions(location: GeoPoint): Result<List<TreeMission>> {
        return try {
            val snapshot = missionsCollection
                .whereEqualTo("status", MissionStatus.ACTIVE)
                .get()
                .await()
            
            val missions = snapshot.toObjects(TreeMission::class.java)
            val nearbyMissions = missions.filter { mission ->
                isInMissionArea(location, mission.area, mission.radius * 2) // Double radius for discovery
            }
            Result.success(nearbyMissions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getTreesInMissionArea(
        center: GeoPoint,
        radius: Double
    ): List<TreeData> {
        // Implémentation de la récupération des arbres dans la zone
        // Utiliser les mêmes techniques que dans TreeSocialService
        return emptyList() // Placeholder
    }

    private fun isInMissionArea(
        point: GeoPoint,
        center: GeoPoint,
        radius: Double
    ): Boolean {
        // Utiliser la même formule de distance que dans TreeSocialService
        val distance = calculateDistance(point, center)
        return distance <= radius
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        // Même implémentation que dans TreeSocialService
        val R = 6371000 // Earth's radius in meters
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}

data class MissionProgress(
    val currentCount: Int,
    val targetCount: Int,
    val participants: Int,
    val csvContent: String
) 