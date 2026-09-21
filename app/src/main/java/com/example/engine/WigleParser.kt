package com.example.engine

import com.example.data.model.TargetRule
import com.example.data.model.TargetType

object WigleParser {

    data class ParseResult(
        val rules: List<TargetRule>,
        val totalImported: Int,
        val formatDetected: String
    )

    /**
     * Automatically detects whether the content is a WiGLE CSV or generic space/newline separated list.
     */
    fun parseContent(content: String): ParseResult {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return ParseResult(emptyList(), 0, "Empty")
        }

        // Check for WiGLE signature (starts with WigleWifi or has MAC,SSID header)
        if (isWigleCsv(trimmed)) {
            return parseWigleCsv(trimmed)
        }

        // Otherwise parse as generic list separated by spaces, tabs, newlines
        return parsePlainList(trimmed)
    }

    /**
     * Check if text is a WiGLE export format
     */
    fun isWigleCsv(text: String): Boolean {
        val firstLines = text.lineSequence().take(5).toList()
        return firstLines.any { line ->
            line.startsWith("WigleWifi", ignoreCase = true) ||
            (line.contains("MAC", ignoreCase = true) && line.contains("SSID", ignoreCase = true)) ||
            (line.contains("BSSID", ignoreCase = true) && line.contains("SSID", ignoreCase = true))
        }
    }

    /**
     * Parses WiGLE CSV file content.
     * Extracts MAC/BSSID and SSID into TargetRules.
     */
    fun parseWigleCsv(csvContent: String): ParseResult {
        val rules = mutableListOf<TargetRule>()
        val seenPatterns = mutableSetOf<String>()

        var macColIndex = -1
        var ssidColIndex = -1
        var headerFound = false

        csvContent.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("WigleWifi", ignoreCase = true)) {
                return@forEach
            }

            // Split by comma, handling potential quotes
            val cols = splitCsvLine(line)

            if (!headerFound) {
                // Find column indices
                cols.forEachIndexed { index, header ->
                    val h = header.trim().uppercase()
                    if (h == "MAC" || h == "BSSID") {
                        macColIndex = index
                    } else if (h == "SSID" || h == "NETWORK_NAME") {
                        ssidColIndex = index
                    }
                }
                if (macColIndex != -1 || ssidColIndex != -1) {
                    headerFound = true
                    return@forEach
                }
            }

            // Data row
            if (headerFound) {
                var ssid: String? = null
                var mac: String? = null

                if (ssidColIndex in cols.indices) {
                    val s = cols[ssidColIndex].trim().trim('"', '\'')
                    if (s.isNotBlank() && s != "<hidden>" && s != "[hidden]") {
                        ssid = s
                    }
                }
                if (macColIndex in cols.indices) {
                    val m = cols[macColIndex].trim().trim('"', '\'')
                    if (m.isNotBlank() && (m.contains(":") || m.contains("-") || m.length >= 8)) {
                        mac = m
                    }
                }

                // Add SSID rule if valid
                if (!ssid.isNullOrBlank() && seenPatterns.add(ssid)) {
                    rules.add(
                        TargetRule(
                            rawPattern = ssid,
                            targetType = TargetType.WIFI_SSID
                        )
                    )
                }

                // Add MAC rule if valid
                if (!mac.isNullOrBlank() && seenPatterns.add(mac)) {
                    rules.add(
                        TargetRule(
                            rawPattern = mac,
                            targetType = TargetType.WIFI_BSSID
                        )
                    )
                }
            }
        }

        return ParseResult(rules, rules.size, "WiGLE CSV")
    }

    /**
     * Parses plain text with entries separated by spaces or newlines.
     * Supports comments starting with '#' or '//'.
     */
    fun parsePlainList(content: String): ParseResult {
        val rules = mutableListOf<TargetRule>()
        val seen = mutableSetOf<String>()

        content.lineSequence().forEach { line ->
            val strippedLine = line.trim()
            if (strippedLine.startsWith("#") || strippedLine.startsWith("//")) {
                return@forEach
            }

            // Split by whitespace
            val tokens = strippedLine.split("\\s+".toRegex()).filter { it.isNotBlank() }
            for (token in tokens) {
                val cleaned = token.trim('"', '\'', ',', ';')
                if (cleaned.isNotBlank() && seen.add(cleaned.lowercase())) {
                    val type = deduceTargetType(cleaned)
                    rules.add(
                        TargetRule(
                            rawPattern = cleaned,
                            targetType = type
                        )
                    )
                }
            }
        }

        return ParseResult(rules, rules.size, "Text List (Space/Newline)")
    }

    /**
     * Guesses the most appropriate TargetType based on format
     */
    private fun deduceTargetType(token: String): TargetType {
        val upper = token.uppercase()
        // If it looks like a MAC prefix or address (e.g. ab:cd:*, ab.cd.*, 00:11:22)
        val macLike = (upper.contains(":") || upper.contains(".") || upper.contains("-")) &&
                upper.filter { it.isLetterOrDigit() }.all { it in "0123456789ABCDEF*" }

        return if (macLike) {
            TargetType.ANY
        } else {
            TargetType.ANY
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result
    }
}
