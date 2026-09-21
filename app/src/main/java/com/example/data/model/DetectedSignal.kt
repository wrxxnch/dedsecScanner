package com.example.data.model

enum class SignalType {
    WIFI,
    BLUETOOTH
}

data class DetectedSignal(
    val id: String,
    val type: SignalType,
    val name: String,         // SSID or BT Device Name
    val address: String,      // BSSID or BT MAC
    val rssi: Int,            // dBm
    val timestamp: Long = System.currentTimeMillis(),
    val matchedRule: String? = null,
    val frequencyOrExtra: String = ""
)
