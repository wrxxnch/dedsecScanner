package com.example.engine

object MorseRhythmEngine {

    const val DEFAULT_DOT_MS = 100L
    const val DEFAULT_DASH_MS = 300L
    const val DEFAULT_ELEMENT_GAP_MS = 80L
    const val DEFAULT_LETTER_GAP_MS = 200L
    const val DEFAULT_WORD_GAP_MS = 450L

    data class VibrationWaveform(
        val timings: LongArray,    // Alternating: [delay, on, off, on, off...]
        val amplitudes: IntArray   // Alternating: [0, amp, 0, amp, 0...]
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as VibrationWaveform
            return timings.contentEquals(other.timings) && amplitudes.contentEquals(other.amplitudes)
        }

        override fun hashCode(): Int {
            var result = timings.contentHashCode()
            result = 31 * result + amplitudes.contentHashCode()
            return result
        }
    }

    /**
     * Parses rhythm input which can be:
     * 1) Morse code symbols: '...---.../...---...'
     * 2) Comma/space numbers (ms): '100, 200, 300, 400'
     */
    fun parseRhythm(patternStr: String, intensity: Int): VibrationWaveform {
        val trimmed = patternStr.trim()
        val amp = intensity.coerceIn(1, 255)

        // Check if numeric list e.g. "100, 200, 100"
        if (isNumericList(trimmed)) {
            return parseNumericRhythm(trimmed, amp)
        }

        // Otherwise parse as Morse pattern (. - / space)
        return parseMorseRhythm(trimmed, amp)
    }

    private fun isNumericList(str: String): Boolean {
        val cleaned = str.replace(",", " ").replace(";", " ")
        val parts = cleaned.split("\\s+".toRegex()).filter { it.isNotBlank() }
        return parts.isNotEmpty() && parts.all { it.toLongOrNull() != null }
    }

    private fun parseNumericRhythm(str: String, amplitude: Int): VibrationWaveform {
        val cleaned = str.replace(",", " ").replace(";", " ")
        val numbers = cleaned.split("\\s+".toRegex())
            .mapNotNull { it.toLongOrNull() }
            .filter { it > 0 }

        if (numbers.isEmpty()) {
            return VibrationWaveform(longArrayOf(0, 200), intArrayOf(0, amplitude))
        }

        // Standard vibration waveform starts with 0 delay (off), then on, off, on...
        val timings = ArrayList<Long>()
        val amplitudes = ArrayList<Int>()

        timings.add(0L)
        amplitudes.add(0)

        for (i in numbers.indices) {
            val duration = numbers[i].coerceAtLeast(20L)
            val isOn = (i % 2 == 0) // even index is ON, odd is OFF
            timings.add(duration)
            amplitudes.add(if (isOn) amplitude else 0)
        }

        return VibrationWaveform(timings.toLongArray(), amplitudes.toIntArray())
    }

    private fun parseMorseRhythm(morse: String, amplitude: Int): VibrationWaveform {
        if (morse.isBlank()) {
            return VibrationWaveform(longArrayOf(0, 200), intArrayOf(0, amplitude))
        }

        val timings = ArrayList<Long>()
        val amplitudes = ArrayList<Int>()

        // Initial delay
        timings.add(0L)
        amplitudes.add(0)

        for (i in morse.indices) {
            val c = morse[i]
            when (c) {
                '.' -> {
                    timings.add(DEFAULT_DOT_MS)
                    amplitudes.add(amplitude)

                    // Gap after dot if next is symbol
                    val next = morse.getOrNull(i + 1)
                    if (next == '.' || next == '-') {
                        timings.add(DEFAULT_ELEMENT_GAP_MS)
                        amplitudes.add(0)
                    }
                }
                '-' -> {
                    timings.add(DEFAULT_DASH_MS)
                    amplitudes.add(amplitude)

                    // Gap after dash if next is symbol
                    val next = morse.getOrNull(i + 1)
                    if (next == '.' || next == '-') {
                        timings.add(DEFAULT_ELEMENT_GAP_MS)
                        amplitudes.add(0)
                    }
                }
                ' ' -> {
                    timings.add(DEFAULT_LETTER_GAP_MS)
                    amplitudes.add(0)
                }
                '/', '|' -> {
                    timings.add(DEFAULT_WORD_GAP_MS)
                    amplitudes.add(0)
                }
                else -> {
                    // Ignore or treat as small pause
                }
            }
        }

        // If no active elements, fallback
        if (amplitudes.none { it > 0 }) {
            return VibrationWaveform(longArrayOf(0, 200), intArrayOf(0, amplitude))
        }

        return VibrationWaveform(timings.toLongArray(), amplitudes.toIntArray())
    }

    /**
     * Presets for quick selection
     */
    val PRESETS = listOf(
        Pair("SOS Urgent", "...---.../...---..."),
        Pair("DedSec Pulse", "... / ... / ..."),
        Pair("Heartbeat", ". - .. -"),
        Pair("Rapid Glitch", "..... ....."),
        Pair("Radar Ping", "- . - ."),
        Pair("Heavy Strike", "--- --- ---")
    )
}
