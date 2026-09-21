package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthRepository
import com.example.data.auth.DedsecUser
import com.example.ui.theme.DedsecAmber
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminUsersPanel(
    currentUser: DedsecUser?,
    users: List<DedsecUser>,
    onAddAdmin: (email: String) -> Unit,
    onToggleBlockUser: (email: String, block: Boolean) -> Unit,
    onRemoveUser: (email: String) -> Unit,
    onToggleAdminStatus: (email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newAdminEmailInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter {
            it.email.contains(searchQuery.trim(), ignoreCase = true) ||
                    it.displayName.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Security Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DedsecDarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = DedsecGreenGlow,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Area",
                            tint = DedsecNeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ADMIN CONTROLS // ACCESS ROLES",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DedsecNeonGreen
                    )
                    Text(
                        text = "Owner: ${AuthRepository.PRIMARY_OWNER}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = DedsecTextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DedsecCardInner,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
                ) {
                    Text(
                        text = "${users.size} USERS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = DedsecNeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Admin Email Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DedsecSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = DedsecNeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADICIONAR NOVO ADMINISTRADOR",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DedsecNeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newAdminEmailInput,
                    onValueChange = { newAdminEmailInput = it },
                    placeholder = {
                        Text(
                            "e.g. novo.admin@gmail.com",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = DedsecTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_admin_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = DedsecTextPrimary
                    ),
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
                        if (newAdminEmailInput.isNotBlank()) {
                            onAddAdmin(newAdminEmailInput.trim())
                            newAdminEmailInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_grant_admin"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DedsecNeonGreen,
                        contentColor = DedsecBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = newAdminEmailInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Grant Admin",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONCEDER PRIVILÉGIOS DE ADMIN",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Users List Header & Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "USUÁRIOS CADASTRADOS (${filteredUsers.size})",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = DedsecNeonGreen
            )

            Text(
                text = "Bloqueie ou remova acesso",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = DedsecTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // User list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUsers, key = { it.email }) { user ->
                UserCardItem(
                    user = user,
                    isCurrentUser = user.email.equals(currentUser?.email, ignoreCase = true),
                    isPrimaryOwner = user.email.equals(AuthRepository.PRIMARY_OWNER, ignoreCase = true),
                    onToggleBlock = { onToggleBlockUser(user.email, !user.isBlocked) },
                    onRemove = { onRemoveUser(user.email) },
                    onToggleAdmin = { onToggleAdminStatus(user.email) }
                )
            }
        }
    }
}

@Composable
private fun UserCardItem(
    user: DedsecUser,
    isCurrentUser: Boolean,
    isPrimaryOwner: Boolean,
    onToggleBlock: () -> Unit,
    onRemove: () -> Unit,
    onToggleAdmin: () -> Unit
) {
    val dateFmt = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
    val lastSeen = dateFmt.format(Date(user.lastLoginAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.email}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isBlocked) DedsecDarkSurface.copy(alpha = 0.6f) else DedsecSurfaceCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (user.isBlocked) DedsecGlitchRed.copy(alpha = 0.6f)
            else if (user.isAdmin) DedsecGreenBorder
            else DedsecBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top user info row
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
                        color = when {
                            user.isBlocked -> DedsecRedGlow
                            user.isAdmin -> DedsecGreenGlow
                            else -> DedsecCardInner
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = when {
                                    user.isBlocked -> Icons.Default.Block
                                    user.isAdmin -> Icons.Default.VerifiedUser
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = when {
                                    user.isBlocked -> DedsecGlitchRed
                                    user.isAdmin -> DedsecNeonGreen
                                    else -> DedsecNeonCyan
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.email,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (user.isBlocked) DedsecGlitchRed else DedsecTextPrimary
                            )
                            if (isCurrentUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DedsecGreenGlow
                                ) {
                                    Text(
                                        text = "VOCÊ",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DedsecNeonGreen,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (user.displayName.isNotBlank() && user.displayName != user.email)
                                "${user.displayName} • Visto: $lastSeen"
                            else "Visto: $lastSeen",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = DedsecTextSecondary
                        )
                    }
                }

                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isPrimaryOwner) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = DedsecGreenGlow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecGreenBorder)
                        ) {
                            Text(
                                text = "PRIMARY OWNER",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = DedsecNeonGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (user.isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = DedsecGreenGlow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecGreenBorder)
                        ) {
                            Text(
                                text = "ADMIN",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DedsecNeonGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (user.isBlocked) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = DedsecRedGlow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DedsecGlitchRed)
                        ) {
                            Text(
                                text = "BLOQUEADO",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DedsecGlitchRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Action Buttons Row (only if not primary owner)
            if (!isPrimaryOwner) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Block / Unblock button
                    OutlinedButton(
                        onClick = onToggleBlock,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (user.isBlocked) DedsecNeonGreen else DedsecGlitchRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (user.isBlocked) DedsecNeonGreen.copy(alpha = 0.5f) else DedsecGlitchRed.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = if (user.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (user.isBlocked) "DESBLOQUEAR" else "BLOQUEAR",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Toggle Admin privilege
                    OutlinedButton(
                        onClick = onToggleAdmin,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (user.isAdmin) DedsecAmber else DedsecNeonCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (user.isAdmin) DedsecAmber.copy(alpha = 0.5f) else DedsecBorder
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (user.isAdmin) "REMOVER ADMIN" else "TORNAR ADMIN",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Delete User button
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover Usuário",
                            tint = DedsecGlitchRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
