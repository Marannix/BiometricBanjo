package com.example.biometricbanjo.login.model

import androidx.biometric.BiometricPrompt
import com.example.biometricbanjo.common.CryptoPurpose

data class AuthContext(
    val purpose: CryptoPurpose,
    val cryptoObject: BiometricPrompt.CryptoObject
)
