package com.example.biometricbanjo.common.composable

sealed class TextFieldState(open val text: String) {
    object Empty : TextFieldState("")
    data class Valid(override val text: String) : TextFieldState(text)
    data class Invalid(override val text: String, val errorMessage: String) : TextFieldState(text)

    fun isInvalid(): Boolean = this is Invalid

    fun isValid(): Boolean = this is Valid

    fun errorMessageOrNull(): String? {
        return try {
            (this as Invalid).errorMessage
        } catch (throwable: Throwable) {
            null
        }
    }
}
