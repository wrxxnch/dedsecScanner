package com.example.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.AlertConfig
import com.example.data.model.AlertMode
import com.example.data.model.DetectedSignal
import com.example.data.model.SoundSourceType
import com.example.data.model.TargetRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sin

class AlertEngine(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var mediaPlayer: MediaPlayer? = null
    private var lastAlertTime = 0L
    private val notificationIdCounter = AtomicInteger(2000)

    private var ttsEngine: TextToSpeech? = null
    private var isTtsReady = false

    init {
        createNotificationChannel()
        initTts()
    }

    private fun initTts() {
        try {
            ttsEngine = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    try {
                        val loc = Locale.getDefault()
                        val langResult = ttsEngine?.setLanguage(loc)
                        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            ttsEngine?.setLanguage(Locale.US)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Locale setup exception in TTS", e)
                    }
                    Log.d(TAG, "TTS Engine Initialized successfully")
                } else {
                    Log.w(TAG, "TTS Initialization failed with status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TextToSpeech", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DedSec Signal Interceptor",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when matched WiFi/Bluetooth networks are intercepted"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                setSound(null, null) // Sound is synthesized / played by our engine
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Trigger alert for a detected target signal, applying either rule-specific
     * or global alert mode, morse rhythm, notification template, and sound.
     */
    fun triggerAlert(
        signal: DetectedSignal,
        config: AlertConfig,
        matchedRule: TargetRule? = null,
        force: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val cooldownMs = config.cooldownSeconds * 1000L
        if (!force && (now - lastAlertTime < cooldownMs)) {
            Log.d(TAG, "Alert suppressed due to cooldown: ${signal.name}")
            return
        }
        lastAlertTime = now

        // Determine effective AlertMode and Rhythm for this alert
        val effectiveMode = matchedRule?.alertMode ?: config.alertMode
        val effectivePattern = if (matchedRule != null && matchedRule.morsePattern.isNotBlank()) {
            matchedRule.morsePattern
        } else {
            config.rhythmPattern
        }
        val effectiveIntensity = matchedRule?.vibrationIntensity ?: config.vibrationIntensity

        // Trigger vibration if requested
        if (effectiveMode == AlertMode.VIBRATE_ONLY || effectiveMode == AlertMode.VIBRATE_AND_SOUND) {
            vibrate(effectivePattern, effectiveIntensity)
        }

        // Trigger sound if requested
        if (effectiveMode == AlertMode.SOUND_ONLY || effectiveMode == AlertMode.VIBRATE_AND_SOUND) {
            playSound(config, matchedRule)
        }

        // Trigger Text-to-Voice (TTS) if enabled on rule or globally
        val isTtsEnabled = if (matchedRule != null) matchedRule.ttsEnabled else config.ttsEnabled
        if (isTtsEnabled) {
            val ttsTemplate = if (matchedRule != null && matchedRule.ttsEnabled && matchedRule.ttsText.isNotBlank()) {
                matchedRule.ttsText
            } else {
                config.ttsText
            }
            val pitch = matchedRule?.ttsPitch ?: config.ttsPitch
            val speed = matchedRule?.ttsSpeed ?: config.ttsSpeed
            val spokenText = if (matchedRule != null) {
                matchedRule.formatTemplate(
                    template = ttsTemplate,
                    signalName = signal.name,
                    signalMac = signal.address,
                    signalRssi = signal.rssi,
                    signalType = signal.type.name,
                    timestamp = now
                )
            } else {
                TargetRule(rawPattern = signal.matchedRule ?: "TARGET").formatTemplate(
                    template = ttsTemplate,
                    signalName = signal.name,
                    signalMac = signal.address,
                    signalRssi = signal.rssi,
                    signalType = signal.type.name,
                    timestamp = now
                )
            }
            speakText(spokenText, pitch, speed)
        }

        // Trigger individual notification
        showNotification(signal, config, matchedRule, effectiveMode)
    }

    /**
     * Test current rhythm and sound configuration immediately
     */
    fun testAlert(config: AlertConfig) {
        triggerAlert(
            signal = DetectedSignal(
                id = "test_signal",
                type = com.example.data.model.SignalType.WIFI,
                name = "DEDSEC_TEST_NODE",
                address = "DE:D5:EC:00:13:37",
                rssi = -42,
                matchedRule = config.rhythmPattern
            ),
            config = config,
            force = true
        )
    }

    /**
     * Test individual rule alert settings (morse, sound, and notification template)
     */
    fun testRuleAlert(rule: TargetRule, config: AlertConfig) {
        triggerAlert(
            signal = DetectedSignal(
                id = "test_rule_${rule.id}",
                type = when (rule.targetType) {
                    com.example.data.model.TargetType.BLUETOOTH_NAME,
                    com.example.data.model.TargetType.BLUETOOTH_MAC -> com.example.data.model.SignalType.BLUETOOTH
                    else -> com.example.data.model.SignalType.WIFI
                },
                name = if (rule.rawPattern.contains("*")) rule.rawPattern.replace("*", "NODE") else rule.rawPattern,
                address = "DE:D5:EC:13:37:42",
                rssi = -52,
                matchedRule = rule.rawPattern
            ),
            config = config,
            matchedRule = rule,
            force = true
        )
    }

    /**
     * Robust vibration engine with USAGE_ALARM attribute prioritization,
     * ensuring vibration triggers reliably even with touch haptics disabled.
     */
    fun vibrate(patternStr: String, intensity: Int) {
        try {
            val waveform = MorseRhythmEngine.parseRhythm(patternStr, intensity)
            val timings = waveform.timings
            val amplitudes = waveform.amplitudes

            // Obtain system Vibrator
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
                    ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator == null || !vibrator.hasVibrator()) {
                Log.w(TAG, "Device has no vibrator available")
                return
            }

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var vibrationSuccessful = false

                // Attempt 1: Custom amplitude waveform if supported
                if (intensity in 1..254 && vibrator.hasAmplitudeControl()) {
                    try {
                        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val vibAttributes = VibrationAttributes.Builder()
                                .setUsage(VibrationAttributes.USAGE_ALARM)
                                .build()
                            vibrator.vibrate(effect, vibAttributes)
                        } else {
                            vibrator.vibrate(effect, audioAttributes)
                        }
                        vibrationSuccessful = true
                    } catch (e: Exception) {
                        Log.w(TAG, "Amplitude waveform vibration failed, falling back to binary waveform", e)
                    }
                }

                // Attempt 2: High-compatibility binary ON/OFF waveform (full motor power)
                if (!vibrationSuccessful) {
                    try {
                        val effect = VibrationEffect.createWaveform(timings, -1)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val vibAttributes = VibrationAttributes.Builder()
                                .setUsage(VibrationAttributes.USAGE_ALARM)
                                .build()
                            vibrator.vibrate(effect, vibAttributes)
                        } else {
                            vibrator.vibrate(effect, audioAttributes)
                        }
                        vibrationSuccessful = true
                    } catch (e: Exception) {
                        Log.w(TAG, "Binary waveform vibration failed, falling back to one-shot pulse", e)
                    }
                }

                // Attempt 3: Single high-priority pulse fallback
                if (!vibrationSuccessful) {
                    try {
                        val effect = VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE)
                        vibrator.vibrate(effect, audioAttributes)
                    } catch (e: Exception) {
                        Log.e(TAG, "One-shot fallback vibration failed", e)
                    }
                }
            } else {
                // Legacy Android API < 26
                @Suppress("DEPRECATION")
                try {
                    vibrator.vibrate(timings, -1, audioAttributes)
                } catch (e: Exception) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute vibration in AlertEngine", e)
        }
    }

    /**
     * Stops any ongoing vibration
     */
    fun stopVibration() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel vibration", e)
        }
    }

    /**
     * Plays sound based on per-rule configuration or global config fallback
     */
    fun playSound(config: AlertConfig, matchedRule: TargetRule? = null) {
        val soundType = matchedRule?.soundSourceType ?: config.soundSourceType
        val customUri = if (matchedRule != null && matchedRule.soundSourceType == SoundSourceType.CUSTOM_AUDIO_FILE && !matchedRule.customAudioUri.isNullOrBlank()) {
            matchedRule.customAudioUri
        } else {
            config.customAudioUri
        }
        val volume = matchedRule?.soundVolume ?: config.soundVolume

        if (soundType == SoundSourceType.CUSTOM_AUDIO_FILE && !customUri.isNullOrBlank()) {
            playCustomAudio(customUri, volume)
        } else {
            playDedSecCyberSynth(volume)
        }
    }

    /**
     * Preview any audio file (e.g. from recent sounds list or file picker)
     */
    fun previewCustomAudio(uriString: String, volume: Float = 1.0f) {
        playCustomAudio(uriString, volume)
    }

    /**
     * Text-to-Speech synthesizer with pitch and speed/rate control
     */
    fun speakText(
        text: String,
        pitch: Float = 1.0f,
        speed: Float = 1.0f
    ) {
        if (text.isBlank()) return
        if (!isTtsReady || ttsEngine == null) {
            initTts()
        }
        CoroutineScope(Dispatchers.Main).launch {
            try {
                ttsEngine?.apply {
                    setPitch(pitch.coerceIn(0.5f, 2.0f))
                    setSpeechRate(speed.coerceIn(0.5f, 2.0f))
                    val params = Bundle().apply {
                        putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
                    }
                    speak(text, TextToSpeech.QUEUE_FLUSH, params, "alert_tts_${System.currentTimeMillis()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute speakText", e)
            }
        }
    }

    fun stopTts() {
        try {
            ttsEngine?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop TTS", e)
        }
    }

    private fun playCustomAudio(uriString: String, volume: Float) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setDataSource(context, Uri.parse(uriString))
                    setVolume(volume, volume)
                    prepare()
                    start()
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play custom audio file, falling back to cyber synth", e)
                playDedSecCyberSynth(volume)
            }
        }
    }

    /**
     * Synthesizes an authentic DedSec cyberpunk alert tone sequence:
     * High cyber chirp (880Hz -> 1320Hz) followed by pulse beep (440Hz -> 880Hz)
     */
    fun playDedSecCyberSynth(volume: Float) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val totalDurationMs = 350
                val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt()
                val audioBuffer = ShortArray(totalSamples)

                val vol = volume.coerceIn(0.1f, 1.0f)

                // Generate two-stage cyber pulse tone
                for (i in 0 until totalSamples) {
                    val time = i.toDouble() / sampleRate
                    val progress = i.toDouble() / totalSamples

                    // Frequency sweep: start at 700 Hz, sweep up to 1500 Hz
                    val freq = if (progress < 0.5) {
                        700.0 + (progress * 2.0) * 800.0
                    } else {
                        1100.0 + sin(progress * 60.0) * 300.0
                    }

                    val wave = sin(2.0 * Math.PI * freq * time) + 0.3 * sin(4.0 * Math.PI * freq * time)
                    val envelope = if (progress > 0.8) (1.0 - progress) / 0.2 else 1.0
                    val sample = (wave * Short.MAX_VALUE * vol * 0.6 * envelope).toInt()
                    audioBuffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, audioBuffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(audioBuffer, 0, audioBuffer.size)
                audioTrack.play()

                // Release after playback
                kotlinx.coroutines.delay(totalDurationMs.toLong() + 100L)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.e(TAG, "AudioTrack synth error", e)
            }
        }
    }

    /**
     * Posts an individual notification for each intercepted target signal
     * formatted with rule-specific or global template placeholders:
     * {{nome}}, {{sinal}}, {{mac}}, {{rule}}, {{time}}, {{type}}
     */
    private fun showNotification(
        signal: DetectedSignal,
        config: AlertConfig,
        matchedRule: TargetRule?,
        alertMode: AlertMode
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            signal.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rawTitle = if (matchedRule != null && matchedRule.notificationTitle.isNotBlank()) {
            matchedRule.notificationTitle
        } else {
            config.customNotificationTitle.ifBlank { "⚠️ ALVO // {{nome}}" }
        }

        val rawMessage = if (matchedRule != null && matchedRule.notificationMessage.isNotBlank()) {
            matchedRule.notificationMessage
        } else {
            config.customNotificationText.ifBlank {
                "[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}} | {{time}}"
            }
        }

        val title = if (matchedRule != null) {
            matchedRule.formatTemplate(
                template = rawTitle,
                signalName = signal.name,
                signalMac = signal.address,
                signalRssi = signal.rssi,
                signalType = signal.type.name,
                timestamp = signal.timestamp
            )
        } else {
            formatFallbackTemplate(rawTitle, signal)
        }

        val body = if (matchedRule != null) {
            matchedRule.formatTemplate(
                template = rawMessage,
                signalName = signal.name,
                signalMac = signal.address,
                signalRssi = signal.rssi,
                signalType = signal.type.name,
                timestamp = signal.timestamp
            )
        } else {
            formatFallbackTemplate(rawMessage, signal)
        }

        val notifBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        // Reinforce vibration on the notification itself if vibration is enabled
        if (alertMode == AlertMode.VIBRATE_ONLY || alertMode == AlertMode.VIBRATE_AND_SOUND) {
            notifBuilder.setVibrate(longArrayOf(0, 250, 100, 250))
        }

        val uniqueNotifId = notificationIdCounter.incrementAndGet()
        try {
            notificationManager.notify(uniqueNotifId, notifBuilder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing", e)
        }
    }

    private fun formatFallbackTemplate(template: String, signal: DetectedSignal): String {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(signal.timestamp))
        val nameStr = signal.name.ifBlank { "<HIDDEN>" }
        val signalStr = "${signal.rssi} dBm"
        val ruleStr = signal.matchedRule ?: ""
        var result = template
        result = result.replace("{{nome}}", nameStr, ignoreCase = true)
        result = result.replace("{{sinal}}", signalStr, ignoreCase = true)
        result = result.replace("{{mac}}", signal.address, ignoreCase = true)
        result = result.replace("{{rule}}", ruleStr, ignoreCase = true)
        result = result.replace("{{rule}", ruleStr, ignoreCase = true)
        result = result.replace("{{regra}}", ruleStr, ignoreCase = true)
        result = result.replace("{{time}}", timeStr, ignoreCase = true)
        result = result.replace("{{hora}}", timeStr, ignoreCase = true)
        result = result.replace("{{type}}", signal.type.name, ignoreCase = true)
        result = result.replace("{{tipo}}", signal.type.name, ignoreCase = true)
        return result
    }

    companion object {
        private const val TAG = "DedSecAlertEngine"
        const val CHANNEL_ID = "dedsec_signals_channel"
    }
}
