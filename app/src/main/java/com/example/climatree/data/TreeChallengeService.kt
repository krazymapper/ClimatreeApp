package com.example.climatree.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import java.util.Date

data class TreeChallenge(
    val id: String = "",
    val title: String,
    val description: String,
    val type: ChallengeType,
    val startDate: Date,
    val endDate: Date,
    val targetCount: Int,
    val currentCount: Int = 0,
    val participants: List<String> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val status: ChallengeStatus = ChallengeStatus.UPCOMING
)

enum class ChallengeType {
    WEEKLY_CHALLENGE, // Défi hebdomadaire
    SPECIAL_EVENT, // Événement spécial (ex: Journée de l'arbre)
    COMMUNITY_CHALLENGE, // Défi communautaire
    URBAN_EXPLORATION // Exploration urbaine
}

enum class ChallengeStatus {
    UPCOMING, ACTIVE, COMPLETED, EXPIRED
}

data class UserBadge(
    val id: String = "",
    val userId: String,
    val badgeType: BadgeType,
    val earnedDate: Date = Date(),
    val progress: Int = 0,
    val level: Int = 1
)

enum class BadgeType {
    // Badges de progression
    TREE_EXPLORER, // 10 arbres
    TREE_MASTER, // 50 arbres
    TREE_LEGEND, // 100 arbres
    URBAN_FORESTER, // 500 arbres
    
    // Badges de spécialisation
    SPECIES_EXPERT, // 10 espèces différentes
    URBAN_GARDENER, // Arbres dans 5 quartiers différents
    CONSERVATION_HERO, // Arbres rares ou menacés
    COMMUNITY_LEADER, // Création de missions
    
    // Badges d'événements
    WEEKLY_CHAMPION, // Gagnant d'un défi hebdomadaire
    EVENT_PARTICIPANT, // Participation à un événement
    COMMUNITY_BUILDER // Organisation d'événements
}

class TreeChallengeService {
    private val db = FirebaseFirestore.getInstance()
    private val challengesCollection = db.collection("challenges")
    private val badgesCollection = db.collection("badges")
    private val missionService = TreeMissionService()

    suspend fun createChallenge(challenge: TreeChallenge): Result<TreeChallenge> {
        return try {
            val docRef = challengesCollection.document()
            val challengeWithId = challenge.copy(id = docRef.id)
            docRef.set(challengeWithId).await()
            Result.success(challengeWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinChallenge(challengeId: String, userId: String): Result<Unit> {
        return try {
            val challengeRef = challengesCollection.document(challengeId)
            db.runTransaction { transaction ->
                val challenge = transaction.get(challengeRef).toObject(TreeChallenge::class.java)!!
                if (userId !in challenge.participants) {
                    val updatedParticipants = challenge.participants + userId
                    transaction.update(challengeRef, "participants", updatedParticipants)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChallengeProgress(
        challengeId: String,
        userId: String,
        treesAdded: Int
    ): Result<Unit> {
        return try {
            val challengeRef = challengesCollection.document(challengeId)
            db.runTransaction { transaction ->
                val challenge = transaction.get(challengeRef).toObject(TreeChallenge::class.java)!!
                val updatedCount = challenge.currentCount + treesAdded
                transaction.update(challengeRef, "currentCount", updatedCount)
                
                // Vérifier si le défi est complété
                if (updatedCount >= challenge.targetCount) {
                    transaction.update(challengeRef, "status", ChallengeStatus.COMPLETED)
                    // Attribuer les récompenses
                    awardChallengeRewards(challenge, userId)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkAndAwardBadges(userId: String, treesAdded: Int): Result<List<UserBadge>> {
        return try {
            val userBadges = mutableListOf<UserBadge>()
            val totalTrees = getTotalTreesForUser(userId)
            
            // Vérifier les badges de progression
            val newBadges = checkProgressionBadges(userId, totalTrees)
            userBadges.addAll(newBadges)
            
            // Vérifier les badges de spécialisation
            val specializationBadges = checkSpecializationBadges(userId)
            userBadges.addAll(specializationBadges)
            
            // Sauvegarder les nouveaux badges
            userBadges.forEach { badge ->
                val docRef = badgesCollection.document()
                docRef.set(badge.copy(id = docRef.id)).await()
            }
            
            Result.success(userBadges)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getTotalTreesForUser(userId: String): Int {
        // Implémentation pour récupérer le nombre total d'arbres ajoutés par l'utilisateur
        return 0 // Placeholder
    }

    private suspend fun checkProgressionBadges(
        userId: String,
        totalTrees: Int
    ): List<UserBadge> {
        val badges = mutableListOf<UserBadge>()
        
        when {
            totalTrees >= 10 -> badges.add(createBadge(userId, BadgeType.TREE_EXPLORER, 1))
            totalTrees >= 50 -> badges.add(createBadge(userId, BadgeType.TREE_MASTER, 2))
            totalTrees >= 100 -> badges.add(createBadge(userId, BadgeType.TREE_LEGEND, 3))
            totalTrees >= 500 -> badges.add(createBadge(userId, BadgeType.URBAN_FORESTER, 4))
        }
        
        return badges
    }

    private suspend fun checkSpecializationBadges(userId: String): List<UserBadge> {
        // Implémentation pour vérifier les badges de spécialisation
        return emptyList() // Placeholder
    }

    private fun createBadge(
        userId: String,
        type: BadgeType,
        level: Int
    ): UserBadge {
        return UserBadge(
            userId = userId,
            badgeType = type,
            level = level
        )
    }

    private suspend fun awardChallengeRewards(
        challenge: TreeChallenge,
        userId: String
    ) {
        challenge.rewards.forEach { reward ->
            when (reward.type) {
                RewardType.BADGE -> {
                    val badge = createBadge(userId, BadgeType.WEEKLY_CHAMPION, 1)
                    badgesCollection.document().set(badge).await()
                }
                else -> {
                    // Gérer d'autres types de récompenses
                }
            }
        }
    }

    suspend fun getActiveChallenges(): Result<List<TreeChallenge>> {
        return try {
            val snapshot = challengesCollection
                .whereEqualTo("status", ChallengeStatus.ACTIVE)
                .get()
                .await()
            Result.success(snapshot.toObjects(TreeChallenge::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserBadges(userId: String): Result<List<UserBadge>> {
        return try {
            val snapshot = badgesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            Result.success(snapshot.toObjects(UserBadge::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 