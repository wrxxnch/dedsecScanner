package com.example.ui

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TargetRepository
import com.example.data.model.AlertConfig
import com.example.data.model.AlertMode
import com.example.data.model.DetectedSignal
import com.example.data.model.SignalType
import com.example.data.model.SoundSourceType
import com.example.data.model.TargetRule
import com.example.data.model.TargetType
import com.example.engine.AlertEngine
import com.example.engine.ScannerStatus
import com.example.engine.SignalScannerManager
import com.example.engine.TerminalLog
import com.example.engine.WigleParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.data.model.RecentSound
import com.example.data.auth.AuthRepository
import com.example.data.auth.DedsecUser
import com.example.data.update.GitHubReleaseInfo
import com.example.data.update.GitHubUpdateManager
import com.example.data.update.UpdateState

data class ScannerUiState(
    val rules: List<TargetRule> = emptyList(),
    val config: AlertConfig = AlertConfig(),
    val recentSounds: List<RecentSound> = emptyList(),
    val selectedTab: Int = 0, // 0: Radar, 1: Targets, 2: Alert Config, 3: Console, 4: Admin
    val isTestingAlert: Boolean = false,
    val isTestingTts: Boolean = false,
    val showAddDialog: Boolean = false,
    val showBulkImportDialog: Boolean = false,
    val showSimulationDialog: Boolean = false,
    val editingRule: TargetRule? = null,
    val toastMessage: String? = null,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null
)

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TargetRepository(application)
    val alertEngine = AlertEngine(application)
    val scannerManager = SignalScannerManager(application, alertEngine)
    val authRepository = AuthRepository(application)
    val updateManager = GitHubUpdateManager(application)

    val currentUser: StateFlow<DedsecUser?> = authRepository.currentUser
    val allUsers: StateFlow<List<DedsecUser>> = authRepository.usersList
    val updateState: StateFlow<UpdateState> = updateManager.updateState

    private val _uiState = MutableStateFlow(
        ScannerUiState(
            rules = repository.loadRules(),
            config = repository.loadConfig(),
            recentSounds = repository.loadRecentSounds()
        )
    )
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    val scannerStatus: StateFlow<ScannerStatus> = scannerManager.status
    val detectedSignals: StateFlow<List<DetectedSignal>> = scannerManager.detectedSignals
    val matchedSignals: StateFlow<List<DetectedSignal>> = scannerManager.matchedSignals
    val terminalLogs: StateFlow<List<TerminalLog>> = scannerManager.terminalLogs

    init {
        syncScannerRules()
    }

    private fun syncScannerRules() {
        scannerManager.updateRulesAndConfig(_uiState.value.rules, _uiState.value.config)
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleScanning() {
        if (scannerStatus.value.isScanning) {
            scannerManager.stopScanning()
        } else {
            syncScannerRules()
            scannerManager.startScanning()
        }
    }

    fun addRule(
        pattern: String,
        type: TargetType = TargetType.ANY,
        alertMode: AlertMode = AlertMode.VIBRATE_AND_SOUND,
        morsePattern: String = "...---...",
        notificationTitle: String = "⚠️ ALVO // {{nome}}",
        notificationMessage: String = "[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}} | {{time}}",
        soundSourceType: SoundSourceType = SoundSourceType.BUILT_IN_CYBER_SYNTH,
        customAudioUri: String? = null,
        customAudioName: String? = null,
        soundVolume: Float = 1.0f,
        ttsEnabled: Boolean = false,
        ttsText: String = "Alvo detectado: {{nome}}, sinal {{sinal}}",
        ttsPitch: Float = 1.0f,
        ttsSpeed: Float = 1.0f
    ) {
        val trimmed = pattern.trim()
        if (trimmed.isBlank()) return

        val newRule = TargetRule(
            rawPattern = trimmed,
            targetType = type,
            alertMode = alertMode,
            morsePattern = morsePattern,
            notificationTitle = notificationTitle,
            notificationMessage = notificationMessage,
            soundSourceType = soundSourceType,
            customAudioUri = customAudioUri,
            customAudioName = customAudioName,
            soundVolume = soundVolume,
            ttsEnabled = ttsEnabled,
            ttsText = ttsText,
            ttsPitch = ttsPitch,
            ttsSpeed = ttsSpeed
        )
        val updated = listOf(newRule) + _uiState.value.rules
        _uiState.value = _uiState.value.copy(rules = updated, showAddDialog = false)
        repository.saveRules(updated)
        syncScannerRules()
        scannerManager.logTerminal("TARGET ADDED // Pattern: $trimmed (${type.name}) [${alertMode.name}]", isAlert = false)
    }

    fun openEditRule(rule: TargetRule) {
        _uiState.value = _uiState.value.copy(editingRule = rule)
    }

    fun closeEditRule() {
        _uiState.value = _uiState.value.copy(editingRule = null)
    }

    fun saveEditedRule(updatedRule: TargetRule) {
        val updated = _uiState.value.rules.map {
            if (it.id == updatedRule.id) updatedRule else it
        }
        _uiState.value = _uiState.value.copy(rules = updated, editingRule = null)
        repository.saveRules(updated)
        syncScannerRules()
        scannerManager.logTerminal(
            "TARGET UPDATED // ${updatedRule.rawPattern} [${updatedRule.alertMode.name}] Morse: ${updatedRule.morsePattern}",
            isAlert = false
        )
    }

    fun testRule(rule: TargetRule) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingAlert = true)
            alertEngine.testRuleAlert(rule, _uiState.value.config)
            kotlinx.coroutines.delay(1200)
            _uiState.value = _uiState.value.copy(isTestingAlert = false)
        }
    }

    fun toggleRule(id: String) {
        val updated = _uiState.value.rules.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        }
        _uiState.value = _uiState.value.copy(rules = updated)
        repository.saveRules(updated)
        syncScannerRules()
    }

    fun deleteRule(id: String) {
        val updated = _uiState.value.rules.filterNot { it.id == id }
        _uiState.value = _uiState.value.copy(rules = updated)
        repository.saveRules(updated)
        syncScannerRules()
        scannerManager.logTerminal("TARGET REMOVED // ID: $id", isAlert = false)
    }

    fun clearAllRules() {
        _uiState.value = _uiState.value.copy(rules = emptyList())
        repository.saveRules(emptyList())
        syncScannerRules()
        scannerManager.logTerminal("TARGETS CLEARED // Watchlist emptied", isAlert = false)
    }

    /**
     * Bulk import from space/newline separated text
     */
    fun importFromText(text: String) {
        viewModelScope.launch {
            val result = WigleParser.parseContent(text)
            if (result.rules.isNotEmpty()) {
                val existing = _uiState.value.rules.map { it.rawPattern.lowercase() }.toSet()
                val newOnes = result.rules.filter { it.rawPattern.lowercase() !in existing }
                val merged = newOnes + _uiState.value.rules

                _uiState.value = _uiState.value.copy(
                    rules = merged,
                    showBulkImportDialog = false,
                    toastMessage = "Imported ${newOnes.size} targets (${result.formatDetected})"
                )
                repository.saveRules(merged)
                syncScannerRules()
                scannerManager.logTerminal(
                    "BULK IMPORT // +${newOnes.size} rules loaded (${result.formatDetected})",
                    isAlert = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    toastMessage = "No valid patterns found in input text"
                )
            }
        }
    }

    /**
     * Import from selected File URI (WiGLE CSV or Text list)
     */
    fun importFromFileUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: ""

                val result = WigleParser.parseContent(content)
                withContext(Dispatchers.Main) {
                    if (result.rules.isNotEmpty()) {
                        val existing = _uiState.value.rules.map { it.rawPattern.lowercase() }.toSet()
                        val newOnes = result.rules.filter { it.rawPattern.lowercase() !in existing }
                        val merged = newOnes + _uiState.value.rules

                        _uiState.value = _uiState.value.copy(
                            rules = merged,
                            toastMessage = "Loaded ${newOnes.size} patterns from file (${result.formatDetected})"
                        )
                        repository.saveRules(merged)
                        syncScannerRules()
                        scannerManager.logTerminal(
                            "FILE IMPORT // ${newOnes.size} rules from URI (${result.formatDetected})",
                            isAlert = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            toastMessage = "No valid targets or WiGLE columns detected in file"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "Failed to read file: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Set Custom Sound URI picked by user for Global AlertConfig
     */
    fun setCustomAudioUri(uri: Uri) {
        val (persistentUri, fileName) = repository.importAudioFileToInternalStorage(uri)
        val updatedConfig = _uiState.value.config.copy(
            soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE,
            customAudioUri = persistentUri,
            customAudioName = fileName
        )
        updateConfig(updatedConfig)
        _uiState.value = _uiState.value.copy(recentSounds = repository.loadRecentSounds())
        scannerManager.logTerminal("AUDIO LOADED // $fileName", isAlert = false)
    }

    /**
     * Set Custom Sound URI picked by user for a specific TargetRule
     */
    fun importAudioForRule(ruleId: String, uri: Uri) {
        val (persistentUri, fileName) = repository.importAudioFileToInternalStorage(uri)
        val updatedRules = _uiState.value.rules.map { rule ->
            if (rule.id == ruleId) {
                rule.copy(
                    soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE,
                    customAudioUri = persistentUri,
                    customAudioName = fileName
                )
            } else rule
        }
        val updatedEditing = if (_uiState.value.editingRule?.id == ruleId) {
            _uiState.value.editingRule?.copy(
                soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE,
                customAudioUri = persistentUri,
                customAudioName = fileName
            )
        } else _uiState.value.editingRule

        _uiState.value = _uiState.value.copy(
            rules = updatedRules,
            editingRule = updatedEditing,
            recentSounds = repository.loadRecentSounds(),
            toastMessage = "Audio vinculado à regra: $fileName"
        )
        repository.saveRules(updatedRules)
        syncScannerRules()
        scannerManager.logTerminal("RULE AUDIO LOADED // $fileName for rule $ruleId", isAlert = false)
    }

    fun selectRecentSoundForConfig(sound: RecentSound) {
        val updatedConfig = _uiState.value.config.copy(
            soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE,
            customAudioUri = sound.uriString,
            customAudioName = sound.name
        )
        updateConfig(updatedConfig)
        scannerManager.logTerminal("RECENT AUDIO SELECTED // ${sound.name}", isAlert = false)
    }

    fun selectRecentSoundForRule(ruleId: String, sound: RecentSound) {
        val updatedRules = _uiState.value.rules.map { rule ->
            if (rule.id == ruleId) {
                rule.copy(
                    soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE,
                    customAudioUri = sound.uriString,
                    customAudioName = sound.name
                )
            } else rule
        }
        val updatedEditing = if (_uiState.value.editingRule?.id == ruleId) {
            _uiState.value.editingRule?.copy(
                soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE,
                customAudioUri = sound.uriString,
                customAudioName = sound.name
            )
        } else _uiState.value.editingRule

        _uiState.value = _uiState.value.copy(
            rules = updatedRules,
            editingRule = updatedEditing,
            toastMessage = "Som recente aplicado: ${sound.name}"
        )
        repository.saveRules(updatedRules)
        syncScannerRules()
    }

    fun deleteRecentSound(soundId: String) {
        repository.removeRecentSound(soundId)
        _uiState.value = _uiState.value.copy(recentSounds = repository.loadRecentSounds())
    }

    fun previewSound(uriString: String, volume: Float = 1.0f) {
        alertEngine.previewCustomAudio(uriString, volume)
    }

    fun testTts(text: String, pitch: Float, speed: Float) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingTts = true)
            alertEngine.speakText(text, pitch, speed)
            kotlinx.coroutines.delay(1800)
            _uiState.value = _uiState.value.copy(isTestingTts = false)
        }
    }

    fun stopTts() {
        alertEngine.stopTts()
    }

    fun updateConfig(config: AlertConfig) {
        _uiState.value = _uiState.value.copy(config = config)
        repository.saveConfig(config)
        syncScannerRules()
    }

    fun updateVibrationIntensity(intensity: Int) {
        updateConfig(_uiState.value.config.copy(vibrationIntensity = intensity))
    }

    fun updateRhythmPattern(rhythm: String) {
        updateConfig(_uiState.value.config.copy(rhythmPattern = rhythm))
    }

    fun updateAlertMode(mode: AlertMode) {
        updateConfig(_uiState.value.config.copy(alertMode = mode))
    }

    fun updateSoundSourceType(type: SoundSourceType) {
        updateConfig(_uiState.value.config.copy(soundSourceType = type))
    }

    fun updateVolume(vol: Float) {
        updateConfig(_uiState.value.config.copy(soundVolume = vol))
    }

    fun updateCooldown(seconds: Int) {
        updateConfig(_uiState.value.config.copy(cooldownSeconds = seconds))
    }

    fun testCurrentAlert() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingAlert = true)
            alertEngine.testAlert(_uiState.value.config)
            kotlinx.coroutines.delay(1200)
            _uiState.value = _uiState.value.copy(isTestingAlert = false)
        }
    }

    fun stopVibration() {
        alertEngine.stopVibration()
    }

    fun injectSimulation(type: SignalType, name: String, address: String, rssi: Int) {
        scannerManager.injectSimulatedSignal(type, name, address, rssi)
        _uiState.value = _uiState.value.copy(showSimulationDialog = false)
    }

    fun setDialogVisible(add: Boolean? = null, bulk: Boolean? = null, sim: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            showAddDialog = add ?: _uiState.value.showAddDialog,
            showBulkImportDialog = bulk ?: _uiState.value.showBulkImportDialog,
            showSimulationDialog = sim ?: _uiState.value.showSimulationDialog
        )
    }

    /**
     * Sends a signal directly from Home (Radar) to Rules as disabled by default,
     * navigates to Alert Config / Target Rules so user can customize it.
     */
    fun sendSignalToRules(signal: DetectedSignal, patternType: String) {
        val pattern = if (patternType.equals("MAC", ignoreCase = true)) {
            signal.address.trim()
        } else {
            signal.name.trim().ifBlank { signal.address.trim() }
        }

        val type = when {
            patternType.equals("MAC", ignoreCase = true) && signal.type == SignalType.WIFI -> TargetType.WIFI_BSSID
            patternType.equals("MAC", ignoreCase = true) && signal.type == SignalType.BLUETOOTH -> TargetType.BLUETOOTH_MAC
            signal.type == SignalType.WIFI -> TargetType.WIFI_SSID
            else -> TargetType.BLUETOOTH_NAME
        }

        val newRule = TargetRule(
            rawPattern = pattern,
            targetType = type,
            enabled = false, // Mandado padrão desativado
            notificationTitle = "⚠️ ALVO // {{nome}}",
            notificationMessage = "[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}}"
        )

        val updated = listOf(newRule) + _uiState.value.rules
        _uiState.value = _uiState.value.copy(
            rules = updated,
            editingRule = newRule, // open in edit mode right away
            selectedTab = 2, // Navigates to Alert Config as requested
            toastMessage = "REGRA ADICIONADA (DESATIVADA) // Editando parâmetros de alerta..."
        )
        repository.saveRules(updated)
        syncScannerRules()
        scannerManager.logTerminal("RULE INJECTED (DISABLED) // $pattern (${type.name})", isAlert = false)
    }

    // Auth actions
    fun authenticateGoogleUser(email: String, displayName: String) {
        _uiState.value = _uiState.value.copy(isAuthLoading = true, authErrorMessage = null)
        val result = authRepository.onGoogleSignInSuccess(email, displayName)
        _uiState.value = _uiState.value.copy(
            isAuthLoading = false,
            authErrorMessage = if (!result.first) result.second else null,
            toastMessage = if (result.first) result.second else null
        )
        if (result.first) {
            scannerManager.logTerminal("GOOGLE AUTH SUCCESS // User: $email", isAlert = false)
        } else {
            scannerManager.logTerminal("AUTH DENIED // $email: ${result.second}", isAlert = true)
        }
    }

    fun signOut() {
        authRepository.signOut()
        scannerManager.stopScanning()
        _uiState.value = _uiState.value.copy(
            selectedTab = 0,
            toastMessage = "Sessão finalizada."
        )
    }

    fun addAdminEmail(email: String) {
        val (success, message) = authRepository.addAdminEmail(email)
        _uiState.value = _uiState.value.copy(toastMessage = message)
        scannerManager.logTerminal(message, isAlert = !success)
    }

    fun toggleBlockUser(email: String, block: Boolean) {
        val (success, message) = authRepository.setUserBlocked(email, block)
        _uiState.value = _uiState.value.copy(toastMessage = message)
        scannerManager.logTerminal(message, isAlert = !success)
    }

    fun removeUser(email: String) {
        val (success, message) = authRepository.removeUser(email)
        _uiState.value = _uiState.value.copy(toastMessage = message)
        scannerManager.logTerminal(message, isAlert = !success)
    }

    fun toggleAdminPrivilege(email: String) {
        val (success, message) = authRepository.toggleAdmin(email)
        _uiState.value = _uiState.value.copy(toastMessage = message)
        scannerManager.logTerminal(message, isAlert = !success)
    }

    // GitHub Auto-Update actions
    fun checkForGitHubUpdates(mandatory: Boolean = true) {
        viewModelScope.launch {
            updateManager.checkForUpdates(mandatory = mandatory)
        }
    }

    fun downloadAndInstallUpdate(release: GitHubReleaseInfo) {
        viewModelScope.launch {
            updateManager.downloadAndInstallApk(release)
        }
    }

    fun dismissNonMandatoryUpdate() {
        updateManager.dismissNonMandatory()
    }

    fun openBrowserUrl(url: String) {
        updateManager.openUrlInBrowser(url)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun clearLogs() {
        scannerManager.clearHistory()
    }

    override fun onCleared() {
        super.onCleared()
        scannerManager.stopScanning()
        alertEngine.stopVibration()
    }
}
