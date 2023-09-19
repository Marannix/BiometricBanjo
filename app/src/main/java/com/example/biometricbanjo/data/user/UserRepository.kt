package com.example.biometricbanjo.data.user

import kotlinx.coroutines.flow.StateFlow

/**
 * Represent the repository for our user
 */
interface UserRepository {

    /**
     * Flow that contains if the user is logged in or not
     */
    val isUserLoggedIn: StateFlow<Boolean>

    /**
     * Login user
     */
    suspend fun login(username: String, password: String)

    /**
     * Login user using biometric
     */
    suspend fun loginWithToken(token: String)

    /**
     * Logout user
     */
    suspend fun logout()
}
