package com.apiguave.tinderclonecompose.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLoginOn: () -> Unit
) {
    val loginViewModel: LoginViewModel = koinViewModel()
    val uiState by loginViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState, block = {
        if (uiState.isUserSignedIn) {
            onNavigateToHome()
        }
    })

    val startForResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = loginViewModel::signIn
    )
    val (showLoginScreen, setShowLoginScreen) = remember { mutableStateOf(false) }

    LoginView(
        uiState = uiState,
        onNavigateToSignUp = onNavigateToSignUp,
        onLoginOnClicked = onNavigateToLoginOn
    )
}