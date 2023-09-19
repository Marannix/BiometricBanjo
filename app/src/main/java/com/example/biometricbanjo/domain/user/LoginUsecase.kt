package com.example.biometricbanjo.domain.user

import com.example.biometricbanjo.data.user.UserRepository
import javax.inject.Inject

class LoginUsecase @Inject constructor(private val userRepository: UserRepository) {

    suspend operator fun invoke(username: String, password: String) =
        userRepository.login(username = username, password = password)
}