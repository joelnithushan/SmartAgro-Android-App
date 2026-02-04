package com.example.smartagro.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagro.data.firebase.FirebaseProvider
import com.example.smartagro.data.firebase.RtdbRepository
import com.example.smartagro.domain.model.rtdb.RelayCommand
import com.example.smartagro.domain.model.rtdb.RelayControl
import com.example.smartagro.domain.model.rtdb.RelayStatusUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

data class IrrigationUiState(
    val deviceId: String? = null,
    val isOn: Boolean? = null,
    val statusSource: String = "unknown",
    val relayStatusUi: RelayStatusUi? = null,
    val relayControl: RelayControl? = null
)

class IrrigationRtdbViewModel(
    private val rtdbRepository: RtdbRepository
) : ViewModel() {

    private val TAG = "IrrigationRtdbViewModel"

    private val _uiState = MutableStateFlow(IrrigationUiState())
    val uiState: StateFlow<IrrigationUiState> = _uiState.asStateFlow()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _writing = MutableLiveData(false)
    val writing: LiveData<Boolean> = _writing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var observeJob: Job? = null

    fun observe(deviceIdFlow: StateFlow<String?>) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            deviceIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collect { deviceId ->
                    subscribe(deviceId)
                }
        }
    }

    private fun subscribe(deviceId: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            _errorMessage.postValue(null)
            _uiState.value = _uiState.value.copy(deviceId = deviceId)

            combine(
                rtdbRepository.observeSensorsLatest(deviceId),
                rtdbRepository.observeRelayStatus(deviceId),
                rtdbRepository.observeRelayControl(deviceId)
            ) { sensorsLatest, relayStatusUi, relayControl ->
                Triple(sensorsLatest, relayStatusUi, relayControl)
            }
                .catch { e ->
                    Log.e(TAG, "Error observing irrigation streams", e)
                    _errorMessage.postValue(e.message ?: "Error loading irrigation status")
                    _loading.postValue(false)
                }
                .collect { (sensorsLatest, relayStatusUi, relayControl) ->
                    val fromSensors = RelayCommand.fromRaw(sensorsLatest.relayStatus)
                    val fromControlStatus = relayStatusUi.value
                    val chosen = fromSensors ?: fromControlStatus

                    val source = when {
                        fromSensors != null -> "sensors/latest.relayStatus"
                        fromControlStatus != null -> "control/relay/status"
                        else -> "unknown"
                    }

                    _uiState.value = IrrigationUiState(
                        deviceId = deviceId,
                        isOn = when (chosen) {
                            RelayCommand.ON -> true
                            RelayCommand.OFF -> false
                            null -> null
                        },
                        statusSource = source,
                        relayStatusUi = relayStatusUi,
                        relayControl = relayControl
                    )
                    _loading.postValue(false)
                }
        }
    }

    fun writeRelay(turnOn: Boolean, mirrorStatus: Boolean = true) {
        val deviceId = _uiState.value.deviceId ?: return
        val command = if (turnOn) "on" else "off"

        _writing.postValue(true)
        _errorMessage.postValue(null)
        viewModelScope.launch {
            try {
                rtdbRepository.writeRelayCommand(deviceId, command, mirrorStatus = mirrorStatus)
                Log.d(TAG, "Relay command written: $command for deviceId=$deviceId")
            } catch (e: Exception) {
                Log.e(TAG, "Error writing relay command", e)
                _errorMessage.postValue(e.message ?: "Error sending command")
            } finally {
                _writing.postValue(false)
            }
        }
    }
}

