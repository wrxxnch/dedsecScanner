package com.example.data.model

enum class AlertMode {
    VIBRATE_ONLY,
    SOUND_ONLY,
    VIBRATE_AND_SOUND,
    NOTIFICATION_ONLY
}

enum class SoundSourceType {
    BUILT_IN_CYBER_SYNTH,
    CUSTOM_AUDIO_FILE
}

data class AlertConfig(
    val alertMode: AlertMode = AlertMode.VIBRATE_AND_SOUND,
    val vibrationIntensity: Int = 255, // 1 - 255 (amplitude)
    val rhythmPattern: String = "...---.../...---...", // Morse SOS or ms timings
    val soundSourceType: SoundSourceType = SoundSourceType.BUILT_IN_CYBER_SYNTH,
    val customAudioUri: String? = null,
    val customAudioName: String? = null,
    val soundVolume: Float = 0.9f,
    val cooldownSeconds: Int = 5,
    val customNotificationTitle: String = "⚠️ DEDSEC // TARGET INTERCEPTED",
    val customNotificationText: String = "Signal detected matching active watch rule",
    val autoScanIntervalSeconds: Int = 8,
    val ttsEnabled: Boolean = false,
    val ttsText: String = "Alvo detectado: {{nome}}, sinal {{sinal}}",
    val ttsPitch: Float = 1.0f,
    val ttsSpeed: Float = 1.0f
)
