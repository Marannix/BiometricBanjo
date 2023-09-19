package com.example.biometricbanjo.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<E : UiEventBase, S : UiStateBase> : ViewModel() {

    abstract fun onUiEvent(event: E)

    protected open val _uiState: MutableStateFlow<S>
        get() = throw IllegalArgumentException("Not initialized")

    open val uiState: StateFlow<S> by lazy { _uiState }

    protected fun updateUiState(updateBlock: (oldState: S) -> S) {
        val newState = updateBlock(_uiState.value)
        _uiState.tryEmit(newState)
    }
}
