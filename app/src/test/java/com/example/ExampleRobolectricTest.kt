package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TargetRule
import com.example.data.model.TargetType
import com.example.engine.MorseRhythmEngine
import com.example.engine.WigleParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DedSec Scanner", appName)
    }

    @Test
    fun `test wildcard matching with CLARO prefix`() {
        val rule = TargetRule(rawPattern = "CLARO-*", targetType = TargetType.WIFI_SSID)
        assertTrue(rule.matches(ssid = "CLARO-WIFI-5G"))
        assertTrue(rule.matches(ssid = "CLARO-NET-1234"))
        assertFalse(rule.matches(ssid = "VIVO-FIBRA"))
    }

    @Test
    fun `test wildcard matching with NET prefix`() {
        val rule = TargetRule(rawPattern = "NET-*", targetType = TargetType.WIFI_SSID)
        assertTrue(rule.matches(ssid = "NET-CLARO-WIFI"))
        assertTrue(rule.matches(ssid = "NET-9999"))
        assertFalse(rule.matches(ssid = "OI-FIBRA"))
    }

    @Test
    fun `test MAC address prefix wildcard matching with dot notation`() {
        val rule = TargetRule(rawPattern = "ab.cd.*", targetType = TargetType.ANY)
        // Tests matching colon formatted BSSID
        assertTrue(rule.matches(bssid = "AB:CD:12:34:56:78"))
        assertTrue(rule.matches(btAddress = "ab:cd:ff:ee:dd:cc"))
        assertFalse(rule.matches(bssid = "00:11:22:33:44:55"))
    }

    @Test
    fun `test morse rhythm parsing`() {
        val waveform = MorseRhythmEngine.parseRhythm("...---...", 255)
        assertTrue(waveform.timings.isNotEmpty())
        assertTrue(waveform.amplitudes.contains(255))
    }

    @Test
    fun `test wigle csv parsing`() {
        val sampleWigleCsv = """
            WigleWifi-1.4,appRelease=20210201,model=Test
            MAC,SSID,AuthMode,FirstSeen,Channel,RSSI,CurrentLatitude,CurrentLongitude,AltitudeMeters,AccuracyMeters,Type
            00:11:22:33:44:55,HACKER_NET,[WPA2-PSK-CCMP],2023-01-01 12:00:00,6,-65,0.0,0.0,0,0,WIFI
            AA:BB:CC:DD:EE:FF,SECRET_SSID,[WPA2-PSK-CCMP],2023-01-01 12:00:00,11,-70,0.0,0.0,0,0,WIFI
        """.trimIndent()

        val result = WigleParser.parseContent(sampleWigleCsv)
        assertEquals("WiGLE CSV", result.formatDetected)
        assertTrue(result.rules.any { it.rawPattern == "HACKER_NET" })
        assertTrue(result.rules.any { it.rawPattern == "00:11:22:33:44:55" })
    }

    @Test
    fun `test space and newline separated list parsing`() {
        val sampleList = "CLARO-* NET-*   ab.cd.* \n Vivo-*\n# comment line\n00:11:22:*"
        val result = WigleParser.parseContent(sampleList)
        assertEquals(5, result.rules.size)
        assertTrue(result.rules.any { it.rawPattern == "CLARO-*" })
        assertTrue(result.rules.any { it.rawPattern == "ab.cd.*" })
    }

    @Test
    fun `test per-rule template placeholder replacement`() {
        val rule = TargetRule(
            rawPattern = "CLARO-*",
            notificationTitle = "DETECTADO // {{nome}}",
            notificationMessage = "Rede {{nome}} ({{mac}}) sinal {{sinal}}dBm tipo {{type}} regra {{rule}}"
        )

        val title = rule.formatTemplate(
            template = rule.notificationTitle,
            signalName = "CLARO-NET-5G",
            signalMac = "AA:BB:CC:11:22:33",
            signalRssi = -65,
            signalType = "WIFI",
            timestamp = 1700000000000L
        )
        val body = rule.formatTemplate(
            template = rule.notificationMessage,
            signalName = "CLARO-NET-5G",
            signalMac = "AA:BB:CC:11:22:33",
            signalRssi = -65,
            signalType = "WIFI",
            timestamp = 1700000000000L
        )

        assertEquals("DETECTADO // CLARO-NET-5G", title)
        assertEquals("Rede CLARO-NET-5G (AA:BB:CC:11:22:33) sinal -65 dBm tipo WIFI regra CLARO-*", body)
    }

    @Test
    fun `test individual rule alert modes and morse patterns`() {
        val ruleVib = TargetRule(
            rawPattern = "TARGET-VIB",
            alertMode = com.example.data.model.AlertMode.VIBRATE_ONLY,
            morsePattern = "...---..."
        )
        val ruleSound = TargetRule(
            rawPattern = "TARGET-SND",
            alertMode = com.example.data.model.AlertMode.SOUND_ONLY,
            morsePattern = ". - .. -"
        )
        val ruleBoth = TargetRule(
            rawPattern = "TARGET-BOTH",
            alertMode = com.example.data.model.AlertMode.VIBRATE_AND_SOUND,
            morsePattern = "... / ..."
        )

        assertEquals(com.example.data.model.AlertMode.VIBRATE_ONLY, ruleVib.alertMode)
        assertEquals(com.example.data.model.AlertMode.SOUND_ONLY, ruleSound.alertMode)
        assertEquals(com.example.data.model.AlertMode.VIBRATE_AND_SOUND, ruleBoth.alertMode)

        val waveformVib = MorseRhythmEngine.parseRhythm(ruleVib.morsePattern, 255)
        val waveformBoth = MorseRhythmEngine.parseRhythm(ruleBoth.morsePattern, 200)

        assertTrue(waveformVib.timings.isNotEmpty())
        assertTrue(waveformBoth.timings.isNotEmpty())
    }
}
