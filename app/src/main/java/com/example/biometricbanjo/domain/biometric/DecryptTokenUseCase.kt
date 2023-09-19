package com.example.biometricbanjo.domain.biometric

import androidx.biometric.BiometricPrompt
import com.example.biometricbanjo.data.biometric.BiometricRepository
import javax.inject.Inject

class DecryptTokenUseCase @Inject constructor(private val biometricRepository: BiometricRepository) {

    suspend operator fun invoke(cryptoObject: BiometricPrompt.CryptoObject) = biometricRepository.decryptToken(cryptoObject)
}