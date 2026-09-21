package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.AlertConfig
import com.example.data.model.AlertMode
import com.example.data.model.RecentSound
import com.example.data.model.SoundSourceType
import com.example.data.model.TargetRule
import com.example.data.model.TargetType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class TargetRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("dedsec_signal_prefs", Context.MODE_PRIVATE)

    fun loadRules(): List<TargetRule> {
        val jsonStr = prefs.getString(KEY_RULES, null) ?: return defaultRules()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TargetRule>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TargetRule(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        rawPattern = obj.getString("rawPattern"),
                        enabled = obj.optBoolean("enabled", true),
                        targetType = try {
                            TargetType.valueOf(obj.optString("targetType", "ANY"))
                        } catch (e: Exception) {
                            TargetType.ANY
                        },
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                        hitCount = obj.optInt("hitCount", 0),
                        alertMode = try {
                            AlertMode.valueOf(obj.optString("alertMode", AlertMode.VIBRATE_AND_SOUND.name))
                        } catch (e: Exception) {
                            AlertMode.VIBRATE_AND_SOUND
                        },
                        morsePattern = obj.optString("morsePattern", "...---..."),
                        vibrationIntensity = obj.optInt("vibrationIntensity", 255),
                        notificationTitle = obj.optString("notificationTitle", "⚠️ ALVO // {{nome}}"),
                        notificationMessage = obj.optString(
                            "notificationMessage",
                            "[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}} | {{time}}"
                        ),
                        soundSourceType = try {
                            SoundSourceType.valueOf(obj.optString("soundSourceType", SoundSourceType.BUILT_IN_CYBER_SYNTH.name))
                        } catch (e: Exception) {
                            SoundSourceType.BUILT_IN_CYBER_SYNTH
                        },
                        customAudioUri = if (obj.has("customAudioUri") && !obj.isNull("customAudioUri")) obj.getString("customAudioUri") else null,
                        customAudioName = if (obj.has("customAudioName") && !obj.isNull("customAudioName")) obj.getString("customAudioName") else null,
                        soundVolume = obj.optDouble("soundVolume", 1.0).toFloat(),
                        ttsEnabled = obj.optBoolean("ttsEnabled", false),
                        ttsText = obj.optString("ttsText", "Alvo detectado: {{nome}}, sinal {{sinal}}"),
                        ttsPitch = obj.optDouble("ttsPitch", 1.0).toFloat(),
                        ttsSpeed = obj.optDouble("ttsSpeed", 1.0).toFloat()
                    )
                )
            }
            if (list.isEmpty()) defaultRules() else list
        } catch (e: Exception) {
            defaultRules()
        }
    }

    fun saveRules(rules: List<TargetRule>) {
        val array = JSONArray()
        for (rule in rules) {
            val obj = JSONObject().apply {
                put("id", rule.id)
                put("rawPattern", rule.rawPattern)
                put("enabled", rule.enabled)
                put("targetType", rule.targetType.name)
                put("addedAt", rule.addedAt)
                put("hitCount", rule.hitCount)
                put("alertMode", rule.alertMode.name)
                put("morsePattern", rule.morsePattern)
                put("vibrationIntensity", rule.vibrationIntensity)
                put("notificationTitle", rule.notificationTitle)
                put("notificationMessage", rule.notificationMessage)
                put("soundSourceType", rule.soundSourceType.name)
                put("customAudioUri", rule.customAudioUri)
                put("customAudioName", rule.customAudioName)
                put("soundVolume", rule.soundVolume.toDouble())
                put("ttsEnabled", rule.ttsEnabled)
                put("ttsText", rule.ttsText)
                put("ttsPitch", rule.ttsPitch.toDouble())
                put("ttsSpeed", rule.ttsSpeed.toDouble())
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_RULES, array.toString()).apply()
    }

    fun loadConfig(): AlertConfig {
        return AlertConfig(
            alertMode = try {
                AlertMode.valueOf(prefs.getString(KEY_ALERT_MODE, AlertMode.VIBRATE_AND_SOUND.name)!!)
            } catch (e: Exception) {
                AlertMode.VIBRATE_AND_SOUND
            },
            vibrationIntensity = prefs.getInt(KEY_VIB_INTENSITY, 255),
            rhythmPattern = prefs.getString(KEY_RHYTHM_PATTERN, "...---.../...---...") ?: "...---.../...---...",
            soundSourceType = try {
                SoundSourceType.valueOf(prefs.getString(KEY_SOUND_TYPE, SoundSourceType.BUILT_IN_CYBER_SYNTH.name)!!)
            } catch (e: Exception) {
                SoundSourceType.BUILT_IN_CYBER_SYNTH
            },
            customAudioUri = prefs.getString(KEY_AUDIO_URI, null),
            customAudioName = prefs.getString(KEY_AUDIO_NAME, null),
            soundVolume = prefs.getFloat(KEY_SOUND_VOL, 0.9f),
            cooldownSeconds = prefs.getInt(KEY_COOLDOWN, 5),
            customNotificationTitle = prefs.getString(KEY_NOTIF_TITLE, "⚠️ DEDSEC // TARGET INTERCEPTED") ?: "⚠️ DEDSEC // TARGET INTERCEPTED",
            customNotificationText = prefs.getString(KEY_NOTIF_TEXT, "Signal detected matching active watch rule") ?: "Signal detected matching active watch rule",
            autoScanIntervalSeconds = prefs.getInt(KEY_SCAN_INTERVAL, 8),
            ttsEnabled = prefs.getBoolean(KEY_TTS_ENABLED, false),
            ttsText = prefs.getString(KEY_TTS_TEXT, "Alvo detectado: {{nome}}, sinal {{sinal}}") ?: "Alvo detectado: {{nome}}, sinal {{sinal}}",
            ttsPitch = prefs.getFloat(KEY_TTS_PITCH, 1.0f),
            ttsSpeed = prefs.getFloat(KEY_TTS_SPEED, 1.0f)
        )
    }

    fun saveConfig(config: AlertConfig) {
        prefs.edit()
            .putString(KEY_ALERT_MODE, config.alertMode.name)
            .putInt(KEY_VIB_INTENSITY, config.vibrationIntensity)
            .putString(KEY_RHYTHM_PATTERN, config.rhythmPattern)
            .putString(KEY_SOUND_TYPE, config.soundSourceType.name)
            .putString(KEY_AUDIO_URI, config.customAudioUri)
            .putString(KEY_AUDIO_NAME, config.customAudioName)
            .putFloat(KEY_SOUND_VOL, config.soundVolume)
            .putInt(KEY_COOLDOWN, config.cooldownSeconds)
            .putString(KEY_NOTIF_TITLE, config.customNotificationTitle)
            .putString(KEY_NOTIF_TEXT, config.customNotificationText)
            .putInt(KEY_SCAN_INTERVAL, config.autoScanIntervalSeconds)
            .putBoolean(KEY_TTS_ENABLED, config.ttsEnabled)
            .putString(KEY_TTS_TEXT, config.ttsText)
            .putFloat(KEY_TTS_PITCH, config.ttsPitch)
            .putFloat(KEY_TTS_SPEED, config.ttsSpeed)
            .apply()
    }

    // ==========================================
    // RECENT SOUNDS MANAGEMENT & LOCAL STORAGE
    // ==========================================

    fun loadRecentSounds(): List<RecentSound> {
        val jsonStr = prefs.getString(KEY_RECENT_SOUNDS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<RecentSound>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RecentSound(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.getString("name"),
                        uriString = obj.getString("uriString"),
                        dateAdded = obj.optLong("dateAdded", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.dateAdded }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveRecentSounds(sounds: List<RecentSound>) {
        val array = JSONArray()
        for (sound in sounds.take(20)) { // Keep top 20 recent sounds
            val obj = JSONObject().apply {
                put("id", sound.id)
                put("name", sound.name)
                put("uriString", sound.uriString)
                put("dateAdded", sound.dateAdded)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_RECENT_SOUNDS, array.toString()).apply()
    }

    fun addRecentSound(name: String, uriString: String): RecentSound {
        val existing = loadRecentSounds().toMutableList()
        // Check if already in list with same URI or name
        val foundIndex = existing.indexOfFirst { it.uriString == uriString || it.name.equals(name, ignoreCase = true) }
        val newEntry = RecentSound(
            id = if (foundIndex >= 0) existing[foundIndex].id else java.util.UUID.randomUUID().toString(),
            name = name,
            uriString = uriString,
            dateAdded = System.currentTimeMillis()
        )
        if (foundIndex >= 0) {
            existing[foundIndex] = newEntry
        } else {
            existing.add(0, newEntry)
        }
        saveRecentSounds(existing)
        return newEntry
    }

    fun removeRecentSound(id: String) {
        val current = loadRecentSounds().filterNot { it.id == id }
        saveRecentSounds(current)
    }

    /**
     * Copies selected audio URI to local app internal storage (files/custom_sounds/)
     * so it never expires or loses ContentResolver permissions across restarts.
     * Returns Pair(persistentUriString, displayName)
     */
    fun importAudioFileToInternalStorage(uri: Uri): Pair<String, String> {
        var displayName = "audio_${System.currentTimeMillis()}"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        val name = cursor.getString(nameIdx)
                        if (!name.isNullOrBlank()) {
                            displayName = name
                        }
                    }
                }
            }
        } catch (e: Exception) {
            displayName = uri.lastPathSegment ?: displayName
        }

        val soundsDir = File(context.filesDir, "custom_sounds").apply { mkdirs() }
        val sanitized = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val destinationFile = File(soundsDir, "${System.currentTimeMillis()}_$sanitized")

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // If copy failed, fallback to raw uri string
            return Pair(uri.toString(), displayName)
        }

        val persistentUri = Uri.fromFile(destinationFile).toString()
        addRecentSound(displayName, persistentUri)
        return Pair(persistentUri, displayName)
    }

    private fun defaultRules(): List<TargetRule> {
        return listOf(
            TargetRule(rawPattern = "CLARO-*", targetType = TargetType.WIFI_SSID),
            TargetRule(rawPattern = "NET-*", targetType = TargetType.WIFI_SSID),
            TargetRule(rawPattern = "ab.cd.*", targetType = TargetType.ANY),
            TargetRule(rawPattern = "Vivo-*", targetType = TargetType.WIFI_SSID),
            TargetRule(rawPattern = "00:11:22:*", targetType = TargetType.ANY)
        )
    }

    companion object {
        private const val KEY_RULES = "saved_rules"
        private const val KEY_ALERT_MODE = "alert_mode"
        private const val KEY_VIB_INTENSITY = "vib_intensity"
        private const val KEY_RHYTHM_PATTERN = "rhythm_pattern"
        private const val KEY_SOUND_TYPE = "sound_type"
        private const val KEY_AUDIO_URI = "audio_uri"
        private const val KEY_AUDIO_NAME = "audio_name"
        private const val KEY_SOUND_VOL = "sound_volume"
        private const val KEY_COOLDOWN = "cooldown"
        private const val KEY_NOTIF_TITLE = "notif_title"
        private const val KEY_NOTIF_TEXT = "notif_text"
        private const val KEY_SCAN_INTERVAL = "scan_interval"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_TTS_TEXT = "tts_text"
        private const val KEY_TTS_PITCH = "tts_pitch"
        private const val KEY_TTS_SPEED = "tts_speed"
        private const val KEY_RECENT_SOUNDS = "recent_sounds"
    }
}
