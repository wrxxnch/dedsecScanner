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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthRepository
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
fun GoogleLoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleSignInClick: () -> Unit,
    onDirectGoogleAuth: (email: String, displayName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var manualEmailInput by remember { mutableStateOf("") }
    var manualNameInput by remember { mutableStateOf("") }
    var showManualPrompt by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "shieldPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DedsecBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("google_login_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecGreenBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DedSec Cyber Shield Emblem
                Surface(
                    shape = CircleShape,
                    color = DedsecGreenGlow,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security Shield",
                            tint = DedsecNeonGreen,
                            modifier = Modifier
                                .size(38.dp)
                                .alpha(pulseAlpha)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "DEDSEC // ACCESS GATEWAY",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    color = DedsecNeonGreen
                )

                Text(
                    text = "AUTENTICAÇÃO OBRIGATÓRIA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = DedsecGlitchRed
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Para utilizar o Scanner de Frequências e Regras de Alerta, é estritamente necessário autenticar-se com sua Conta Google via Firebase Auth.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    color = DedsecTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DedsecCardInner,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                ) {
                    Text(
                        text = "ADMINISTRADOR PRINCIPAL:\n${AuthRepository.PRIMARY_OWNER}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        color = DedsecNeonCyan,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DedsecRedGlow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecGlitchRed)
                    ) {
                        Text(
                            text = errorMessage,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = DedsecGlitchRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        color = DedsecNeonGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conectando ao Google / Firebase...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DedsecNeonGreen
                    )
                } else {
                    // Exclusive Google Sign-In button (NO OWNER LOGIN BUTTON)
                    Button(
                        onClick = {
                            onGoogleSignInClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signin_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        // Google "G" representation
                        Surface(
                            shape = CircleShape,
                            color = DedsecBlack,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = DedsecNeonGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ENTRAR COM CONTA GOOGLE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Google Account Selector (for devices without Play Services login)
                    Button(
                        onClick = { showManualPrompt = !showManualPrompt },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_select_google_account"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DedsecCardInner,
                            contentColor = DedsecNeonCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DedsecNeonCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showManualPrompt) "FECHAR SELEÇÃO" else "DIGITAR E-MAIL GOOGLE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showManualPrompt) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = manualEmailInput,
                            onValueChange = { manualEmailInput = it },
                            label = { Text("E-mail Google (@gmail.com)", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            placeholder = { Text("exemplo@gmail.com", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_email_manual_input"),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = DedsecTextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DedsecNeonGreen,
                                unfocusedBorderColor = DedsecBorder,
                                focusedContainerColor = DedsecCardInner,
                                unfocusedContainerColor = DedsecCardInner
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = manualNameInput,
                            onValueChange = { manualNameInput = it },
                            label = { Text("Nome de exibição (opcional)", fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                            placeholder = { Text("Seu Nome", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_name_manual_input"),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, color = DedsecTextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DedsecNeonGreen,
                                unfocusedBorderColor = DedsecBorder,
                                focusedContainerColor = DedsecCardInner,
                                unfocusedContainerColor = DedsecCardInner
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (manualEmailInput.isNotBlank()) {
                                    onDirectGoogleAuth(manualEmailInput.trim(), manualNameInput.trim())
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("btn_confirm_google_account"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DedsecNeonGreen,
                                contentColor = DedsecBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = manualEmailInput.contains("@")
                        ) {
                            Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTENTICAR CONTA GOOGLE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
