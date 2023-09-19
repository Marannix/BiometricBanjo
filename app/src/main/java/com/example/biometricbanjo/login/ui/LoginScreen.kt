@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class
)

package com.example.biometricbanjo.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import com.example.biometricbanjo.R
import com.example.biometricbanjo.biometric.ui.BiometricPromptContainer
import com.example.biometricbanjo.biometric.ui.BiometricPromptContainerState
import com.example.biometricbanjo.common.composable.CallToActionButton
import com.example.biometricbanjo.common.composable.PasswordContent
import com.example.biometricbanjo.common.composable.UsernameContent
import com.example.biometricbanjo.main.navigation.Destinations
import com.example.biometricbanjo.utils.ResumeLifecycleHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navHostController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        navHostController = navHostController,
        uiState = uiState,
        uiEvents = viewModel::onUiEvent
    )

    ResumeLifecycleHandler {
        viewModel.onUiEvent(LoginContract.UiEvents.ScreenResumed)
    }
}

@Composable
fun LoginContent(
    navHostController: NavHostController,
    uiState: LoginContract.UiState,
    uiEvents: (LoginContract.UiEvents) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val shouldNavigateToHome by remember(uiState) {
        derivedStateOf { uiState.loggedIn && !uiState.askBiometricEnrollment }
    }

    val promptContainerState = remember { BiometricPromptContainerState() }
    val snackbarHostState = remember { SnackbarHostState() }

    if (shouldNavigateToHome) {
        LaunchedEffect(key1 = Unit) {
            navHostController.navigate(
                route = Destinations.Home.name,
                navOptions = NavOptions
                    .Builder()
                    .setPopUpTo(Destinations.Login.name, true)
                    .build()
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        style = typography.titleLarge,
                        text = "Login",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { contentPadding ->

            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage
                    .collectLatest { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
            }

            LoginBiometricPromptContainer(uiEvents, promptContainerState)

            uiState.authContext?.let { auth ->
                LaunchedEffect(key1 = auth) {
                    uiState.promptInfo?.let { promptInfo ->
                        promptContainerState.authenticate(promptInfo, auth.cryptoObject)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .padding(top = 32.dp),
                horizontalAlignment = CenterHorizontally,
            ) {

                ErrorBanner(uiState)

                Spacer(modifier = Modifier.height(8.dp))

                UsernameContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    usernameState = uiState.usernameField,
                    onValueChange = { uiEvents(LoginContract.UiEvents.UpdateUsername(it)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PasswordContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    passwordState = uiState.passwordField,
                    onValueChange = { uiEvents(LoginContract.UiEvents.UpdatePassword(it)) },
                    onDone = {
                            focusManager.clearFocus(force = true)
                            uiEvents(
                                LoginContract.UiEvents.LoginClicked(
                                    uiState.usernameField.text,
                                    uiState.passwordField.text,
                                )
                            )
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CallToActionButton(
                    label = stringResource(id = R.string.login)
                ) {
                    focusManager.clearFocus(force = true)
                    uiEvents(
                        LoginContract.UiEvents.LoginClicked(
                            uiState.usernameField.text,
                            uiState.passwordField.text
                        )
                    )
                }

                if (uiState.canLoginWithBiometric) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CallToActionButton(
                        label = stringResource(id = R.string.login_with_biometric)
                    ) {
                        focusManager.clearFocus(force = true)
                        uiEvents(
                            LoginContract.UiEvents.LoginWithBiometric
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ErrorBanner(uiState: LoginContract.UiState) {
    if (!uiState.errorMessage.isNullOrBlank()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = uiState.errorMessage,
            color = MaterialTheme.colorScheme.onBackground,
            style = typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoginBiometricPromptContainer(
    uiEvents: (LoginContract.UiEvents) -> Unit,
    promptContainerState: BiometricPromptContainerState
) {
    BiometricPromptContainer(
        promptContainerState,
        onAuthSucceeded = { cryptoObject ->
            uiEvents(LoginContract.UiEvents.OnAuthSucceeded(cryptoObject))
        },
        onAuthError = { authError ->
            uiEvents(LoginContract.UiEvents.OnAuthError(authError))
        },
    )
}
