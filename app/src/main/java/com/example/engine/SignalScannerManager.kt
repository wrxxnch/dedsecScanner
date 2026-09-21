package com.example.engine

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.AlertConfig
import com.example.data.model.DetectedSignal
import com.example.data.model.SignalType
import com.example.data.model.TargetRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ScannerStatus(
    val isScanning: Boolean = false,
    val wifiEnabled: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val lastScanTime: Long = 0L,
    val totalSignalsDetected: Int = 0,
    val totalMatchesDetected: Int = 0,
    val lastMatch: DetectedSignal? = null,
    val statusMessage: String = "IDLE // READY"
)

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val text: String,
    val isAlert: Boolean = false
)

class SignalScannerManager(
    private val context: Context,
    private val alertEngine: AlertEngine
) {

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val bluetoothManager =
        context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private val _status = MutableStateFlow(ScannerStatus())
    val status = _status.asStateFlow()

    private val _detectedSignals = MutableStateFlow<List<DetectedSignal>>(emptyList())
    val detectedSignals = _detectedSignals.asStateFlow()

    private val _matchedSignals = MutableStateFlow<List<DetectedSignal>>(emptyList())
    val matchedSignals = _matchedSignals.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLog>>(emptyList())
    val terminalLogs = _terminalLogs.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var scanLoopJob: Job? = null

    private var wifiReceiverRegistered = false
    private var btReceiverRegistered = false
    private var isBleScanning = false

    private var currentRules: List<TargetRule> = emptyList()
    private var currentConfig: AlertConfig = AlertConfig()

    // Wi-Fi BroadcastReceiver
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                processWifiResults()
            }
        }
    }

    // Bluetooth BroadcastReceiver (for Classic Bluetooth device discovery)
    private val btDeviceReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                val rssi: Short = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                if (device != null) {
                    val name = try {
                        device.name ?: "BT_DEV_${device.address.takeLast(5)}"
                    } catch (e: SecurityException) {
                        "BT_DEV_${device.address.takeLast(5)}"
                    }
                    val address = device.address ?: "00:00:00:00:00:00"
                    processSignal(
                        type = SignalType.BLUETOOTH,
                        name = name,
                        address = address,
                        rssi = rssi.toInt(),
                        extra = "CLASSIC_BT"
                    )
                }
            }
        }
    }

    // BLE ScanCallback
    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val device = it.device
                val name = try {
                    it.scanRecord?.deviceName ?: device?.name ?: "BLE_BEACON"
                } catch (e: SecurityException) {
                    "BLE_BEACON"
                }
                val address = device?.address ?: "00:00:00:00:00:00"
                processSignal(
                    type = SignalType.BLUETOOTH,
                    name = name,
                    address = address,
                    rssi = it.rssi,
                    extra = "BLE"
                )
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { onScanResult(0, it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE Scan Failed: $errorCode")
            logTerminal("WARN // BLE Scan Error Code: $errorCode", isAlert = false)
        }
    }

    fun updateRulesAndConfig(rules: List<TargetRule>, config: AlertConfig) {
        this.currentRules = rules
        this.currentConfig = config
    }

    fun startScanning() {
        if (_status.value.isScanning) return

        registerReceivers()
        _status.value = _status.value.copy(
            isScanning = true,
            statusMessage = "SCANNER ARMED // MONITORING SPECTRUM"
        )
        logTerminal("INIT // Dedicated DedSec Interceptor armed", isAlert = false)

        scanLoopJob = coroutineScope.launch {
            while (isActive) {
                triggerSingleScanCycle()
                val interval = currentConfig.autoScanIntervalSeconds.coerceAtLeast(3) * 1000L
                delay(interval)
            }
        }
    }

    fun stopScanning() {
        scanLoopJob?.cancel()
        scanLoopJob = null
        stopBleScanning()
        unregisterReceivers()

        _status.value = _status.value.copy(
            isScanning = false,
            statusMessage = "STANDBY // SCANNER PAUSED"
        )
        logTerminal("STANDBY // Interceptor paused", isAlert = false)
    }

    private fun triggerSingleScanCycle() {
        // WiFi scan
        try {
            if (wifiManager != null) {
                @Suppress("DEPRECATION")
                val success = wifiManager.startScan()
                if (success) {
                    _status.value = _status.value.copy(wifiEnabled = true)
                }
                // Also immediately read any cached results
                processWifiResults()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "WiFi scan permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "WiFi scan failed", e)
        }

        // Bluetooth scan
        startBleScanning()
    }

    @SuppressLint("MissingPermission")
    private fun processWifiResults() {
        try {
            val results = wifiManager?.scanResults ?: return
            for (res in results) {
                val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    res.wifiSsid?.toString()?.trim('"') ?: res.SSID ?: ""
                } else {
                    res.SSID ?: ""
                }
                val bssid = res.BSSID ?: "00:00:00:00:00:00"
                val rssi = res.level
                val freq = "${res.frequency}MHz"

                processSignal(
                    type = SignalType.WIFI,
                    name = ssid.ifBlank { "<HIDDEN_SSID>" },
                    address = bssid,
                    rssi = rssi,
                    extra = freq
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location or wifi permission to read scan results", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing WiFi scan results", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBleScanning() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return

        _status.value = _status.value.copy(bluetoothEnabled = true)

        try {
            val leScanner = adapter.bluetoothLeScanner
            if (leScanner != null && !isBleScanning) {
                isBleScanning = true
                leScanner.startScan(bleScanCallback)
                // Stop BLE scan after 5 seconds to conserve battery and cycle
                coroutineScope.launch {
                    delay(5000)
                    stopBleScanning()
                }
            }
            if (!adapter.isDiscovering) {
                adapter.startDiscovery()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Bluetooth permission missing", e)
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth scan error", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScanning() {
        if (!isBleScanning) return
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(bleScanCallback)
            bluetoothAdapter?.cancelDiscovery()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping BLE scan", e)
        } finally {
            isBleScanning = false
        }
    }

    /**
     * Central point for processing any detected wireless signal
     */
    fun processSignal(
        type: SignalType,
        name: String,
        address: String,
        rssi: Int,
        extra: String = ""
    ) {
        val id = "${type.name}_$address"
        val matchedRule = findMatchingRule(type, name, address)

        val signal = DetectedSignal(
            id = id,
            type = type,
            name = name,
            address = address,
            rssi = rssi,
            timestamp = System.currentTimeMillis(),
            matchedRule = matchedRule?.rawPattern,
            frequencyOrExtra = extra
        )

        // Update live list (deduplicate / update existing)
        val currentList = _detectedSignals.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            currentList[index] = signal
        } else {
            currentList.add(0, signal)
        }
        _detectedSignals.value = currentList.take(100)

        // If matched target rule:
        if (matchedRule != null) {
            val matchedList = _matchedSignals.value.toMutableList()
            matchedList.add(0, signal)
            _matchedSignals.value = matchedList.take(50)

            _status.value = _status.value.copy(
                totalMatchesDetected = _status.value.totalMatchesDetected + 1,
                lastMatch = signal
            )

            logTerminal(
                "MATCH DETECTED // [${type.name}] $name ($address) matched rule '${matchedRule.rawPattern}'",
                isAlert = true
            )

            // Trigger Alert (Vibration rhythm + sound + notification configured for this rule)
            alertEngine.triggerAlert(signal, currentConfig, matchedRule = matchedRule)
        }

        _status.value = _status.value.copy(
            totalSignalsDetected = _detectedSignals.value.size,
            lastScanTime = System.currentTimeMillis()
        )
    }

    private fun findMatchingRule(type: SignalType, name: String, address: String): TargetRule? {
        return currentRules.firstOrNull { rule ->
            when (type) {
                SignalType.WIFI -> rule.matches(ssid = name, bssid = address)
                SignalType.BLUETOOTH -> rule.matches(btName = name, btAddress = address)
            }
        }
    }

    /**
     * Inject a simulated signal for instant user testing and verification.
     */
    fun injectSimulatedSignal(type: SignalType, name: String, address: String, rssi: Int = -55) {
        logTerminal("SIMULATION INJECT // [${type.name}] $name | $address", isAlert = false)
        processSignal(type, name, address, rssi, extra = "SIMULATED")
    }

    fun clearHistory() {
        _detectedSignals.value = emptyList()
        _matchedSignals.value = emptyList()
        _terminalLogs.value = emptyList()
        logTerminal("SYS // Buffer purged", isAlert = false)
    }

    fun logTerminal(message: String, isAlert: Boolean = false) {
        val entry = TerminalLog(text = message, isAlert = isAlert)
        val logs = _terminalLogs.value.toMutableList()
        logs.add(0, entry)
        _terminalLogs.value = logs.take(150)
    }

    private fun registerReceivers() {
        try {
            if (!wifiReceiverRegistered) {
                context.registerReceiver(
                    wifiScanReceiver,
                    IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                )
                wifiReceiverRegistered = true
            }
            if (!btReceiverRegistered) {
                val btFilter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                context.registerReceiver(btDeviceReceiver, btFilter)
                btReceiverRegistered = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering receivers", e)
        }
    }

    private fun unregisterReceivers() {
        try {
            if (wifiReceiverRegistered) {
                context.unregisterReceiver(wifiScanReceiver)
                wifiReceiverRegistered = false
            }
            if (btReceiverRegistered) {
                context.unregisterReceiver(btDeviceReceiver)
                btReceiverRegistered = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers", e)
        }
    }

    companion object {
        private const val TAG = "SignalScannerManager"
    }
}
