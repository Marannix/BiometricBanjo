package com.example.biometricbanjo.domain.user

import com.example.biometricbanjo.data.user.UserRepository
import javax.inject.Inject

class LoginWithTokenUseCase @Inject constructor(private val userRepository: UserRepository) {

    suspend operator fun invoke(token: String) = userRepository.loginWithToken(token)
}