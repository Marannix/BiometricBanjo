package com.example.biometricbanjo.domain.biometric

import com.example.biometricbanjo.common.CryptoPurpose
import com.example.biometricbanjo.data.biometric.BiometricRepository
import javax.inject.Inject

class CreateCryptoObjectUseCase @Inject constructor(private val biometricRepository: BiometricRepository) {

    suspend operator fun invoke(purpose: CryptoPurpose) =
        biometricRepository.createCryptoObject(purpose = purpose)

}
