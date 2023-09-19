package com.example.biometricbanjo.home.ui

import androidx.compose.runtime.Immutable
import com.example.biometricbanjo.common.BaseViewModel
import com.example.biometricbanjo.common.UiEventBase
import com.example.biometricbanjo.common.UiStateBase


interface HomeContract {

    abstract class ViewModel : BaseViewModel<UiEvents, UiState>()

    @Immutable
    data class UiState(
        val loggedIn : Boolean,
    ) : UiStateBase

    sealed class UiEvents : UiEventBase {
        object LogoutClicked : UiEvents()
    }
}
