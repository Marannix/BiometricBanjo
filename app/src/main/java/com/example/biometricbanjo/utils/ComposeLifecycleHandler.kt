package com.example.biometricbanjo.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver


/**
 * Registers an [eventHandler] callback which will be triggered whenever event is [Lifecycle.Event.ON_RESUME]
 * */
@Composable
fun ResumeLifecycleHandler(eventHandler: (Lifecycle.Event) -> Unit) {
    LifecycleEventListener {
        if (it == Lifecycle.Event.ON_RESUME) {
            eventHandler(it)
        }
    }
}


@Composable
fun LifecycleEventListener(eventHandler: (Lifecycle.Event) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(key1 = lifecycle) {
        // Make MapView follow the current lifecycle
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            eventHandler(event)
        }
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}
