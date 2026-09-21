package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertConfig
import com.example.data.model.AlertMode
import com.example.data.model.RecentSound
import com.example.data.model.SoundSourceType
import com.example.engine.MorseRhythmEngine
import com.example.ui.theme.*

@Composable
fun AlertConfigView(
    config: AlertConfig,
    recentSounds: List<RecentSound> = emptyList(),
    isTesting: Boolean,
    onUpdateConfig: (AlertConfig) -> Unit,
    onTestAlert: () -> Unit,
    onPickAudioFile: () -> Unit,
    onSelectRecentSound: (RecentSound) -> Unit = {},
    onDeleteRecentSound: (String) -> Unit = {},
    onPreviewSound: (String) -> Unit = {},
    onTestTts: (text: String, pitch: Float, speed: Float) -> Unit = { _, _, _ -> },
    onStopVibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Test Alert Action Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.DedsecGreenBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ALERT ENGINE // DISPATCHER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = DedsecNeonGreen
                        )
                        Text(
                            text = "Mode: ${config.alertMode.name}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DedsecTextSecondary
                        )
                    }

                    Button(
                        onClick = onTestAlert,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("test_alert_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTesting) DedsecGlitchRed else DedsecNeonGreen,
                            contentColor = DedsecBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isTesting) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                            contentDescription = "Test Alert"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTesting) "FIRING..." else "TEST ALERT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Visual rhythm wave indicator
                Spacer(modifier = Modifier.height(10.dp))
                RhythmVisualizer(pattern = config.rhythmPattern, isFiring = isTesting)
            }
        }

        // Section 1: Alert Mode Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DISPATCH MODE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DedsecNeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                listOf(
                    Pair(AlertMode.VIBRATE_AND_SOUND, "VIBRATE + SOUND (Full Sensory)"),
                    Pair(AlertMode.VIBRATE_ONLY, "VIBRATION ONLY (Tactile Morse)"),
                    Pair(AlertMode.SOUND_ONLY, "SOUND ONLY (Audio Alert)"),
                    Pair(AlertMode.NOTIFICATION_ONLY, "NOTIFICATION ONLY (Silent)")
                ).forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUpdateConfig(config.copy(alertMode = mode)) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.alertMode == mode,
                            onClick = { onUpdateConfig(config.copy(alertMode = mode)) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = DedsecNeonGreen,
                                unselectedColor = DedsecBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (config.alertMode == mode) DedsecTextPrimary else DedsecTextSecondary
                        )
                    }
                }
            }
        }

        // Section 2: Vibration Intensity & Rhythm Programmer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = DedsecNeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VIBRATION INTENSITY & RHYTHM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DedsecNeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Intensity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "INTENSITY / AMPLITUDE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DedsecTextSecondary
                    )
                    Text(
                        text = "${config.vibrationIntensity} / 255 (${(config.vibrationIntensity * 100) / 255}%)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = DedsecNeonGreen
                    )
                }

                Slider(
                    value = config.vibrationIntensity.toFloat(),
                    onValueChange = { onUpdateConfig(config.copy(vibrationIntensity = it.toInt())) },
                    valueRange = 1f..255f,
                    steps = 254,
                    colors = SliderDefaults.colors(
                        thumbColor = DedsecNeonGreen,
                        activeTrackColor = DedsecNeonGreen,
                        inactiveTrackColor = DedsecSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Rhythm Pattern String Input
                Text(
                    text = "PROGRAM RHYTHM (MORSE CODE OR MS TIMINGS)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = config.rhythmPattern,
                    onValueChange = { onUpdateConfig(config.copy(rhythmPattern = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rhythm_pattern_input"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DedsecNeonGreen
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonGreen,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    singleLine = true,
                    placeholder = {
                        Text("...---.../...---...", fontFamily = FontFamily.Monospace, color = DedsecBorder)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Morse Input Bar
                Text(
                    text = "MORSE KEYBOARD // PUNCH CODE:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair(".", "DOT (.)"),
                        Pair("-", "DASH (-)"),
                        Pair(" ", "GAP ( )"),
                        Pair("/", "BREAK (/)")
                    ).forEach { (symbol, label) ->
                        OutlinedButton(
                            onClick = { onUpdateConfig(config.copy(rhythmPattern = config.rhythmPattern + symbol)) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecNeonCyan)
                        ) {
                            Text(text = symbol, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (config.rhythmPattern.isNotEmpty()) {
                                onUpdateConfig(config.copy(rhythmPattern = config.rhythmPattern.dropLast(1)))
                            }
                        },
                        modifier = Modifier.height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecGlitchRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecGlitchRed)
                    ) {
                        Icon(imageVector = Icons.Default.Backspace, contentDescription = "Backspace", modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Presets Dropdown/Chips
                Text(
                    text = "RHYTHM PRESETS:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MorseRhythmEngine.PRESETS.take(3).forEach { (name, pattern) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateConfig(config.copy(rhythmPattern = pattern)) },
                            shape = RoundedCornerShape(50),
                            color = if (config.rhythmPattern == pattern) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (config.rhythmPattern == pattern) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (config.rhythmPattern == pattern) FontWeight.Bold else FontWeight.Normal,
                                color = if (config.rhythmPattern == pattern) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MorseRhythmEngine.PRESETS.drop(3).take(3).forEach { (name, pattern) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateConfig(config.copy(rhythmPattern = pattern)) },
                            shape = RoundedCornerShape(50),
                            color = if (config.rhythmPattern == pattern) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (config.rhythmPattern == pattern) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (config.rhythmPattern == pattern) FontWeight.Bold else FontWeight.Normal,
                                color = if (config.rhythmPattern == pattern) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Sound Engine & Custom Audio File
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, tint = DedsecNeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AUDIO ENGINE & SOUND FILE INPUT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DedsecNeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Source Type Radio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateConfig(config.copy(soundSourceType = SoundSourceType.BUILT_IN_CYBER_SYNTH)) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = config.soundSourceType == SoundSourceType.BUILT_IN_CYBER_SYNTH,
                        onClick = { onUpdateConfig(config.copy(soundSourceType = SoundSourceType.BUILT_IN_CYBER_SYNTH)) },
                        colors = RadioButtonDefaults.colors(selectedColor = DedsecNeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("DEDSEC CYBER SYNTH (Built-in)", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = DedsecTextPrimary)
                        Text("Synthesized cyberpunk radar chirps and alert tones", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = DedsecTextSecondary)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateConfig(config.copy(soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE)) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = config.soundSourceType == SoundSourceType.CUSTOM_AUDIO_FILE,
                        onClick = { onUpdateConfig(config.copy(soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE)) },
                        colors = RadioButtonDefaults.colors(selectedColor = DedsecNeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("CUSTOM AUDIO FILE INPUT", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = DedsecTextPrimary)
                        Text(
                            text = config.customAudioName ?: "No sound file loaded (tap to import .mp3 / .wav)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (config.customAudioName != null) DedsecNeonGreen else DedsecTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onPickAudioFile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("pick_sound_file_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.DedsecCardInner,
                        contentColor = DedsecNeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                ) {
                    Icon(imageVector = Icons.Default.Audiotrack, contentDescription = "Pick Audio", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (config.customAudioName != null) "CHANGE AUDIO FILE (${config.customAudioName})" else "SELECT AUDIO FILE (.mp3, .wav, .ogg)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Recent Sounds Carousel / List
                if (recentSounds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = DedsecNeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "RECENT AUDIO FILES",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DedsecNeonGreen
                            )
                        }
                        Text(
                            text = "${recentSounds.size} SAVED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = DedsecTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recentSounds.forEach { sound ->
                            val isSelected = config.customAudioUri == sound.uriString
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectRecentSound(sound) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) DedsecNeonGreen else DedsecBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = null,
                                        tint = if (isSelected) DedsecNeonGreen else DedsecNeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sound.name,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) DedsecNeonGreen else DedsecTextPrimary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isSelected) "● ACTIVE GLOBAL SOUND" else "Tap to set as active sound",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.5.sp,
                                            color = if (isSelected) DedsecNeonGreen else DedsecTextSecondary
                                        )
                                    }

                                    // Preview Play Button
                                    IconButton(
                                        onClick = { onPreviewSound(sound.uriString) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Preview Sound",
                                            tint = DedsecNeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete from Recent List Button
                                    IconButton(
                                        onClick = { onDeleteRecentSound(sound.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Sound",
                                            tint = DedsecGlitchRed.copy(alpha = 0.7f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Volume Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("AUDIO VOLUME", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = DedsecTextSecondary)
                    Text("${(config.soundVolume * 100).toInt()}%", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DedsecNeonCyan)
                }

                Slider(
                    value = config.soundVolume,
                    onValueChange = { onUpdateConfig(config.copy(soundVolume = it)) },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = DedsecNeonCyan,
                        activeTrackColor = DedsecNeonCyan,
                        inactiveTrackColor = DedsecSurfaceVariant
                    )
                )
            }
        }

        // Section 4: Text-to-Voice (TTS) Synthesizer Engine
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (config.ttsEnabled) DedsecNeonGreen else DedsecBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = if (config.ttsEnabled) DedsecNeonGreen else DedsecNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TEXT-TO-VOICE (TTS) ENGINE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (config.ttsEnabled) DedsecNeonGreen else DedsecTextPrimary
                            )
                            Text(
                                text = "Synthetic voice alert for detected intercepts",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                color = DedsecTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = config.ttsEnabled,
                        onCheckedChange = { onUpdateConfig(config.copy(ttsEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DedsecBlack,
                            checkedTrackColor = DedsecNeonGreen,
                            uncheckedThumbColor = DedsecTextSecondary,
                            uncheckedTrackColor = com.example.ui.theme.DedsecCardInner
                        )
                    )
                }

                if (config.ttsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VOICE PHRASE TEMPLATE:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = DedsecTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = config.ttsText,
                        onValueChange = { onUpdateConfig(config.copy(ttsText = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = DedsecTextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DedsecNeonGreen,
                            unfocusedBorderColor = DedsecBorder,
                            focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                            unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Placeholder chips
                    val placeholders = listOf("{{nome}}", "{{sinal}}", "{{mac}}", "{{rule}}", "{{time}}", "{{type}}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        placeholders.take(3).forEach { ph ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateConfig(config.copy(ttsText = config.ttsText + " $ph")) },
                                shape = RoundedCornerShape(6.dp),
                                color = com.example.ui.theme.DedsecCardInner,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                            ) {
                                Text(
                                    text = ph,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = DedsecNeonGreen,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        placeholders.drop(3).forEach { ph ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateConfig(config.copy(ttsText = config.ttsText + " $ph")) },
                                shape = RoundedCornerShape(6.dp),
                                color = com.example.ui.theme.DedsecCardInner,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                            ) {
                                Text(
                                    text = ph,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = DedsecNeonGreen,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("VOICE PITCH (TOM)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = DedsecTextSecondary)
                        Text(
                            text = when {
                                config.ttsPitch < 0.8f -> "${String.format(java.util.Locale.US, "%.1f", config.ttsPitch)}x (Deep Robot)"
                                config.ttsPitch > 1.2f -> "${String.format(java.util.Locale.US, "%.1f", config.ttsPitch)}x (High Pitch)"
                                else -> "${String.format(java.util.Locale.US, "%.1f", config.ttsPitch)}x (Natural)"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = DedsecNeonGreen
                        )
                    }
                    Slider(
                        value = config.ttsPitch,
                        onValueChange = { onUpdateConfig(config.copy(ttsPitch = it)) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = DedsecNeonGreen,
                            activeTrackColor = DedsecNeonGreen,
                            inactiveTrackColor = DedsecSurfaceVariant
                        )
                    )

                    // Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("VOICE SPEED (VELOCIDADE)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = DedsecTextSecondary)
                        Text(
                            text = when {
                                config.ttsSpeed < 0.8f -> "${String.format(java.util.Locale.US, "%.1f", config.ttsSpeed)}x (Slow)"
                                config.ttsSpeed > 1.2f -> "${String.format(java.util.Locale.US, "%.1f", config.ttsSpeed)}x (Fast)"
                                else -> "${String.format(java.util.Locale.US, "%.1f", config.ttsSpeed)}x (Normal)"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = DedsecNeonCyan
                        )
                    }
                    Slider(
                        value = config.ttsSpeed,
                        onValueChange = { onUpdateConfig(config.copy(ttsSpeed = it)) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = DedsecNeonCyan,
                            activeTrackColor = DedsecNeonCyan,
                            inactiveTrackColor = DedsecSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Test TTS Preview Button
                    Button(
                        onClick = {
                            val sample = config.ttsText
                                .replace("{{nome}}", "TARGET_NODE")
                                .replace("{{sinal}}", "-48dBm")
                                .replace("{{mac}}", "AA:BB:CC:11:22:33")
                                .replace("{{rule}}", "DEFAULT")
                                .replace("{{time}}", "12:00")
                                .replace("{{type}}", "WIFI")
                            onTestTts(sample, config.ttsPitch, config.ttsSpeed)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.ui.theme.DedsecGreenGlow,
                            contentColor = DedsecNeonGreen
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Test Voice",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TEST VOICE SYNTHESIZER (TTS)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 4: Notification & Cooldown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = DedsecGlitchRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NOTIFICATION & ALERT COOLDOWN",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DedsecGlitchRed
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("CUSTOM NOTIFICATION TITLE", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = DedsecTextSecondary)
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = config.customNotificationTitle,
                    onValueChange = { onUpdateConfig(config.copy(customNotificationTitle = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = DedsecTextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecGlitchRed,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ALERT COOLDOWN", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = DedsecTextSecondary)
                    Text("${config.cooldownSeconds}s", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DedsecGlitchRed)
                }

                Slider(
                    value = config.cooldownSeconds.toFloat(),
                    onValueChange = { onUpdateConfig(config.copy(cooldownSeconds = it.toInt())) },
                    valueRange = 2f..30f,
                    steps = 27,
                    colors = SliderDefaults.colors(
                        thumbColor = DedsecGlitchRed,
                        activeTrackColor = DedsecGlitchRed,
                        inactiveTrackColor = DedsecSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun RhythmVisualizer(
    pattern: String,
    isFiring: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        color = DedsecBlack,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isFiring) DedsecNeonGreen else DedsecBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            pattern.take(32).forEach { ch ->
                when (ch) {
                    '.' -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isFiring) DedsecGlitchRed else DedsecNeonGreen)
                        )
                    }
                    '-' -> {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isFiring) DedsecGlitchRed else DedsecNeonCyan)
                        )
                    }
                    ' ' -> {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    '/' -> {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(DedsecBorder)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(DedsecTextSecondary)
                        )
                    }
                }
            }
        }
    }
}
