package com.example.biometricbanjo.common.mapper

import com.example.biometricbanjo.common.composable.TextFieldState
import com.example.biometricbanjo.utils.FieldValidationResult
import com.example.biometricbanjo.utils.TextFieldValidator
import javax.inject.Inject

class TextFieldStateMapper @Inject constructor(
    private val textFieldValidator: TextFieldValidator,
) {

    fun getUsernameState(name: String): TextFieldState {
        val usernameState = when (textFieldValidator.validateUsername(name)) {
            FieldValidationResult.EMPTY -> TextFieldState.Invalid(
                name,
                "Yikes! No username provided."
            )

            FieldValidationResult.INVALID -> TextFieldState.Invalid(
                name,
                "Oops! Please enter a valid username."
            )

            FieldValidationResult.VALID -> TextFieldState.Valid(name)
        }

        return usernameState
    }

    fun getPasswordState(password: String): TextFieldState {
        val passwordState = when (textFieldValidator.validatePassword(password)) {
            FieldValidationResult.EMPTY -> TextFieldState.Invalid(
                password,
                "Oops! I think you forgot to enter a password"
            )
            FieldValidationResult.INVALID -> TextFieldState.Invalid(
                password,
                "Oops! Please enter a valid password"
            )
            FieldValidationResult.VALID -> TextFieldState.Valid(password)
        }

        return passwordState
    }
}
