package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetectedSignal
import com.example.data.model.SignalType
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarView(
    signals: List<DetectedSignal>,
    matchedSignals: List<DetectedSignal>,
    isScanning: Boolean,
    onInjectTestSignal: () -> Unit,
    onSendToRules: ((signal: DetectedSignal, patternType: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(signals, matchedSignals, selectedFilter) {
        when (selectedFilter) {
            "MATCHES" -> matchedSignals
            "WIFI" -> signals.filter { it.type == SignalType.WIFI }
            "BT" -> signals.filter { it.type == SignalType.BLUETOOTH }
            else -> signals
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Cyber Radar Visualizer Mini-Canvas
        CyberRadarCanvas(isScanning = isScanning, signalCount = signals.size)

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "MATCHES", "WIFI", "BT").forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(50),
                    label = {
                        Text(
                            text = if (filter == "MATCHES") "TARGETS (${matchedSignals.size})" else filter,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (filter == "MATCHES") DedsecGlitchRed else DedsecNeonGreen,
                        selectedLabelColor = DedsecBlack,
                        containerColor = DedsecCardInner,
                        labelColor = DedsecTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color.Transparent else DedsecBorder,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Signals List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isScanning) "// SWEEPING SPECTRUM..." else "// SCANNER STANDBY",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DedsecNeonGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isScanning)
                            "Listening for 802.11 beacons & BT advertisements\nOr tap TEST BEACON above to inject target"
                        else
                            "Tap 'ARM INTERCEPTOR' above to begin scanning",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DedsecTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id + it.timestamp }) { signal ->
                    SignalCard(signal = signal)
                }
            }
        }
    }
}

@Composable
fun CyberRadarCanvas(
    isScanning: Boolean,
    signalCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarRotation")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(116.dp),
        shape = RoundedCornerShape(20.dp),
        color = DedsecDarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radar scope canvas
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f

                    // Background circles
                    drawCircle(
                        color = DedsecBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        color = DedsecBorder.copy(alpha = 0.5f),
                        radius = radius * 0.66f,
                        center = center,
                        style = Stroke(width = 1f)
                    )
                    drawCircle(
                        color = DedsecBorder.copy(alpha = 0.3f),
                        radius = radius * 0.33f,
                        center = center,
                        style = Stroke(width = 1f)
                    )

                    // Crosshairs
                    drawLine(
                        color = DedsecBorder,
                        start = Offset(center.x, 0f),
                        end = Offset(center.x, size.height),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = DedsecBorder,
                        start = Offset(0f, center.y),
                        end = Offset(size.width, center.y),
                        strokeWidth = 1f
                    )

                    // Sweeper line if active
                    if (isScanning) {
                        val rad = Math.toRadians(sweepAngle.toDouble())
                        val endX = center.x + (radius * cos(rad)).toFloat()
                        val endY = center.y + (radius * sin(rad)).toFloat()
                        drawLine(
                            brush = Brush.radialGradient(
                                listOf(DedsecNeonGreen, Color.Transparent),
                                center = center,
                                radius = radius
                            ),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.5f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Radar stats
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isScanning) "RADAR // ACTIVE SWEEP" else "RADAR // INACTIVE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isScanning) DedsecNeonGreen else DedsecAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DISCOVERED NODES: $signalCount",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = DedsecTextPrimary
                )
                Text(
                    text = "PROTOCOLS: 802.11 a/b/g/n/ac/ax + BLE 5.x",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecTextSecondary
                )
            }
        }
    }
}

@Composable
fun SignalCard(
    signal: DetectedSignal,
    onSendToRules: ((signal: DetectedSignal, patternType: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isMatched = !signal.matchedRule.isNullOrBlank()

    val signalColor = when {
        signal.rssi >= -60 -> DedsecSignalHigh
        signal.rssi >= -75 -> DedsecSignalMed
        else -> DedsecSignalLow
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_card_${signal.address}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMatched) DedsecDarkSurface else com.example.ui.theme.DedsecCardInner
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMatched) DedsecNeonGreen else DedsecBorder
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sleek left accent bar for matched items
            if (isMatched) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(86.dp)
                        .background(DedsecNeonGreen)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isMatched) com.example.ui.theme.DedsecGreenGlow else DedsecDarkSurface,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = if (signal.type == SignalType.WIFI) Icons.Default.Wifi else Icons.Default.Bluetooth,
                                    contentDescription = signal.type.name,
                                    tint = if (isMatched) DedsecNeonGreen else DedsecNeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = signal.name,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isMatched) Color.White else DedsecTextPrimary,
                            maxLines = 1
                        )
                    }

                    // RSSI dBm
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DedsecDarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                    ) {
                        Text(
                            text = "${signal.rssi} dBm",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = signalColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Address & Protocol
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MAC: ${signal.address}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DedsecTextSecondary
                    )
                    if (signal.frequencyOrExtra.isNotBlank()) {
                        Text(
                            text = signal.frequencyOrExtra,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = DedsecTextSecondary
                        )
                    }
                }

                // Target match badge
                if (isMatched) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = com.example.ui.theme.DedsecGreenGlow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.DedsecGreenBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = "Matched",
                                tint = DedsecNeonGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MATCH: ${signal.matchedRule}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = DedsecNeonGreen
                            )
                        }
                    }
                }

                // Send to Rules action row (Default Disabled)
                if (onSendToRules != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onSendToRules(signal, "MAC") },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DedsecNeonCyan
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+ REGRA MAC (OFF)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (signal.name.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onSendToRules(signal, "NAME") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DedsecNeonGreen
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+ REGRA NOME (OFF)",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
