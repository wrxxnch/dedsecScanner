package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.IconButton
import com.example.data.auth.DedsecUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ScannerStatus
import com.example.ui.theme.DedsecAmber
import com.example.ui.theme.DedsecBlack
import com.example.ui.theme.DedsecBorder
import com.example.ui.theme.DedsecBorderSubtle
import com.example.ui.theme.DedsecCardInner
import com.example.ui.theme.DedsecDarkSurface
import com.example.ui.theme.DedsecGlitchRed
import com.example.ui.theme.DedsecGreenBorder
import com.example.ui.theme.DedsecGreenGlow
import com.example.ui.theme.DedsecNeonCyan
import com.example.ui.theme.DedsecNeonGreen
import com.example.ui.theme.DedsecRedGlow
import com.example.ui.theme.DedsecTextDim
import com.example.ui.theme.DedsecTextPrimary
import com.example.ui.theme.DedsecTextSecondary

@Composable
fun DedsecHeader(
    status: ScannerStatus,
    currentUser: DedsecUser? = null,
    onToggleScan: () -> Unit,
    onOpenSimulation: () -> Unit,
    onSignOut: (() -> Unit)? = null,
    onCheckUpdate: (() -> Unit)? = null,
    onOpenAdmin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DedsecBorderSubtle,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ),
        color = DedsecDarkSurface,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // User Identity & System Toolbar Row
            if (currentUser != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (currentUser.isAdmin) DedsecGreenGlow else DedsecCardInner,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (currentUser.isAdmin) "★" else "●",
                                    fontSize = 11.sp,
                                    color = if (currentUser.isAdmin) DedsecNeonGreen else DedsecNeonCyan
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = currentUser.email,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser.isAdmin) DedsecNeonGreen else DedsecTextPrimary
                            )
                            if (currentUser.isAdmin) {
                                Text(
                                    text = "ADMINISTRATOR",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DedsecNeonGreen
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onCheckUpdate != null) {
                            IconButton(
                                onClick = onCheckUpdate,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Check GitHub Update",
                                    tint = DedsecNeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (currentUser.isAdmin && onOpenAdmin != null) {
                            IconButton(
                                onClick = onOpenAdmin,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Painel Admin",
                                    tint = DedsecNeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (onSignOut != null) {
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sair",
                                    tint = DedsecGlitchRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Top Tagline & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NETWORK INTRUSION SYSTEM",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = DedsecNeonGreen.copy(alpha = 0.85f)
                )

                // Sleek status pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (status.isScanning) DedsecGreenGlow else DedsecCardInner,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (status.isScanning) DedsecGreenBorder else DedsecBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (status.isScanning) DedsecNeonGreen else DedsecAmber)
                                .alpha(if (status.isScanning) pulseAlpha else 0.7f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (status.isScanning) "ACTIVE MONITORING" else "STANDBY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (status.isScanning) DedsecNeonGreen else DedsecTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Header Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DEDSEC // WATCHDOG",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp,
                        color = DedsecTextPrimary
                    )
                    Text(
                        text = "Real-Time Wi-Fi & Bluetooth Spectrum Sentinel",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DedsecTextSecondary
                    )
                }

                // Hits Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (status.totalMatchesDetected > 0) DedsecRedGlow else DedsecCardInner,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (status.totalMatchesDetected > 0) DedsecGlitchRed else DedsecBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HITS",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = DedsecTextSecondary
                        )
                        Text(
                            text = "${status.totalMatchesDetected}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (status.totalMatchesDetected > 0) DedsecGlitchRed else DedsecNeonGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scanner action controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleScan,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("toggle_scan_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status.isScanning) DedsecGlitchRed else DedsecNeonGreen,
                        contentColor = DedsecBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (status.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (status.isScanning) "Stop Scanner" else "Start Scanner",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (status.isScanning) "DISARM SCANNER" else "ARM INTERCEPTOR",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                OutlinedButton(
                    onClick = onOpenSimulation,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("simulate_beacon_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DedsecTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = "Inject Test Signal",
                        modifier = Modifier.size(16.dp),
                        tint = DedsecNeonCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEST BEACON",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Telemetry line in card
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DedsecCardInner,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "> ${status.statusMessage}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DedsecNeonGreen.copy(alpha = 0.9f),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SPECTRUM: ${status.totalSignalsDetected}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DedsecTextSecondary
                    )
                }
            }
        }
    }
}
