package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertMode
import com.example.data.model.TargetRule
import com.example.data.model.TargetType
import com.example.ui.theme.*

@Composable
fun TargetsView(
    rules: List<TargetRule>,
    onAddRuleClick: () -> Unit,
    onBulkImportClick: () -> Unit,
    onPickFileClick: () -> Unit,
    onPickWigleFileClick: () -> Unit,
    onToggleRule: (String) -> Unit,
    onDeleteRule: (String) -> Unit,
    onClearAll: () -> Unit,
    onEditRule: (TargetRule) -> Unit = {},
    onTestRule: (TargetRule) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showWildcardHelp by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddRuleClick,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("add_target_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DedsecNeonGreen,
                    contentColor = DedsecBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Target", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD RULE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = onBulkImportClick,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("bulk_import_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DedsecNeonCyan
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PostAdd, contentDescription = "Paste List", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PASTE LIST", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // File Import Row (Text file & WiGLE CSV)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPickFileClick,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("import_file_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecTextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Import File", modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("IMPORT .TXT", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }

            OutlinedButton(
                onClick = onPickWigleFileClick,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("import_wigle_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecNeonGreen),
                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.FileOpen, contentDescription = "Import WiGLE", modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("WiGLE CSV", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            if (rules.isNotEmpty()) {
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = DedsecGlitchRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Wildcard / Syntax Guide Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showWildcardHelp = !showWildcardHelp },
            shape = RoundedCornerShape(14.dp),
            color = DedsecDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = DedsecNeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WILDCARD & MAC SYNTAX TIPS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = DedsecNeonCyan
                        )
                    }
                    Text(
                        text = if (showWildcardHelp) "▲ HIDE" else "▼ SHOW",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = DedsecTextSecondary
                    )
                }

                if (showWildcardHelp) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• '*' matches any sequence: CLARO-* , NET-* , Vivo-*\n" +
                               "• MAC Prefix: ab.cd.* or ab:cd:* matches AB:CD:12:34:56:78\n" +
                               "• Exact MAC: 00:11:22:33:44:55 or 00-11-22-...\n" +
                               "• List format: separate entries with spaces or newlines.\n" +
                               "• WiGLE: imports SSID & MAC columns from wigle.net exports.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = DedsecTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Watchlist Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVE WATCHLIST (${rules.size} RULES)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = DedsecNeonGreen
            )
            Text(
                text = "${rules.count { it.enabled }} ENABLED",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = DedsecTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Rules List
        if (rules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "// WATCHLIST EMPTY\nTap 'ADD RULE' or 'PASTE LIST' to define target patterns",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = DedsecTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    TargetRuleCard(
                        rule = rule,
                        onToggle = { onToggleRule(rule.id) },
                        onDelete = { onDeleteRule(rule.id) },
                        onEdit = { onEditRule(rule) },
                        onTest = { onTestRule(rule) }
                    )
                }
            }
        }
    }
}

@Composable
fun TargetRuleCard(
    rule: TargetRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.enabled) com.example.ui.theme.DedsecCardInner else DedsecDarkSurface.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rule.enabled) com.example.ui.theme.DedsecGreenBorder else DedsecBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Pattern + Switch + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.rawPattern,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (rule.enabled) DedsecNeonGreen else DedsecTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = rule.enabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DedsecBlack,
                            checkedTrackColor = DedsecNeonGreen,
                            uncheckedThumbColor = DedsecTextSecondary,
                            uncheckedTrackColor = DedsecBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Rule",
                            tint = DedsecTextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Badges row: TargetType, AlertMode, Morse Rhythm, Hits
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Type badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DedsecDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                ) {
                    Text(
                        text = rule.targetType.name.replace("WIFI_", ""),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DedsecNeonCyan,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                // Alert Mode badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when (rule.alertMode) {
                        AlertMode.VIBRATE_AND_SOUND -> com.example.ui.theme.DedsecGreenGlow
                        AlertMode.VIBRATE_ONLY -> com.example.ui.theme.DedsecCardInner
                        AlertMode.SOUND_ONLY -> DedsecDarkSurface
                        AlertMode.NOTIFICATION_ONLY -> DedsecDarkSurface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (rule.alertMode) {
                            AlertMode.VIBRATE_AND_SOUND -> DedsecNeonGreen
                            AlertMode.VIBRATE_ONLY -> DedsecNeonCyan
                            AlertMode.SOUND_ONLY -> DedsecBorder
                            AlertMode.NOTIFICATION_ONLY -> DedsecTextSecondary
                        }
                    )
                ) {
                    Text(
                        text = when (rule.alertMode) {
                            AlertMode.VIBRATE_AND_SOUND -> "⚡ VIB+SOM"
                            AlertMode.VIBRATE_ONLY -> "📳 VIBRAÇÃO"
                            AlertMode.SOUND_ONLY -> "🔊 SOM"
                            AlertMode.NOTIFICATION_ONLY -> "🔔 APENAS NOTIF"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (rule.alertMode) {
                            AlertMode.VIBRATE_AND_SOUND -> DedsecNeonGreen
                            AlertMode.VIBRATE_ONLY -> DedsecNeonCyan
                            AlertMode.SOUND_ONLY -> DedsecTextPrimary
                            AlertMode.NOTIFICATION_ONLY -> DedsecTextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Morse badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = DedsecDarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                ) {
                    Text(
                        text = "RITMO: ${rule.morsePattern}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = DedsecNeonGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (rule.hitCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = com.example.ui.theme.DedsecRedGlow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecGlitchRed.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${rule.hitCount} HITS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DedsecGlitchRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Notification message preview
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = DedsecDarkSurface.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, DedsecBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = DedsecTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rule.notificationMessage,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = DedsecTextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: TEST ALERT & CONFIGURE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTest,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DedsecNeonGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test Alert",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TESTAR ALERTA",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.DedsecGreenGlow,
                        contentColor = DedsecNeonGreen
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecNeonGreen.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure Rule",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "EDITAR ALERTA",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
