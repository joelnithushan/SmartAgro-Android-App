package com.example.smartagro.data.firebase

object DeviceRtdbPaths {
    private const val DEVICES = "devices"
    private const val SENSORS = "sensors"
    private const val LATEST = "latest"
    private const val META = "meta"
    private const val LAST_SEEN = "lastSeen"
    private const val CONTROLS = "controls"
    private const val RELAY_COMMAND = "relayCommand"
    private const val CONTROL = "control"
    private const val RELAY = "relay"
    private const val STATUS = "status"

    fun sensorsLatest(deviceId: String) = "$DEVICES/$deviceId/$SENSORS/$LATEST"

    fun metaLastSeen(deviceId: String) = "$DEVICES/$deviceId/$META/$LAST_SEEN"

    fun controlsRelayCommand(deviceId: String) = "$DEVICES/$deviceId/$CONTROLS/$RELAY_COMMAND"

    fun controlsRelayStatus(deviceId: String) = "$DEVICES/$deviceId/$CONTROLS/$RELAY_STATUS"

    fun controlRelayStatus(deviceId: String) = "$DEVICES/$deviceId/$CONTROL/$RELAY/$STATUS"

    fun controlRelay(deviceId: String) = "$DEVICES/$deviceId/$CONTROL/$RELAY"

    private const val RELAY_STATUS = "relayStatus"
}

