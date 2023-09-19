package com.example.biometricbanjo.domain.biometric

import com.example.biometricbanjo.data.biometric.BiometricRepository
import javax.inject.Inject

class GetBiometricInfoUseCase @Inject constructor(private val biometricRepository: BiometricRepository) {

    suspend operator fun invoke() = biometricRepository.getBiometricInfo()
}
