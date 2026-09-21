package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.auth.GoogleSignInHelper
import com.example.ui.DedsecMainScreen
import com.example.ui.ScannerViewModel
import com.example.ui.components.GoogleLoginScreen
import com.example.ui.components.MandatoryUpdateDialog
import com.example.ui.theme.DedsecBlack
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val googleSignInHelper by lazy { GoogleSignInHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DedsecBlack
                ) {
                    val scannerViewModel: ScannerViewModel = viewModel()
                    val currentUser by scannerViewModel.currentUser.collectAsState()
                    val uiState by scannerViewModel.uiState.collectAsState()
                    val updateState by scannerViewModel.updateState.collectAsState()
                    val scope = rememberCoroutineScope()

                    // Auto-check GitHub update on launch (mandatory as requested)
                    LaunchedEffect(Unit) {
                        scannerViewModel.checkForGitHubUpdates(mandatory = true)
                    }

                    // Enforce Google Login: if not logged in, user CANNOT use the app
                    Crossfade(
                        targetState = currentUser,
                        label = "auth_gate_transition"
                    ) { user ->
                        if (user == null) {
                            GoogleLoginScreen(
                                isLoading = uiState.isAuthLoading,
                                errorMessage = uiState.authErrorMessage,
                                onGoogleSignInClick = {
                                    scope.launch {
                                        val (success, cred) = googleSignInHelper.signInWithGoogle()
                                        if (success && cred != null) {
                                            scannerViewModel.authenticateGoogleUser(cred.first, cred.second)
                                        } else {
                                            // Fallback prompt guidance
                                            scannerViewModel.clearToast()
                                        }
                                    }
                                },
                                onDirectGoogleAuth = { email, displayName ->
                                    scannerViewModel.authenticateGoogleUser(email, displayName)
                                }
                            )
                        } else {
                            DedsecMainScreen(viewModel = scannerViewModel)
                        }
                    }

                    // Always display update dialog if update check returns a pending update
                    MandatoryUpdateDialog(
                        updateState = updateState,
                        currentVersion = scannerViewModel.updateManager.currentVersionName,
                        onDownloadAndInstall = { release ->
                            scannerViewModel.downloadAndInstallUpdate(release)
                        },
                        onCheckAgain = {
                            scannerViewModel.checkForGitHubUpdates(mandatory = true)
                        },
                        onDismissNonMandatory = {
                            scannerViewModel.dismissNonMandatoryUpdate()
                        },
                        onOpenUrl = { url ->
                            scannerViewModel.openBrowserUrl(url)
                        }
                    )
                }
            }
        }
    }
}

