package com.example.data.model

import java.util.Locale
import java.util.regex.Pattern

enum class TargetType {
    ANY,
    WIFI_SSID,
    WIFI_BSSID,
    BLUETOOTH_NAME,
    BLUETOOTH_MAC
}

data class TargetRule(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawPattern: String,
    val enabled: Boolean = true,
    val targetType: TargetType = TargetType.ANY,
    val addedAt: Long = System.currentTimeMillis(),
    val hitCount: Int = 0,
    val lastSeenAt: Long? = null,
    val alertMode: AlertMode = AlertMode.VIBRATE_AND_SOUND,
    val morsePattern: String = "...---...",
    val vibrationIntensity: Int = 255,
    val notificationTitle: String = "⚠️ ALVO // {{nome}}",
    val notificationMessage: String = "[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}} | {{time}}",
    val soundSourceType: SoundSourceType = SoundSourceType.BUILT_IN_CYBER_SYNTH,
    val customAudioUri: String? = null,
    val customAudioName: String? = null,
    val soundVolume: Float = 1.0f,
    val ttsEnabled: Boolean = false,
    val ttsText: String = "Alvo detectado: {{nome}}, sinal {{sinal}}",
    val ttsPitch: Float = 1.0f,
    val ttsSpeed: Float = 1.0f
) {
    /**
     * Formats notification title or body replacing placeholders:
     * {{nome}}, {{sinal}}, {{mac}}, {{rule}}, {{time}}, {{type}}
     */
    fun formatTemplate(template: String, signalName: String, signalMac: String, signalRssi: Int, signalType: String, timestamp: Long): String {
        val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        val nameStr = signalName.ifBlank { "<HIDDEN>" }
        val signalStr = "$signalRssi dBm"
        val ruleStr = rawPattern

        var result = template
        result = result.replace("{{nome}}", nameStr, ignoreCase = true)
        result = result.replace("{{name}}", nameStr, ignoreCase = true)
        // Handle both {{sinal}}dBm and {{sinal}} cleanly
        result = result.replace("{{sinal}} dBm", signalStr, ignoreCase = true)
        result = result.replace("{{sinal}}dBm", signalStr, ignoreCase = true)
        result = result.replace("{{sinal}}", signalStr, ignoreCase = true)
        result = result.replace("{{signal}} dBm", signalStr, ignoreCase = true)
        result = result.replace("{{signal}}dBm", signalStr, ignoreCase = true)
        result = result.replace("{{signal}}", signalStr, ignoreCase = true)
        result = result.replace("{{mac}}", signalMac, ignoreCase = true)
        result = result.replace("{{rule}}", ruleStr, ignoreCase = true)
        result = result.replace("{{rule}", ruleStr, ignoreCase = true)
        result = result.replace("{{regra}}", ruleStr, ignoreCase = true)
        result = result.replace("{{time}}", timeStr, ignoreCase = true)
        result = result.replace("{{hora}}", timeStr, ignoreCase = true)
        result = result.replace("{{type}}", signalType, ignoreCase = true)
        result = result.replace("{{tipo}}", signalType, ignoreCase = true)
        return result
    }
    /**
     * Checks if this rule matches either WiFi SSID/BSSID or Bluetooth Name/Address.
     */
    fun matches(
        ssid: String? = null,
        bssid: String? = null,
        btName: String? = null,
        btAddress: String? = null
    ): Boolean {
        if (!enabled) return false
        val trimmed = rawPattern.trim()
        if (trimmed.isEmpty()) return false

        // Check based on TargetType or ANY
        return when (targetType) {
            TargetType.WIFI_SSID -> ssid != null && matchesString(trimmed, ssid, isMac = false)
            TargetType.WIFI_BSSID -> bssid != null && matchesMac(trimmed, bssid)
            TargetType.BLUETOOTH_NAME -> btName != null && matchesString(trimmed, btName, isMac = false)
            TargetType.BLUETOOTH_MAC -> btAddress != null && matchesMac(trimmed, btAddress)
            TargetType.ANY -> {
                (ssid != null && matchesString(trimmed, ssid, isMac = false)) ||
                (bssid != null && matchesMac(trimmed, bssid)) ||
                (btName != null && matchesString(trimmed, btName, isMac = false)) ||
                (btAddress != null && matchesMac(trimmed, btAddress))
            }
        }
    }

    companion object {
        /**
         * Match string against wildcard pattern (* and ? supported, case-insensitive).
         */
        fun matchesString(patternStr: String, candidate: String, isMac: Boolean): Boolean {
            if (candidate.isBlank()) return false
            val pat = patternStr.trim()
            if (pat == "*" || pat == candidate) return true

            // Build regex from pattern with wildcards
            val regex = wildcardToRegex(pat, isMac = isMac)
            return try {
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(candidate).matches()
            } catch (e: Exception) {
                candidate.contains(pat, ignoreCase = true)
            }
        }

        /**
         * Matches MAC addresses supporting forms like:
         * **:**:**:**:94:** -> matches any MAC where 5th octet is 94
         * *:*:*:*:94:* -> matches any MAC where 5th octet is 94
         * 00:11:22:*:*:* -> matches vendor prefix
         * **:**:**:**:**:94 -> matches ending with 94
         * 94:**:**:**:**:** -> matches starting with 94
         * ab.cd.* -> matches AB:CD:12:34:56:78
         * ab:cd:* -> matches AB:CD:12:34:56:78
         * ab-cd-* -> matches AB:CD:12:34:56:78
         * 00:11:22:33:44:55 -> exact match
         */
        fun matchesMac(patternStr: String, macCandidate: String): Boolean {
            if (macCandidate.isBlank()) return false
            val cleanCandidate = macCandidate.trim().uppercase(Locale.ROOT)
            val cleanPattern = patternStr.trim().uppercase(Locale.ROOT)

            // Direct match or wildcard for everything
            if (cleanPattern == "*" || cleanPattern == "**") {
                return true
            }

            // Normalize separators: convert . and - to :
            val normCandidate = cleanCandidate.replace('.', ':').replace('-', ':')
            val normPattern = cleanPattern.replace('.', ':').replace('-', ':')

            if (normPattern == normCandidate) {
                return true
            }

            // Extract 6 individual candidate octets
            val candidateOctets = extractOctets(normCandidate)

            // If the pattern has colon-separated octets and candidate is a valid 6-octet MAC
            if (normPattern.contains(':') && candidateOctets != null && candidateOctets.size == 6) {
                val patternOctets = normPattern.split(':').map { it.trim() }

                // Case 1: Exact 6 octets in pattern (e.g., **:**:**:**:94:** or *:*:*:*:94:* or 00:11:22:*:*:*)
                if (patternOctets.size == 6) {
                    var allMatch = true
                    for (i in 0 until 6) {
                        if (!matchesOctet(patternOctets[i], candidateOctets[i])) {
                            allMatch = false
                            break
                        }
                    }
                    if (allMatch) return true
                } else if (patternOctets.size in 1..5) {
                    // Prefix matching up to patternOctets.size (e.g. 00:11:22:* or 00:11:22)
                    var prefixMatch = true
                    for (i in patternOctets.indices) {
                        val pOctet = patternOctets[i]
                        if (i == patternOctets.lastIndex && (pOctet == "*" || pOctet == "**" || pOctet.isEmpty())) {
                            break
                        }
                        if (!matchesOctet(pOctet, candidateOctets[i])) {
                            prefixMatch = false
                            break
                        }
                    }
                    if (prefixMatch) return true
                }
            }

            // Fallback: delimiter-safe regex match
            if (matchesString(normPattern, normCandidate, isMac = true)) {
                return true
            }

            // Also test stripped hex format if pattern does not contain colons
            if (!normPattern.contains(':')) {
                val strippedCandidate = normCandidate.replace(":", "")
                val strippedPattern = normPattern.replace(":", "")
                if (matchesString(strippedPattern, strippedCandidate, isMac = false)) {
                    return true
                }
            }

            return false
        }

        private fun extractOctets(mac: String): List<String>? {
            val parts = mac.split(':').filter { it.isNotEmpty() }
            if (parts.size == 6) {
                return parts.map { it.padStart(2, '0').uppercase(Locale.ROOT) }
            }
            // In case of continuous 12 hex chars without separators
            val raw = mac.replace(":", "").replace("-", "").replace(".", "")
            if (raw.length == 12 && raw.all { it.isDigit() || (it in 'A'..'F') || (it in 'a'..'f') }) {
                return raw.chunked(2).map { it.uppercase(Locale.ROOT) }
            }
            return null
        }

        private fun matchesOctet(patternOctet: String, candidateOctet: String): Boolean {
            val p = patternOctet.trim().uppercase(Locale.ROOT)
            val c = candidateOctet.trim().uppercase(Locale.ROOT)
            if (p == "*" || p == "**" || p == "??" || p.isEmpty()) return true
            if (p == c) return true

            // Wildcard within the 2-char octet, e.g. 9* or *4 or 9? or ?4
            val regex = wildcardToRegex(p, isMac = true)
            return try {
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(c).matches()
            } catch (e: Exception) {
                false
            }
        }

        private fun wildcardToRegex(pattern: String, isMac: Boolean = false): String {
            val sb = StringBuilder("^")
            for (ch in pattern) {
                when (ch) {
                    '*' -> if (isMac && pattern.contains(':')) sb.append("[^:]*") else sb.append(".*")
                    '?' -> if (isMac && pattern.contains(':')) sb.append("[^:]") else sb.append(".")
                    '(', ')', '[', ']', '{', '}', '^', '$', '+', '.', '\\' -> {
                        sb.append('\\').append(ch)
                    }
                    else -> sb.append(ch)
                }
            }
            sb.append("$")
            return sb.toString()
        }
    }
}
