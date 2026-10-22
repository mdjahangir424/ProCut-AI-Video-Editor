package com.example.core.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.database.ProjectJsonAdapter
import com.example.core.model.EditingProject
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSyncManager(context: Context) {
    private val db = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    private val auth = FirebaseAuth.getInstance()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    suspend fun syncProjectToCloud(project: EditingProject): Boolean = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: run {
            _syncMessage.value = "Sign in to backup to cloud"
            return@withContext false
        }

        _isSyncing.value = true
        _syncMessage.value = "Backing up to Firebase Cloud..."

        try {
            val json = ProjectJsonAdapter.toJson(project)
            val docRef = db.collection("users").document(user.uid)
                .collection("projects").document(project.id)

            val payload = hashMapOf<String, Any>(
                "id" to project.id,
                "userId" to user.uid,
                "title" to project.title,
                "aspectRatio" to project.aspectRatio.name,
                "resolution" to project.resolution.name,
                "frameRate" to project.frameRate.name,
                "totalDurationMs" to project.totalDurationMs,
                "thumbnailUri" to (project.thumbnailUri ?: ""),
                "projectJson" to json,
                "createdAt" to Timestamp(project.createdAt / 1000, 0),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(payload, SetOptions.merge()).await()
            _syncMessage.value = "Successfully synced to Firebase!"
            _isSyncing.value = false
            true
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Failed to sync project", e)
            _syncMessage.value = "Cloud backup failed: ${e.localizedMessage}"
            _isSyncing.value = false
            false
        }
    }
}
