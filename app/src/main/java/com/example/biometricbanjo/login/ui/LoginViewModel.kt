package com.example.biometricbanjo.login.ui

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.viewModelScope
import com.example.biometricbanjo.R
import com.example.biometricbanjo.biometric.BiometricInfo
import com.example.biometricbanjo.common.CryptoPurpose
import com.example.biometricbanjo.common.mapper.TextFieldStateMapper
import com.example.biometricbanjo.data.crypto.InvalidCryptoLayerException
import com.example.biometricbanjo.domain.biometric.CreateCryptoObjectUseCase
import com.example.biometricbanjo.domain.biometric.DecryptTokenUseCase
import com.example.biometricbanjo.domain.biometric.FetchAndStoreEncryptedTokenUseCase
import com.example.biometricbanjo.domain.biometric.GetBiometricInfoUseCase
import com.example.biometricbanjo.domain.user.LoginUsecase
import com.example.biometricbanjo.domain.user.LoginWithTokenUseCase
import com.example.biometricbanjo.domain.user.UserLoggedInUseCase
import com.example.biometricbanjo.login.domain.PromptInfoMapper
import com.example.biometricbanjo.login.model.AuthContext
import com.example.biometricbanjo.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val textFieldStateMapper: TextFieldStateMapper,
    private val getBiometricInfoUseCase: GetBiometricInfoUseCase,
    private val userLoggedInUseCase: UserLoggedInUseCase,
    private val loginUsecase: LoginUsecase,
    private val createCryptoObjectUseCase: CreateCryptoObjectUseCase,
    private val promptInfoMapper: PromptInfoMapper,
    private val fetchAndStoreEncryptedTokenUseCase: FetchAndStoreEncryptedTokenUseCase,
    private val decryptTokenUseCase: DecryptTokenUseCase,
    private val loginWithTokenUseCase: LoginWithTokenUseCase,
    private val resourceProvider: ResourceProvider,
) : LoginContract.ViewModel() {

    private val _snackbarMessage = MutableSharedFlow<String>()

    override val _uiState = MutableStateFlow(
        initialUiState()
    )

    init {
        viewModelScope.launch {
            userLoggedInUseCase.invoke()
                .map { isLoggedIn -> Pair(isLoggedIn, getBiometricInfoUseCase.invoke()) }
                .collectLatest { info -> updateState(info.first, info.second) }
        }
    }

    override fun onUiEvent(event: LoginContract.UiEvents) {
        when (event) {
            is LoginContract.UiEvents.LoginClicked -> {
                if (textFieldStateMapper.getUsernameState(event.username).isValid()
                    && textFieldStateMapper.getPasswordState(event.password).isValid()
                ) {
                    viewModelScope.launch {
                        makeLoginRequest(event.username, event.password)
                    }
                } else {
                    updateUiState { oldState ->
                        oldState.copy(
                            isLoading = false,
                            errorMessage = resourceProvider.getString(R.string.sign_in_error_message)
                        )
                    }
                }
            }

            is LoginContract.UiEvents.OnAuthError -> {
                viewModelScope.launch {
                    when (event.authError.errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            Timber.i("operation is cancelled by user interaction")
                        }

                        else -> {
                            _snackbarMessage.emit(event.authError.errString)
                        }
                    }
                    updateUiState { oldState ->
                        oldState.copy(
                            askBiometricEnrollment = false, authContext = null
                        )
                    }
                }
            }

            is LoginContract.UiEvents.OnAuthSucceeded -> {
                viewModelScope.launch {
                    Timber.i("On Auth Succeeded $event.cryptoObject")
                    val pendingAuthContext = uiState.value.authContext

                    updateUiState { oldState ->
                        oldState.copy(
                            askBiometricEnrollment = false,
                            authContext = null
                        )
                    }

                    pendingAuthContext?.let { authContext ->
                        event.cryptoObject?.let { cryptoObject ->
                            if (authContext.purpose == CryptoPurpose.Encryption) {
                                startBiometricTokenEnrollment(cryptoObject)
                            } else {
                                startLoginWithToken(cryptoObject)
                            }
                        }
                    }
                }
            }

            is LoginContract.UiEvents.UpdatePassword -> {
                val password = textFieldStateMapper.getPasswordState(event.password)
                updateUiState { oldState ->
                    oldState.copy(
                        errorMessage = "",
                        passwordField = password
                    )
                }
            }

            is LoginContract.UiEvents.UpdateUsername -> {
                val username = textFieldStateMapper.getUsernameState(event.username)
                updateUiState { oldState ->
                    oldState.copy(
                        errorMessage = "",
                        usernameField = username
                    )
                }
            }

            LoginContract.UiEvents.LoginWithBiometric -> {
                requireBiometricLogin()
            }

            LoginContract.UiEvents.ScreenResumed -> {
                if (uiState.value.canLoginWithBiometric) {
                    requireBiometricLogin()
                }
            }
        }
    }

    private suspend fun updateState(isLoggedIn: Boolean, biometricInfo: BiometricInfo) {
        val currentState = uiState.value
        val askBiometricEnrollment =
            shouldAskTokenEnrollment(isLoggedIn, currentState, biometricInfo)
        var authContext: AuthContext? = currentState.authContext
        var promptInfo: BiometricPrompt.PromptInfo? = null

        // we want to check if enrollment is ok or not
        if (askBiometricEnrollment) {
            try {
                val newAuthContext = prepareAuthContext(CryptoPurpose.Encryption)
                authContext = newAuthContext
            } catch (exception: Exception) {
                // In this case we decide to not show an error to the user.
                Timber.e(exception)
            }
        }

        if (authContext != null) {
            promptInfo = promptInfoMapper.invoke(authContext.purpose)
        }

        updateUiState { oldState ->
            oldState.copy(
                loggedIn = isLoggedIn,
                canLoginWithBiometric = canLoginWithBiometricToken(biometricInfo),
                askBiometricEnrollment = askBiometricEnrollment,
                authContext = authContext,
                promptInfo = promptInfo
            )
        }

//        if (uiState.value.canLoginWithBiometric && askBiometricEnrollment) {
//            requireBiometricLogin()
//        }
    }

    private suspend fun prepareAuthContext(purpose: CryptoPurpose): AuthContext {
        val cryptoObject = createCryptoObjectUseCase.invoke(purpose)
        return AuthContext(
            purpose = purpose,
            cryptoObject = cryptoObject
        )
    }

    private fun canLoginWithBiometricToken(biometricInfo: BiometricInfo) =
        (biometricInfo.biometricTokenPresent && biometricInfo.canAskAuthentication())

    private suspend fun startBiometricTokenEnrollment(cryptoObject: BiometricPrompt.CryptoObject) {
        try {
            fetchAndStoreEncryptedTokenUseCase.invoke(cryptoObject)
            Timber.i("fetchAndStoreEncryptedToken done")
        } catch (exception: Exception) {
            if (exception is InvalidCryptoLayerException) {
                handleInvalidCryptoException(exception, false)
            } else {
                handleError(exception)
            }
        }
    }

    private suspend fun startLoginWithToken(cryptoObject: BiometricPrompt.CryptoObject) {
        try {
            val tokenAsCredential = decryptTokenUseCase.invoke(cryptoObject)
            loginWithToken(tokenAsCredential)
            Timber.d("Login Done")
        } catch (exception: Exception) {
            if (exception is InvalidCryptoLayerException) {
                updateUiState { oldState ->
                    oldState.copy(
                        canLoginWithBiometric = false
                    )
                }
            } else {
                handleError(exception)
            }
        }
    }

    private fun loginWithToken(tokenAsCredential: String) {
        viewModelScope.launch {
            delay(100)
            loginWithTokenUseCase.invoke(tokenAsCredential)
        }
    }

    private fun handleInvalidCryptoException(
        ex: InvalidCryptoLayerException,
        isLogin: Boolean
    ) {
        viewModelScope.launch {
            Timber.e(ex, "handleInvalidCryptoException... isLogin: $isLogin")
            if (ex.isKeyPermanentlyInvalidated) {
                _snackbarMessage.emit(resourceProvider.getString(R.string.error_key_permanently_invalidated))
            } else if (ex.isKeyInitFailed) {
                _snackbarMessage.emit(resourceProvider.getString(R.string.error_key_init_fail))
            } else {
                _snackbarMessage.emit(resourceProvider.getString(R.string.error_generic))
            }
            if (isLogin) {
                //update to inform ui that login with biometry is not available
                updateUiState { oldState ->
                    oldState.copy(
                        canLoginWithBiometric = false
                    )
                }
            }
        }
    }

    private fun requireBiometricLogin() {
        viewModelScope.launch {
            try {
                val authContext = prepareAuthContext(CryptoPurpose.Decryption)
                val promptInfo = promptInfoMapper.invoke(authContext.purpose)

                updateUiState { oldState ->
                    oldState.copy(
                        askBiometricEnrollment = false,
                        authContext = authContext,
                        promptInfo = promptInfo,
                    )
                }
            } catch (exception: Exception) {
                if (exception is InvalidCryptoLayerException) {
                    handleInvalidCryptoException(exception, true)
                } else {
                    handleError(exception)
                }
            }
        }
    }

    private fun handleError(exception: Throwable?) {
        viewModelScope.launch {
            Timber.e(exception, "handleException: ${exception?.message}")
            _snackbarMessage.emit(resourceProvider.getString(R.string.error_generic))
        }
    }

    private fun shouldAskTokenEnrollment(
        isLoggedIn: Boolean,
        currentState: LoginContract.UiState,
        biometricInfo: BiometricInfo
    ) = (isLoggedIn && !currentState.askBiometricEnrollment
            && !biometricInfo.biometricTokenPresent
            && biometricInfo.canAskAuthentication())

    /**
     * @param username The user username
     * @param password The user password
     * Makes a login request to login the current user
     * If successful navigate user to Home Screen
     * If error occurs handle error [onLoginRequestError]
     */
    private fun makeLoginRequest(username: String, password: String) {
        viewModelScope.launch {
            updateUiState { oldState -> oldState.copy(isLoading = true, errorMessage = null) }

            try {
                loginUsecase.invoke(username = username, password = password)
            } catch (throwable: Throwable) {
                onLoginError(throwable)
            } finally {
                updateUiState { oldState -> oldState.copy(isLoading = false) }
            }
        }
    }

    /**
     * @param throwable Throwable of error that occurred during login
     * Handle login in error
     */
    private fun onLoginError(throwable: Throwable) {
        viewModelScope.launch {
            _snackbarMessage.emit("onSignUpError: $throwable")
        }
    }

    private fun initialUiState(): LoginContract.UiState {
        return LoginContract.UiState(
            isLoading = false,
            loggedIn = userLoggedInUseCase.invoke().value,
            askBiometricEnrollment = false,
            snackbarMessage = _snackbarMessage.asSharedFlow()
        )
    }
}
