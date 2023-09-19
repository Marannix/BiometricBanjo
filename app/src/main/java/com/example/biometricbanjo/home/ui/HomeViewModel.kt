package com.example.biometricbanjo.home.ui

import androidx.lifecycle.viewModelScope
import com.example.biometricbanjo.domain.user.LogoutUseCase
import com.example.biometricbanjo.domain.user.UserLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val loggedInUseCase: UserLoggedInUseCase
) : HomeContract.ViewModel() {


    override val _uiState = MutableStateFlow(
        initialUiState()
    )

    init {
        viewModelScope.launch {
            loggedInUseCase.invoke().collectLatest { loggedIn ->
                updateUiState { oldState ->
                    oldState.copy(
                        loggedIn = loggedIn
                    )
                }
            }
        }
    }

    override fun onUiEvent(event: HomeContract.UiEvents) {
        when (event) {
            HomeContract.UiEvents.LogoutClicked -> {
                viewModelScope.launch {
                    logoutUseCase.invoke()
                }
            }
        }
    }

    private fun initialUiState(): HomeContract.UiState {
        return HomeContract.UiState(
            loggedIn = loggedInUseCase.invoke().value
        )
    }
}
