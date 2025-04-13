package com.example.climatport.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

data class TreeProfile(
    val id: String = "",
    val name: String,
    val species: String,
    val ownerId: String,
    val location: GeoPoint,
    val photos: List<String> = emptyList(),
    val description: String = "",
    val familyId: String? = null,
    val createdAt: Date = Date(),
    val likes: Int = 0,
    val comments: List<Comment> = emptyList(),
    val milestones: List<Milestone> = emptyList()
)

data class Comment(
    val userId: String,
    val userName: String,
    val text: String,
    val timestamp: Date = Date()
)

data class Milestone(
    val type: MilestoneType,
    val date: Date,
    val description: String
)

enum class MilestoneType {
    PLANTING, FIRST_FLOWER, FIRST_FRUIT, GROWTH_SPURT, SPECIAL_EVENT
}

class TreeSocialService {
    private val db = FirebaseFirestore.getInstance()
    private val treesCollection = db.collection("trees")
    private val familiesCollection = db.collection("tree_families")

    suspend fun createTreeProfile(tree: TreeProfile): Result<TreeProfile> {
        return try {
            val docRef = treesCollection.document()
            val treeWithId = tree.copy(id = docRef.id)
            docRef.set(treeWithId).await()
            Result.success(treeWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTreeProfile(treeId: String): Result<TreeProfile> {
        return try {
            val snapshot = treesCollection.document(treeId).get().await()
            Result.success(snapshot.toObject(TreeProfile::class.java)!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(treeId: String, comment: Comment): Result<Unit> {
        return try {
            val treeRef = treesCollection.document(treeId)
            db.runTransaction { transaction ->
                val tree = transaction.get(treeRef).toObject(TreeProfile::class.java)!!
                val updatedComments = tree.comments + comment
                transaction.update(treeRef, "comments", updatedComments)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMilestone(treeId: String, milestone: Milestone): Result<Unit> {
        return try {
            val treeRef = treesCollection.document(treeId)
            db.runTransaction { transaction ->
                val tree = transaction.get(treeRef).toObject(TreeProfile::class.java)!!
                val updatedMilestones = tree.milestones + milestone
                transaction.update(treeRef, "milestones", updatedMilestones)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeTree(treeId: String): Result<Unit> {
        return try {
            val treeRef = treesCollection.document(treeId)
            db.runTransaction { transaction ->
                val tree = transaction.get(treeRef).toObject(TreeProfile::class.java)!!
                transaction.update(treeRef, "likes", tree.likes + 1)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTreeFamily(name: String, description: String, treeIds: List<String>): Result<String> {
        return try {
            val docRef = familiesCollection.document()
            val familyData = mapOf(
                "name" to name,
                "description" to description,
                "treeIds" to treeIds,
                "createdAt" to Date()
            )
            docRef.set(familyData).await()
            
            // Update trees to reference the family
            val batch = db.batch()
            treeIds.forEach { treeId ->
                val treeRef = treesCollection.document(treeId)
                batch.update(treeRef, "familyId", docRef.id)
            }
            batch.commit().await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNearbyTrees(location: GeoPoint, radiusInMeters: Double): Result<List<TreeProfile>> {
        return try {
            // Note: This is a simplified version. In production, you'd want to use
            // a proper geospatial query with Firestore
            val snapshot = treesCollection.get().await()
            val trees = snapshot.toObjects(TreeProfile::class.java)
            val nearbyTrees = trees.filter { tree ->
                val distance = calculateDistance(location, tree.location)
                distance <= radiusInMeters
            }
            Result.success(nearbyTrees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        // Simplified Haversine formula for demonstration
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