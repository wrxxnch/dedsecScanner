package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.update.GitHubReleaseInfo
import com.example.data.update.UpdateState
import com.example.ui.theme.DedsecBlack
import com.example.ui.theme.DedsecBorder
import com.example.ui.theme.DedsecCardInner
import com.example.ui.theme.DedsecDarkSurface
import com.example.ui.theme.DedsecGlitchRed
import com.example.ui.theme.DedsecGreenBorder
import com.example.ui.theme.DedsecGreenGlow
import com.example.ui.theme.DedsecNeonCyan
import com.example.ui.theme.DedsecNeonGreen
import com.example.ui.theme.DedsecRedGlow
import com.example.ui.theme.DedsecSurfaceCard
import com.example.ui.theme.DedsecTextPrimary
import com.example.ui.theme.DedsecTextSecondary

@Composable
fun MandatoryUpdateDialog(
    updateState: UpdateState,
    currentVersion: String,
    onDownloadAndInstall: (GitHubReleaseInfo) -> Unit,
    onCheckAgain: () -> Unit,
    onDismissNonMandatory: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val release = (updateState as? UpdateState.UpdateAvailable)?.release
        ?: (updateState as? UpdateState.Downloading)?.let { null }

    val isMandatory = (updateState as? UpdateState.UpdateAvailable)?.mandatory ?: true

    Dialog(
        onDismissRequest = {
            if (!isMandatory) onDismissNonMandatory()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory,
            dismissOnClickOutside = !isMandatory
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mandatory_update_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = DedsecDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, DedsecGlitchRed)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning / Update icon with pulsing aura
                Surface(
                    shape = CircleShape,
                    color = DedsecRedGlow,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = "Update Icon",
                            tint = DedsecGlitchRed,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ATUALIZAÇÃO OBRIGATÓRIA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = DedsecGlitchRed,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "GITHUB SYNC // SECURITY PROTOCOL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = DedsecNeonGreen,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (updateState) {
                    is UpdateState.UpdateAvailable -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = DedsecCardInner,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "VERSÃO ATUAL: v$currentVersion",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = DedsecTextSecondary
                                    )
                                    Text(
                                        text = "NOVA: v${updateState.release.tagName}",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = DedsecNeonGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = updateState.release.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DedsecTextPrimary
                                )

                                if (updateState.release.body.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = updateState.release.body,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        color = DedsecTextSecondary,
                                        maxLines = 4
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Para continuar utilizando o scanner, é obrigatório atualizar o aplicativo com os binários do repositório GitHub.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DedsecTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onDownloadAndInstall(updateState.release) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_install_update"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DedsecNeonGreen,
                                contentColor = DedsecBlack
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BAIXAR & INSTALAR AGORA",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onOpenUrl(updateState.release.downloadUrl) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DedsecNeonCyan)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ABRIR NO NAVEGADOR (GITHUB)", fontFamily = FontFamily.Monospace, fontSize = 10.5.sp)
                        }
                    }

                    is UpdateState.Downloading -> {
                        Text(
                            text = "Baixando atualização do GitHub...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = DedsecTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { updateState.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = DedsecNeonGreen,
                            trackColor = DedsecCardInner
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${updateState.progressPercent}%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DedsecNeonGreen
                        )
                    }

                    is UpdateState.DownloadComplete -> {
                        Text(
                            text = "Download concluído! Iniciando o instalador do Android...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DedsecNeonGreen,
                            textAlign = TextAlign.Center
                        )
                    }

                    is UpdateState.Error -> {
                        Text(
                            text = updateState.message,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DedsecGlitchRed,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onCheckAgain,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DedsecCardInner,
                                contentColor = DedsecNeonCyan
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TENTAR NOVAMENTE", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
