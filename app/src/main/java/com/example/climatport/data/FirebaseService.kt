package com.example.climatport.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    suspend fun saveTree(treeData: TreeData): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val treeRef = firestore.collection("users").document(userId).collection("trees")
            val result = treeRef.add(treeData).await()
            Result.success(result.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrees(): Result<List<TreeData>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("trees")
                .get()
                .await()
            
            val trees = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TreeData::class.java)
            }
            Result.success(trees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadTreeImage(treeId: String, imageBytes: ByteArray): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val imageRef = storage.reference
                .child("users")
                .child(userId)
                .child("trees")
                .child("$treeId.jpg")
            
            val uploadTask = imageRef.putBytes(imageBytes).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 