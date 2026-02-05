package com.example.smartagro.data.firestore

import android.util.Log
import com.example.smartagro.data.firebase.FirebaseProvider
import com.example.smartagro.domain.model.firestore.DeviceRequest
import com.example.smartagro.domain.model.firestore.UserDevice
import com.example.smartagro.domain.model.firestore.UserDoc
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val firestore: FirebaseFirestore? = FirebaseProvider.firestore
) {
    private val TAG = "FirestoreRepository"

    private val validStatuses = listOf("assigned", "device-assigned", "approved", "completed", "active")

    fun getUserActiveDeviceId(uid: String): Flow<String?> = callbackFlow {
        if (firestore == null) {
            Log.w(TAG, "Firestore not initialized. Returning null activeDeviceId.")
            trySend(null)
            close()
            return@callbackFlow
        }

        val userDocRef = firestore.collection(FirestorePaths.USERS).document(uid)

        Log.d(TAG, "Observing activeDeviceId for uid: $uid")

        val listenerRegistration = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing activeDeviceId", error)
                close(error)
                return@addSnapshotListener
            }

            try {
                val userDoc = snapshot?.toObject(UserDoc::class.java)
                val activeDeviceId = userDoc?.activeDeviceId
                Log.d(TAG, "ActiveDeviceId received: $activeDeviceId")
                trySend(activeDeviceId)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing activeDeviceId", e)
                trySend(null)
            }
        }

        awaitClose {
            Log.d(TAG, "Removing activeDeviceId listener for uid: $uid")
            listenerRegistration.remove()
        }
    }

    fun getUserDevices(uid: String): Flow<List<UserDevice>> = callbackFlow {
        if (firestore == null) {
            Log.w(TAG, "Firestore not initialized. Returning empty device list.")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val deviceRequestsRef = firestore.collection(FirestorePaths.DEVICE_REQUESTS)
            .whereEqualTo("userId", uid)
            .whereIn("status", validStatuses)

        Log.d(TAG, "Querying user devices for uid: $uid with statuses: $validStatuses")

        val listenerRegistration = deviceRequestsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error querying user devices", error)
                close(error)
                return@addSnapshotListener
            }

            try {
                val devices = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val request = doc.toObject(DeviceRequest::class.java)
                        val deviceId = request?.deviceId
                        if (deviceId != null) {
                            UserDevice(
                                deviceId = deviceId,
                                farmName = doc.getString("farmName"),
                                location = doc.getString("location"),
                                status = request.status ?: doc.getString("status")
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing device request doc ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "User devices received: ${devices.size} devices")
                trySend(devices)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing user devices", e)
                trySend(emptyList())
            }
        }

        awaitClose {
            Log.d(TAG, "Removing user devices listener for uid: $uid")
            listenerRegistration.remove()
        }
    }

    suspend fun updateActiveDeviceId(uid: String, deviceId: String?) {
        if (firestore == null) {
            Log.w(TAG, "Firestore not initialized. Cannot update activeDeviceId.")
            throw IllegalStateException("Firestore is not initialized. Please configure google-services.json")
        }

        val userDocRef = firestore.collection(FirestorePaths.USERS).document(uid)

        Log.d(TAG, "Updating activeDeviceId=$deviceId for uid: $uid")

        try {
            val updates = mapOf("activeDeviceId" to deviceId)
            userDocRef.update(updates).await()
            Log.d(TAG, "ActiveDeviceId updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating activeDeviceId", e)
            throw e
        }
    }
}
