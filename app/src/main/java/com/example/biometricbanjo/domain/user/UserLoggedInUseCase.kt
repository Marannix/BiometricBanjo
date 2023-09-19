package com.example.biometricbanjo.domain.user

import com.example.biometricbanjo.data.user.UserRepository
import javax.inject.Inject

class UserLoggedInUseCase @Inject constructor(private val userRepository: UserRepository) {

    operator fun invoke() = userRepository.isUserLoggedIn
}
