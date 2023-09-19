package com.example.biometricbanjo.utils

import java.util.regex.Pattern
import javax.inject.Inject

class TextFieldValidator @Inject constructor() {

    /**
     *  @param username Username to be validated
     *  Return [FieldValidationResult] based on validation
     */
    fun validateUsername(username: String): FieldValidationResult {
        return when {
            username.isEmpty() -> FieldValidationResult.EMPTY
            else -> FieldValidationResult.VALID
        }
    }

    /**
     *  @param password Password to be validated
     *  Used to validate user password during sign in flow
     *  Return [FieldValidationResult]
     */
    fun validatePassword(password: String?): FieldValidationResult {
        return when {
            password.isNullOrEmpty() -> FieldValidationResult.EMPTY
            else -> FieldValidationResult.VALID
        }
    }

    /**
     *  @param password Password to be validated
     *  Used to validate user password during sign up flow
     *  Return [FieldValidationResult]
     */
    fun validateCreatePassword(password: String?): FieldValidationResult {
        return when {
            password.isNullOrEmpty() -> FieldValidationResult.EMPTY
            !VALID_PASSWORD_REGEX.matcher(password).find() -> FieldValidationResult.INVALID
            else -> FieldValidationResult.VALID
        }
    }

    companion object {
        private val VALID_PASSWORD_REGEX = Pattern.compile("^.{8,}$")
    }
}
