package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.update.UpdateState
import com.example.ui.components.AddTargetDialog
import com.example.ui.components.AdminUsersPanel
import com.example.ui.components.AlertConfigView
import com.example.ui.components.BulkImportDialog
import com.example.ui.components.DedsecHeader
import com.example.ui.components.EditTargetRuleDialog
import com.example.ui.components.MandatoryUpdateDialog
import com.example.ui.components.RadarView
import com.example.ui.components.SimulationDialog
import com.example.ui.components.TargetsView
import com.example.ui.components.TerminalView
import com.example.ui.theme.DedsecBlack
import com.example.ui.theme.DedsecBorder
import com.example.ui.theme.DedsecDarkSurface
import com.example.ui.theme.DedsecGlitchRed
import com.example.ui.theme.DedsecNeonCyan
import com.example.ui.theme.DedsecNeonGreen
import com.example.ui.theme.DedsecSurfaceCard
import com.example.ui.theme.DedsecTextPrimary
import com.example.ui.theme.DedsecTextSecondary

@Composable
fun DedsecMainScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scannerStatus by viewModel.scannerStatus.collectAsState()
    val detectedSignals by viewModel.detectedSignals.collectAsState()
    val matchedSignals by viewModel.matchedSignals.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Toast / Snackbar notification
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Permission request launcher
    val permissionsToRequest = remember {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(Manifest.permission.BLUETOOTH_SCAN)
            list.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
            list.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        list.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        viewModel.scannerManager.logTerminal(
            if (granted) "PERMISSIONS // All sensory authorizations GRANTED"
            else "PERMISSIONS // Some authorizations restricted. Simulation and partial scanning active.",
            isAlert = false
        )
    }

    LaunchedEffect(Unit) {
        val hasMissing = permissionsToRequest.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (hasMissing) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // File pickers
    val textFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromFileUri(it) }
    }

    val wigleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromFileUri(it) }
    }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setCustomAudioUri(it) }
    }

    var targetRuleIdForAudioPicker by remember { mutableStateOf<String?>(null) }
    val ruleAudioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        val ruleId = targetRuleIdForAudioPicker
        if (uri != null && ruleId != null) {
            viewModel.importAudioForRule(ruleId, uri)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DedsecBlack),
        containerColor = DedsecBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DedsecHeader(
                status = scannerStatus,
                currentUser = currentUser,
                onToggleScan = { viewModel.toggleScanning() },
                onOpenSimulation = { viewModel.setDialogVisible(sim = true) },
                onSignOut = { viewModel.signOut() },
                onCheckUpdate = { viewModel.checkForGitHubUpdates(mandatory = true) },
                onOpenAdmin = { viewModel.selectTab(4) }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = DedsecDarkSurface,
                tonalElevation = 0.dp
            ) {
                val baseNavItems = mutableListOf(
                    Triple(0, "RADAR", Icons.Default.Radar),
                    Triple(1, "TARGETS", Icons.Default.FormatListBulleted),
                    Triple(2, "ALERT", Icons.Default.Vibration),
                    Triple(3, "CONSOLE", Icons.Default.Terminal)
                )

                if (currentUser?.isAdmin == true) {
                    baseNavItems.add(Triple(4, "ADMIN", Icons.Default.AdminPanelSettings))
                }

                baseNavItems.forEach { (index, label, icon) ->
                    val selected = uiState.selectedTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (selected) DedsecNeonGreen else DedsecTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = if (selected) DedsecNeonGreen else DedsecTextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DedsecNeonGreen,
                            unselectedIconColor = DedsecTextSecondary,
                            selectedTextColor = DedsecNeonGreen,
                            unselectedTextColor = DedsecTextSecondary,
                            indicatorColor = com.example.ui.theme.DedsecGreenGlow
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(targetState = uiState.selectedTab, label = "tabTransition") { tab ->
                when (tab) {
                    0 -> RadarView(
                        signals = detectedSignals,
                        matchedSignals = matchedSignals,
                        isScanning = scannerStatus.isScanning,
                        onInjectTestSignal = { viewModel.setDialogVisible(sim = true) },
                        onSendToRules = { signal, patternType ->
                            viewModel.sendSignalToRules(signal, patternType)
                        }
                    )
                    1 -> TargetsView(
                        rules = uiState.rules,
                        onAddRuleClick = { viewModel.setDialogVisible(add = true) },
                        onBulkImportClick = { viewModel.setDialogVisible(bulk = true) },
                        onPickFileClick = { textFilePicker.launch(arrayOf("*/*", "text/*")) },
                        onPickWigleFileClick = { wigleFilePicker.launch(arrayOf("*/*", "text/csv", "text/plain")) },
                        onToggleRule = { viewModel.toggleRule(it) },
                        onDeleteRule = { viewModel.deleteRule(it) },
                        onClearAll = { viewModel.clearAllRules() },
                        onEditRule = { viewModel.openEditRule(it) },
                        onTestRule = { viewModel.testRule(it) }
                    )
                    2 -> AlertConfigView(
                        config = uiState.config,
                        recentSounds = uiState.recentSounds,
                        isTesting = uiState.isTestingAlert,
                        onUpdateConfig = { viewModel.updateConfig(it) },
                        onTestAlert = { viewModel.testCurrentAlert() },
                        onPickAudioFile = { audioFilePicker.launch("audio/*") },
                        onSelectRecentSound = { viewModel.selectRecentSoundForConfig(it) },
                        onDeleteRecentSound = { viewModel.deleteRecentSound(it) },
                        onPreviewSound = { viewModel.previewSound(it) },
                        onTestTts = { text, pitch, speed -> viewModel.testTts(text, pitch, speed) },
                        onStopVibration = { viewModel.stopVibration() }
                    )
                    3 -> TerminalView(
                        logs = terminalLogs,
                        onClearLogs = { viewModel.clearLogs() }
                    )
                    4 -> AdminUsersPanel(
                        currentUser = currentUser,
                        users = allUsers,
                        onAddAdmin = { email -> viewModel.addAdminEmail(email) },
                        onToggleBlockUser = { email, block -> viewModel.toggleBlockUser(email, block) },
                        onRemoveUser = { email -> viewModel.removeUser(email) },
                        onToggleAdminStatus = { email -> viewModel.toggleAdminPrivilege(email) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAddDialog) {
        AddTargetDialog(
            onDismiss = { viewModel.setDialogVisible(add = false) },
            onAdd = { pattern, type, alertMode, morse, notifTitle, notifMsg ->
                viewModel.addRule(
                    pattern = pattern,
                    type = type,
                    alertMode = alertMode,
                    morsePattern = morse,
                    notificationTitle = notifTitle,
                    notificationMessage = notifMsg
                )
            }
        )
    }

    uiState.editingRule?.let { ruleToEdit ->
        EditTargetRuleDialog(
            rule = ruleToEdit,
            recentSounds = uiState.recentSounds,
            onDismiss = { viewModel.closeEditRule() },
            onSave = { updatedRule -> viewModel.saveEditedRule(updatedRule) },
            onPickAudioFile = {
                targetRuleIdForAudioPicker = ruleToEdit.id
                ruleAudioFilePicker.launch("audio/*")
            },
            onPreviewSound = { uriString ->
                viewModel.previewSound(uriString)
            },
            onTestTts = { text, pitch, speed ->
                viewModel.testTts(text, pitch, speed)
            },
            onTestRule = { testTarget -> viewModel.testRule(testTarget) }
        )
    }

    if (uiState.showBulkImportDialog) {
        BulkImportDialog(
            onDismiss = { viewModel.setDialogVisible(bulk = false) },
            onImport = { viewModel.importFromText(it) }
        )
    }

    if (uiState.showSimulationDialog) {
        SimulationDialog(
            onDismiss = { viewModel.setDialogVisible(sim = false) },
            onInject = { type, name, address, rssi ->
                viewModel.injectSimulation(type, name, address, rssi)
            }
        )
    }

    // Mandatory / Non-Mandatory GitHub Update Dialog
    MandatoryUpdateDialog(
        updateState = updateState,
        currentVersion = viewModel.updateManager.currentVersionName,
        onDownloadAndInstall = { release ->
            viewModel.downloadAndInstallUpdate(release)
        },
        onCheckAgain = {
            viewModel.checkForGitHubUpdates(mandatory = true)
        },
        onDismissNonMandatory = {
            viewModel.dismissNonMandatoryUpdate()
        },
        onOpenUrl = { url ->
            viewModel.openBrowserUrl(url)
        }
    )
}
