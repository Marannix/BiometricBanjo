package com.example.biometricbanjo.domain.user

import com.example.biometricbanjo.data.user.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val userRepository: UserRepository) {

    suspend operator fun invoke() = userRepository.logout()

}