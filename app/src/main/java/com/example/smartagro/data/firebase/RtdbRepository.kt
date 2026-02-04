package com.example.smartagro.data.firebase

import android.util.Log
import com.example.smartagro.data.firebase.DeviceRtdbPaths
import com.example.smartagro.domain.model.rtdb.RelayCommand
import com.example.smartagro.domain.model.rtdb.RelayControl
import com.example.smartagro.domain.model.rtdb.RelayStatus
import com.example.smartagro.domain.model.rtdb.RelayStatusUi
import com.example.smartagro.domain.model.rtdb.RtdbRelay
import com.example.smartagro.domain.model.rtdb.RtdbSensorLatest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RtdbRepository(
    private val auth: FirebaseAuth = FirebaseProvider.auth
) {
    private val database = FirebaseProvider.rtdb
    private val TAG = "RtdbRepository"

    fun observeSensorsLatest(deviceId: String): Flow<RtdbSensorLatest> = callbackFlow {
        val path = DeviceRtdbPaths.sensorsLatest(deviceId)
        val sensorsRef = database.getReference(path)

        Log.d(TAG, "Observing sensors latest at path: $path")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val sensorData = snapshot.getValue(RtdbSensorLatest::class.java)
                    val result = sensorData ?: RtdbSensorLatest()
                    Log.d(TAG, "Sensor latest data received: $result")
                    trySend(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing sensor latest data", e)
                    trySend(RtdbSensorLatest())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Sensor latest listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        sensorsRef.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "Removing sensor latest listener from path: $path")
            sensorsRef.removeEventListener(listener)
        }
    }

    fun observeLastSeen(deviceId: String): Flow<Long?> = callbackFlow {
        val path = DeviceRtdbPaths.metaLastSeen(deviceId)
        val lastSeenRef = database.getReference(path)

        Log.d(TAG, "Observing lastSeen at path: $path")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val lastSeen = snapshot.getValue(Long::class.java)
                    Log.d(TAG, "LastSeen received: $lastSeen")
                    trySend(lastSeen)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing lastSeen", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "LastSeen listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        lastSeenRef.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "Removing lastSeen listener from path: $path")
            lastSeenRef.removeEventListener(listener)
        }
    }

    fun observeRelayStatus(deviceId: String): Flow<RelayStatusUi> = callbackFlow {
        val path = DeviceRtdbPaths.controlRelayStatus(deviceId)
        val statusRef = database.getReference(path)

        Log.d(TAG, "Observing relay status at path: $path")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val relayStatus = RelayStatus.parse(snapshot)
                    val uiStatus = RelayStatusUi.fromRelayStatus(relayStatus)
                    Log.d(TAG, "Relay status received: $uiStatus")
                    trySend(uiStatus)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing relay status", e)
                    trySend(RelayStatusUi.fromRelayStatus(null))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Relay status listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        statusRef.addValueEventListener(listener)

        awaitClose {
            Log.d(TAG, "Removing relay status listener from path: $path")
            statusRef.removeEventListener(listener)
        }
    }

    fun observeRelayControl(deviceId: String): Flow<RelayControl?> = callbackFlow {
        val relayPath = DeviceRtdbPaths.controlRelay(deviceId)
        val statusPath = DeviceRtdbPaths.controlRelayStatus(deviceId)
        val relayRef = database.getReference(relayPath)
        val statusRef = database.getReference(statusPath)

        Log.d(TAG, "Observing relay control at path: $relayPath")

        var relaySnapshot: DataSnapshot? = null
        var statusSnapshot: DataSnapshot? = null

        fun tryEmit() {
            if (relaySnapshot != null && statusSnapshot != null) {
                try {
                    val rtdbRelay = relaySnapshot!!.getValue(RtdbRelay::class.java) ?: RtdbRelay()
                    val control = RelayControl.fromRtdbRelay(rtdbRelay, statusSnapshot)
                    Log.d(TAG, "Relay control received: $control")
                    trySend(control)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing relay control", e)
                    trySend(null)
                }
            }
        }

        val relayListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                relaySnapshot = snapshot
                tryEmit()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Relay control listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        val statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                statusSnapshot = snapshot
                tryEmit()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Relay status listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        relayRef.addValueEventListener(relayListener)
        statusRef.addValueEventListener(statusListener)

        awaitClose {
            Log.d(TAG, "Removing relay control listeners from paths: $relayPath, $statusPath")
            relayRef.removeEventListener(relayListener)
            statusRef.removeEventListener(statusListener)
        }
    }

    suspend fun writeRelayCommand(deviceId: String, command: String, mirrorStatus: Boolean = true) {
        val path = DeviceRtdbPaths.controlsRelayCommand(deviceId)
        val commandRef = database.getReference(path)

        Log.d(TAG, "Writing relay command=$command to path: $path")

        try {
            commandRef.setValue(command).await()
            Log.d(TAG, "Relay command written successfully")

            val currentUser = auth.currentUser
            if (mirrorStatus && currentUser != null) {
                val statusPath = DeviceRtdbPaths.controlRelayStatus(deviceId)
                val statusRef = database.getReference(statusPath)

                val statusPayload = mapOf(
                    "value" to command,
                    "requestedBy" to currentUser.uid,
                    "requestedByEmail" to (currentUser.email ?: ""),
                    "timestamp" to System.currentTimeMillis()
                )

                statusRef.setValue(statusPayload).await()
                Log.d(TAG, "Relay status mirrored successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing relay command", e)
            throw e
        }
    }
}
