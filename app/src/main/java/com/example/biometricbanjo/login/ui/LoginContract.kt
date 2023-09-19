package com.example.biometricbanjo.login.ui

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Immutable
import com.example.biometricbanjo.biometric.model.AuthError
import com.example.biometricbanjo.common.BaseViewModel
import com.example.biometricbanjo.common.UiEventBase
import com.example.biometricbanjo.common.UiStateBase
import com.example.biometricbanjo.common.composable.TextFieldState
import com.example.biometricbanjo.login.model.AuthContext
import kotlinx.coroutines.flow.SharedFlow

interface LoginContract {

    abstract class ViewModel : BaseViewModel<UiEvents, UiState>()

    @Immutable
    data class UiState(
        val isLoading: Boolean,
        val usernameField: TextFieldState = TextFieldState.Empty,
        val passwordField: TextFieldState = TextFieldState.Empty,

        val errorMessage: String? = null,

        val snackbarMessage: SharedFlow<String>,
        /**
         * True when the user is logged in, false otherwise
         */
        val loggedIn: Boolean,
        /**
         * True when we want to render the "access with biometry" button
         */
        val canLoginWithBiometric: Boolean = false,
        /**
         * indicate that we should to show the biometric prompt to the user to enroll
         * the biometric token
         */
        val askBiometricEnrollment: Boolean,
        /**
         * Represent the Authentication context of our prompt
         */
        val authContext: AuthContext? = null,
        /**
         * Represent our biometric prompt
         */
        val promptInfo: BiometricPrompt.PromptInfo? = null,
    ) : UiStateBase

    sealed class UiEvents : UiEventBase {
        object ScreenResumed: UiEvents()
        data class UpdateUsername(val username: String) : UiEvents()
        data class UpdatePassword(val password: String) : UiEvents()
        data class OnAuthSucceeded(val cryptoObject: BiometricPrompt.CryptoObject?) : UiEvents()
        data class OnAuthError(val authError: AuthError) : UiEvents()

        data class LoginClicked(val username: String, val password: String) : UiEvents()
        object LoginWithBiometric : UiEvents()

    }
}
