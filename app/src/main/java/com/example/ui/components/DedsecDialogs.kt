package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertMode
import com.example.data.model.RecentSound
import com.example.data.model.SignalType
import com.example.data.model.SoundSourceType
import com.example.data.model.TargetRule
import com.example.data.model.TargetType
import com.example.ui.theme.*

@Composable
fun AddTargetDialog(
    onDismiss: () -> Unit,
    onAdd: (
        pattern: String,
        type: TargetType,
        alertMode: AlertMode,
        morse: String,
        notifTitle: String,
        notifMsg: String
    ) -> Unit
) {
    var pattern by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TargetType.ANY) }
    var selectedAlertMode by remember { mutableStateOf(AlertMode.VIBRATE_AND_SOUND) }
    var morsePattern by remember { mutableStateOf("...---...") }
    var notifTitle by remember { mutableStateOf("⚠️ ALVO // {{nome}}") }
    var notifMsg by remember {
        mutableStateOf("[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}} | {{time}}")
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DedsecDarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "// ADD TARGET RULE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DedsecNeonGreen
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Text(
                    text = "Enter SSID, BSSID, BT Name, or MAC prefix.\nSupports wildcards '*' and '?' (e.g. CLARO-* or ab.cd.*):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_target_pattern_input"),
                    placeholder = { Text("e.g. CLARO-* or **:**:**:**:94:**", fontFamily = FontFamily.Monospace, color = DedsecBorder) },
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = DedsecTextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonGreen,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    singleLine = true
                )

                // Quick MAC Wildcard Template Button
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        pattern = "**:**:**:**:94:**"
                        selectedType = TargetType.ANY
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("btn_mac_wildcard_template"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.DedsecGreenGlow,
                        contentColor = DedsecNeonGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "MAC Wildcard Template",
                        modifier = Modifier.size(16.dp),
                        tint = DedsecNeonGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INSERIR MODELO: **:**:**:**:94:**",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "**:**:**:**:94:**" to "**:**:**:**:94:**",
                        "**:**:**:**:**:**" to "**:**:**:**:**:**",
                        "00:1A:2B:*:*:*" to "00:1A:2B:*:*:*",
                        "*:*:*:*:*:94" to "*:*:*:*:*:94"
                    ).forEach { (label, model) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    pattern = model
                                    selectedType = TargetType.ANY
                                },
                            shape = RoundedCornerShape(6.dp),
                            color = if (pattern == model) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (pattern == model) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = if (pattern == model) FontWeight.Bold else FontWeight.Normal,
                                color = if (pattern == model) DedsecNeonGreen else DedsecNeonCyan,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 Toque no botão acima para preencher com asteriscos e dois pontos. Altere '94' para o octeto desejado.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = DedsecTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "TARGET TYPE:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(TargetType.ANY, TargetType.WIFI_SSID, TargetType.WIFI_BSSID).forEach { type ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(50),
                            color = if (selectedType == type) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedType == type) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = type.name.replace("WIFI_", ""),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedType == type) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alert Mode selector for this rule
                Text(
                    text = "ALERT TRIGGER (ESTA REGRA):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecNeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        Triple(AlertMode.VIBRATE_AND_SOUND, "⚡ AMBOS", Icons.Default.Bolt),
                        Triple(AlertMode.VIBRATE_ONLY, "📳 VIBRAÇÃO", Icons.Default.Vibration),
                        Triple(AlertMode.SOUND_ONLY, "🔊 SOM", Icons.Default.VolumeUp)
                    )
                    modes.forEach { (mode, label, _) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedAlertMode = mode },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedAlertMode == mode) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedAlertMode == mode) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = if (selectedAlertMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedAlertMode == mode) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Morse Pattern
                Text(
                    text = "RITMO MORSE (VIBRAÇÃO):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = morsePattern,
                    onValueChange = { morsePattern = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = DedsecNeonGreen,
                        fontSize = 12.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonGreen,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Quick preset chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "SOS" to "...---...",
                        "PULSO" to "... / ...",
                        "BATIMENTO" to ". - .. -",
                        "RÁPIDO" to "....."
                    ).forEach { (label, pat) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { morsePattern = pat },
                            shape = RoundedCornerShape(50),
                            color = if (morsePattern == pat) com.example.ui.theme.DedsecGreenGlow else DedsecDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (morsePattern == pat) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = if (morsePattern == pat) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notification Message with Placeholders
                Text(
                    text = "MENSAGEM DA NOTIFICAÇÃO:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = notifMsg,
                    onValueChange = { notifMsg = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = DedsecTextPrimary,
                        fontSize = 11.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonCyan,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PLACEHOLDERS DISPONÍVEIS (toque para inserir):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = DedsecNeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Placeholder insertion chips
                val placeholders = listOf("{{nome}}", "{{sinal}}", "{{mac}}", "{{rule}}", "{{time}}", "{{type}}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    placeholders.take(3).forEach { ph ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { notifMsg += " $ph" },
                            shape = RoundedCornerShape(6.dp),
                            color = com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                        ) {
                            Text(
                                text = ph,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = DedsecNeonCyan,
                                modifier = Modifier.padding(vertical = 3.dp),
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
                                .clickable { notifMsg += " $ph" },
                            shape = RoundedCornerShape(6.dp),
                            color = com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                        ) {
                            Text(
                                text = ph,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = DedsecNeonCyan,
                                modifier = Modifier.padding(vertical = 3.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pattern.isNotBlank()) {
                        onAdd(
                            pattern.trim(),
                            selectedType,
                            selectedAlertMode,
                            morsePattern.trim().ifBlank { "...---..." },
                            notifTitle.trim().ifBlank { "⚠️ ALVO // {{nome}}" },
                            notifMsg.trim().ifBlank { "[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}} | {{time}}" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DedsecNeonGreen,
                    contentColor = DedsecBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_add_target_button")
            ) {
                Text("ADD RULE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecTextSecondary)
            ) {
                Text("CANCEL", fontFamily = FontFamily.Monospace)
            }
        }
    )
}

@Composable
fun EditTargetRuleDialog(
    rule: TargetRule,
    recentSounds: List<RecentSound> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TargetRule) -> Unit,
    onTestRule: (TargetRule) -> Unit,
    onPickAudioFile: () -> Unit,
    onPreviewSound: (String) -> Unit = {},
    onTestTts: (text: String, pitch: Float, speed: Float) -> Unit = { _, _, _ -> }
) {
    var pattern by remember { mutableStateOf(rule.rawPattern) }
    var selectedType by remember { mutableStateOf(rule.targetType) }
    var selectedAlertMode by remember { mutableStateOf(rule.alertMode) }
    var morsePattern by remember { mutableStateOf(rule.morsePattern) }
    var notifTitle by remember { mutableStateOf(rule.notificationTitle) }
    var notifMsg by remember { mutableStateOf(rule.notificationMessage) }

    // Per-rule Audio & TTS states
    var soundSourceType by remember { mutableStateOf(rule.soundSourceType) }
    var customAudioUri by remember { mutableStateOf(rule.customAudioUri) }
    var customAudioName by remember { mutableStateOf(rule.customAudioName) }
    var soundVolume by remember { mutableStateOf(rule.soundVolume) }
    var ttsEnabled by remember { mutableStateOf(rule.ttsEnabled) }
    var ttsText by remember { mutableStateOf(rule.ttsText) }
    var ttsPitch by remember { mutableStateOf(rule.ttsPitch) }
    var ttsSpeed by remember { mutableStateOf(rule.ttsSpeed) }

    // Sync if audio picked externally
    androidx.compose.runtime.LaunchedEffect(rule.customAudioUri, rule.customAudioName, rule.soundSourceType) {
        if (rule.customAudioUri != null) {
            customAudioUri = rule.customAudioUri
            customAudioName = rule.customAudioName
            soundSourceType = rule.soundSourceType
        }
    }

    val scrollState = rememberScrollState()

    // Current draft rule for test or save
    val currentDraft = remember(
        pattern, selectedType, selectedAlertMode, morsePattern, notifTitle, notifMsg,
        soundSourceType, customAudioUri, customAudioName, soundVolume,
        ttsEnabled, ttsText, ttsPitch, ttsSpeed
    ) {
        rule.copy(
            rawPattern = pattern.trim().ifBlank { rule.rawPattern },
            targetType = selectedType,
            alertMode = selectedAlertMode,
            morsePattern = morsePattern.trim().ifBlank { "...---..." },
            notificationTitle = notifTitle.trim().ifBlank { "⚠️ ALVO // {{nome}}" },
            notificationMessage = notifMsg.trim().ifBlank { "[{{type}}] {{nome}} ({{mac}}) | Sinal: {{sinal}} | Regra: {{rule}} | {{time}}" },
            soundSourceType = soundSourceType,
            customAudioUri = customAudioUri,
            customAudioName = customAudioName,
            soundVolume = soundVolume,
            ttsEnabled = ttsEnabled,
            ttsText = ttsText.trim().ifBlank { "Alvo detectado: {{nome}}, sinal {{sinal}}" },
            ttsPitch = ttsPitch,
            ttsSpeed = ttsSpeed
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DedsecDarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = DedsecNeonGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "// EDIT TARGET ALERTS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DedsecNeonGreen
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                // Rule Pattern
                Text(
                    text = "TARGET PATTERN (SSID / MAC):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DedsecNeonGreen,
                        fontSize = 13.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonGreen,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    singleLine = true
                )

                // Quick MAC Wildcard Template Button
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        pattern = "**:**:**:**:94:**"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("btn_edit_mac_wildcard_template"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.DedsecGreenGlow,
                        contentColor = DedsecNeonGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "MAC Wildcard Template",
                        modifier = Modifier.size(16.dp),
                        tint = DedsecNeonGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INSERIR MODELO: **:**:**:**:94:**",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "**:**:**:**:94:**" to "**:**:**:**:94:**",
                        "**:**:**:**:**:**" to "**:**:**:**:**:**",
                        "00:1A:2B:*:*:*" to "00:1A:2B:*:*:*",
                        "*:*:*:*:*:94" to "*:*:*:*:*:94"
                    ).forEach { (label, model) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { pattern = model },
                            shape = RoundedCornerShape(6.dp),
                            color = if (pattern == model) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (pattern == model) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                fontWeight = if (pattern == model) FontWeight.Bold else FontWeight.Normal,
                                color = if (pattern == model) DedsecNeonGreen else DedsecNeonCyan,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 Toque no botão acima para preencher com asteriscos e dois pontos. Altere '94' para o octeto desejado.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = DedsecTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Type
                Text(
                    text = "TARGET TYPE:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        TargetType.ANY,
                        TargetType.WIFI_SSID,
                        TargetType.WIFI_BSSID,
                        TargetType.BLUETOOTH_NAME
                    ).forEach { type ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(50),
                            color = if (selectedType == type) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedType == type) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = when (type) {
                                    TargetType.ANY -> "ANY"
                                    TargetType.WIFI_SSID -> "SSID"
                                    TargetType.WIFI_BSSID -> "BSSID"
                                    TargetType.BLUETOOTH_NAME -> "BT"
                                    else -> type.name
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedType == type) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alert Mode: Vibration Only, Sound Only, Both
                Text(
                    text = "MODO DE ALERTA INDIVIDUAL:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DedsecNeonCyan
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        Triple(AlertMode.VIBRATE_AND_SOUND, "⚡ AMBOS", "Vibração + Som"),
                        Triple(AlertMode.VIBRATE_ONLY, "📳 VIBRAÇÃO", "Apenas Vibra"),
                        Triple(AlertMode.SOUND_ONLY, "🔊 SOM", "Apenas Som")
                    )
                    modes.forEach { (mode, label, desc) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedAlertMode = mode },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedAlertMode == mode) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selectedAlertMode == mode) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedAlertMode == mode) DedsecNeonGreen else DedsecTextPrimary
                                )
                                Text(
                                    text = desc,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.5.sp,
                                    color = DedsecTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // SOUND INPUT & RECENT SOUNDS FOR THIS RULE
                // ==========================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = com.example.ui.theme.DedsecCardInner,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = DedsecNeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SOM INDIVIDUAL DESTA REGRA",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = DedsecNeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sound source toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { soundSourceType = SoundSourceType.BUILT_IN_CYBER_SYNTH },
                                shape = RoundedCornerShape(8.dp),
                                color = if (soundSourceType == SoundSourceType.BUILT_IN_CYBER_SYNTH) com.example.ui.theme.DedsecGreenGlow else DedsecDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (soundSourceType == SoundSourceType.BUILT_IN_CYBER_SYNTH) DedsecNeonGreen else DedsecBorder
                                )
                            ) {
                                Text(
                                    text = "SYNTH CYBER",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (soundSourceType == SoundSourceType.BUILT_IN_CYBER_SYNTH) DedsecNeonGreen else DedsecTextSecondary,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE },
                                shape = RoundedCornerShape(8.dp),
                                color = if (soundSourceType == SoundSourceType.CUSTOM_AUDIO_FILE) com.example.ui.theme.DedsecGreenGlow else DedsecDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (soundSourceType == SoundSourceType.CUSTOM_AUDIO_FILE) DedsecNeonGreen else DedsecBorder
                                )
                            ) {
                                Text(
                                    text = "ARQUIVO PRÓPRIO",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (soundSourceType == SoundSourceType.CUSTOM_AUDIO_FILE) DedsecNeonGreen else DedsecTextSecondary,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        if (soundSourceType == SoundSourceType.CUSTOM_AUDIO_FILE) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Current custom sound badge (if chosen)
                            if (!customAudioUri.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = DedsecDarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = DedsecNeonGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = customAudioName ?: "Arquivo de Áudio",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = DedsecNeonGreen,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        IconButton(
                                            onClick = { onPreviewSound(customAudioUri!!) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Ouvir",
                                                tint = DedsecNeonCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                customAudioUri = null
                                                customAudioName = null
                                                soundSourceType = SoundSourceType.BUILT_IN_CYBER_SYNTH
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remover",
                                                tint = DedsecGlitchRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Pick Sound File Button
                            Button(
                                onClick = onPickAudioFile,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DedsecDarkSurface,
                                    contentColor = DedsecNeonCyan
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonCyan)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = "Select Sound",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IMPORTAR ARQUIVO DE SOM",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Recent Sounds List
                            if (recentSounds.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = DedsecTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SONS RECENTES (Toque para usar):",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = DedsecTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    recentSounds.take(5).forEach { sound ->
                                        val isThisSelected = customAudioUri == sound.uriString
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    soundSourceType = SoundSourceType.CUSTOM_AUDIO_FILE
                                                    customAudioUri = sound.uriString
                                                    customAudioName = sound.name
                                                },
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isThisSelected) com.example.ui.theme.DedsecGreenGlow else DedsecDarkSurface,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isThisSelected) DedsecNeonGreen else DedsecBorder
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🎵 ${sound.name}",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 9.5.sp,
                                                    color = if (isThisSelected) DedsecNeonGreen else DedsecTextPrimary,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { onPreviewSound(sound.uriString) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Ouvir",
                                                        tint = DedsecNeonCyan,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rule Sound Volume Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "VOLUME DESTA REGRA:",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    color = DedsecTextSecondary
                                )
                                Text(
                                    text = "${(soundVolume * 100).toInt()}%",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DedsecNeonCyan
                                )
                            }
                            Slider(
                                value = soundVolume,
                                onValueChange = { soundVolume = it },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = DedsecNeonCyan,
                                    activeTrackColor = DedsecNeonCyan,
                                    inactiveTrackColor = DedsecDarkSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // TEXT-TO-VOICE (TTS) SINTETIZADOR DE VOZ
                // ==========================================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = com.example.ui.theme.DedsecCardInner,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (ttsEnabled) DedsecNeonGreen else DedsecBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = if (ttsEnabled) DedsecNeonGreen else DedsecTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "TEXT-TO-VOICE (TTS)",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (ttsEnabled) DedsecNeonGreen else DedsecTextPrimary
                                    )
                                    Text(
                                        text = "Falar mensagem ao interceptar sinal",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.5.sp,
                                        color = DedsecTextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = ttsEnabled,
                                onCheckedChange = { ttsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DedsecBlack,
                                    checkedTrackColor = DedsecNeonGreen,
                                    uncheckedThumbColor = DedsecTextSecondary,
                                    uncheckedTrackColor = DedsecDarkSurface
                                )
                            )
                        }

                        if (ttsEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "TEXTO A SER FALADO:",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                color = DedsecTextSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            OutlinedTextField(
                                value = ttsText,
                                onValueChange = { ttsText = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = DedsecTextPrimary
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DedsecNeonGreen,
                                    unfocusedBorderColor = DedsecBorder,
                                    focusedContainerColor = DedsecDarkSurface,
                                    unfocusedContainerColor = DedsecDarkSurface
                                ),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick chips for TTS text
                            val ttsPlaceholders = listOf("{{nome}}", "{{sinal}}", "{{mac}}", "{{rule}}", "{{type}}")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ttsPlaceholders.forEach { ph ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { ttsText += " $ph" },
                                        shape = RoundedCornerShape(4.dp),
                                        color = DedsecDarkSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                                    ) {
                                        Text(
                                            text = ph,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = DedsecNeonGreen,
                                            modifier = Modifier.padding(vertical = 3.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Pitch Control (Tom da voz)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TOM (PITCH):",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    color = DedsecTextSecondary
                                )
                                Text(
                                    text = when {
                                        ttsPitch < 0.8f -> "${String.format(java.util.Locale.US, "%.1f", ttsPitch)}x (Grave/Robô)"
                                        ttsPitch > 1.2f -> "${String.format(java.util.Locale.US, "%.1f", ttsPitch)}x (Agudo)"
                                        else -> "${String.format(java.util.Locale.US, "%.1f", ttsPitch)}x (Natural)"
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DedsecNeonGreen
                                )
                            }
                            Slider(
                                value = ttsPitch,
                                onValueChange = { ttsPitch = it },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = DedsecNeonGreen,
                                    activeTrackColor = DedsecNeonGreen,
                                    inactiveTrackColor = DedsecDarkSurface
                                )
                            )

                            // Speed Control (Velocidade de fala)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "VELOCIDADE (SPEED):",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    color = DedsecTextSecondary
                                )
                                Text(
                                    text = when {
                                        ttsSpeed < 0.8f -> "${String.format(java.util.Locale.US, "%.1f", ttsSpeed)}x (Lento)"
                                        ttsSpeed > 1.2f -> "${String.format(java.util.Locale.US, "%.1f", ttsSpeed)}x (Rápido)"
                                        else -> "${String.format(java.util.Locale.US, "%.1f", ttsSpeed)}x (Normal)"
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DedsecNeonCyan
                                )
                            }
                            Slider(
                                value = ttsSpeed,
                                onValueChange = { ttsSpeed = it },
                                valueRange = 0.5f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = DedsecNeonCyan,
                                    activeTrackColor = DedsecNeonCyan,
                                    inactiveTrackColor = DedsecDarkSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Test TTS voice button
                            Button(
                                onClick = {
                                    val previewText = currentDraft.formatTemplate(
                                        template = ttsText,
                                        signalName = "ALVO_TESTE",
                                        signalMac = "00:11:22:33:44:55",
                                        signalRssi = -50,
                                        signalType = "WIFI",
                                        timestamp = System.currentTimeMillis()
                                    )
                                    onTestTts(previewText, ttsPitch, ttsSpeed)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.ui.theme.DedsecGreenGlow,
                                    contentColor = DedsecNeonGreen
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Test Voice",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OUVIR VOZ (TESTAR TTS)",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Morse Rhythm for this rule
                Text(
                    text = "RITMO MORSE DESTA REGRA:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecNeonGreen
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = morsePattern,
                    onValueChange = { morsePattern = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = DedsecNeonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonGreen,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Preset chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "SOS" to "...---...",
                        "DEDSEC" to "... / ...",
                        "PULSO" to ". - .. -",
                        "RÁPIDO" to "..... .....",
                        "PESADO" to "--- ---"
                    ).forEach { (label, pat) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { morsePattern = pat },
                            shape = RoundedCornerShape(50),
                            color = if (morsePattern == pat) com.example.ui.theme.DedsecGreenGlow else DedsecDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (morsePattern == pat) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = if (morsePattern == pat) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notification Title
                Text(
                    text = "TÍTULO DA NOTIFICAÇÃO:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notifTitle,
                    onValueChange = { notifTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = DedsecTextPrimary,
                        fontSize = 11.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonCyan,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notification Message
                Text(
                    text = "MENSAGEM DA NOTIFICAÇÃO:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notifMsg,
                    onValueChange = { notifMsg = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = DedsecTextPrimary,
                        fontSize = 11.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonCyan,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Clickable Placeholder Insertion Chips
                Text(
                    text = "PLACEHOLDERS (toque para adicionar à mensagem):",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = DedsecNeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                val placeholders = listOf("{{nome}}", "{{sinal}}", "{{mac}}", "{{rule}}", "{{time}}", "{{type}}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    placeholders.take(3).forEach { ph ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { notifMsg += " $ph" },
                            shape = RoundedCornerShape(6.dp),
                            color = com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = ph,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = DedsecNeonCyan,
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
                                .clickable { notifMsg += " $ph" },
                            shape = RoundedCornerShape(6.dp),
                            color = com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = ph,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = DedsecNeonCyan,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DedsecBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PRÉVIA DA NOTIFICAÇÃO:",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            color = DedsecTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentDraft.formatTemplate(
                                template = notifTitle,
                                signalName = "TARGET_NODE_1",
                                signalMac = "00:11:22:33:44:55",
                                signalRssi = -55,
                                signalType = "WIFI",
                                timestamp = System.currentTimeMillis()
                            ),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = DedsecNeonGreen
                        )
                        Text(
                            text = currentDraft.formatTemplate(
                                template = notifMsg,
                                signalName = "TARGET_NODE_1",
                                signalMac = "00:11:22:33:44:55",
                                signalRssi = -55,
                                signalType = "WIFI",
                                timestamp = System.currentTimeMillis()
                            ),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = DedsecTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instant Test Button
                Button(
                    onClick = { onTestRule(currentDraft) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.DedsecCardInner,
                        contentColor = DedsecNeonGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test Alert",
                        modifier = Modifier.size(16.dp),
                        tint = DedsecNeonGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TESTAR ALERTA DESTA REGRA AGORA",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DedsecNeonGreen
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(currentDraft)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DedsecNeonGreen,
                    contentColor = DedsecBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("SALVAR REGRA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecTextSecondary)
            ) {
                Text("CANCELAR", fontFamily = FontFamily.Monospace)
            }
        }
    )
}

@Composable
fun BulkImportDialog(
    onDismiss: () -> Unit,
    onImport: (text: String) -> Unit
) {
    var rawText by remember {
        mutableStateOf(
            "CLARO-*\nNET-*\nab.cd.*\nVivo-*\n00:11:22:*"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DedsecDarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "// BULK IMPORT TARGET LIST",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DedsecNeonCyan
            )
        },
        text = {
            Column {
                Text(
                    text = "Paste items separated by SPACES or NEWLINES.\nLines with '#' are ignored as comments.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("bulk_import_textarea"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = DedsecNeonGreen
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonCyan,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    )
                )

                // Quick MAC wildcard insertion buttons for bulk list
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                rawText = if (rawText.isBlank()) "**:**:**:**:94:**" else "$rawText\n**:**:**:**:94:**"
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = com.example.ui.theme.DedsecGreenGlow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = DedsecNeonGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ **:**:**:**:94:**", fontFamily = FontFamily.Monospace, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = DedsecNeonGreen)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                rawText = if (rawText.isBlank()) "**:**:**:**:**:**" else "$rawText\n**:**:**:**:**:**"
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = com.example.ui.theme.DedsecCardInner,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("+ **:**:**:**:**:**", fontFamily = FontFamily.Monospace, fontSize = 9.5.sp, color = DedsecNeonCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tokens detected: ${rawText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = DedsecTextSecondary
                    )
                    Text(
                        text = "RESET DEFAULT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = DedsecNeonCyan,
                        modifier = Modifier.clickable {
                            rawText = "CLARO-*\nNET-*\nab.cd.*\nVivo-*\n00:11:22:*"
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawText.isNotBlank()) {
                        onImport(rawText)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DedsecNeonCyan,
                    contentColor = DedsecBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_bulk_import_button")
            ) {
                Text("PARSE & IMPORT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecTextSecondary)
            ) {
                Text("CANCEL", fontFamily = FontFamily.Monospace)
            }
        }
    )
}

@Composable
fun SimulationDialog(
    onDismiss: () -> Unit,
    onInject: (type: SignalType, name: String, address: String, rssi: Int) -> Unit
) {
    var type by remember { mutableStateOf(SignalType.WIFI) }
    var name by remember { mutableStateOf("CLARO-WIFI-5G") }
    var address by remember { mutableStateOf("AB:CD:12:34:56:78") }
    var rssi by remember { mutableStateOf("-48") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DedsecDarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "⚡ INJECT SIMULATED SIGNAL",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DedsecNeonGreen
            )
        },
        text = {
            Column {
                Text(
                    text = "Emulate physical WiFi / Bluetooth beacons to test wildcard matching, vibration rhythms, and sounds instantly.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = DedsecTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Preset Chips
                Text("QUICK PRESETS:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = DedsecTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("CLARO-5G", "CLARO-NET-WIFI", "00:11:22:33:44:55"),
                        Triple("NET-VIRTUA", "NET-VIRTUA-404", "12:34:56:78:9A:BC"),
                        Triple("ab.cd.* MAC", "HOME-ROUTER", "AB:CD:99:88:77:66")
                    ).forEach { (label, presetName, presetMac) ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    name = presetName
                                    address = presetMac
                                },
                            shape = RoundedCornerShape(50),
                            color = com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                        ) {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DedsecNeonGreen,
                                modifier = Modifier.padding(vertical = 5.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("SIGNAL TYPE:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = DedsecTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(SignalType.WIFI, SignalType.BLUETOOTH).forEach { t ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = t },
                            shape = RoundedCornerShape(50),
                            color = if (type == t) com.example.ui.theme.DedsecGreenGlow else com.example.ui.theme.DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (type == t) DedsecNeonGreen else DedsecBorder
                            )
                        ) {
                            Text(
                                text = t.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = if (type == t) FontWeight.Bold else FontWeight.Normal,
                                color = if (type == t) DedsecNeonGreen else DedsecTextSecondary,
                                modifier = Modifier.padding(vertical = 5.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("SSID or Device Name", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = DedsecTextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonGreen,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("BSSID or Hardware MAC", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = DedsecTextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DedsecNeonGreen,
                        unfocusedBorderColor = DedsecBorder,
                        focusedContainerColor = com.example.ui.theme.DedsecCardInner,
                        unfocusedContainerColor = com.example.ui.theme.DedsecCardInner
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedRssi = rssi.toIntOrNull() ?: -55
                    onInject(type, name, address, parsedRssi)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DedsecNeonGreen,
                    contentColor = DedsecBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_inject_signal_button")
            ) {
                Text("INJECT BEACON", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecTextSecondary)
            ) {
                Text("CANCEL", fontFamily = FontFamily.Monospace)
            }
        }
    )
}
