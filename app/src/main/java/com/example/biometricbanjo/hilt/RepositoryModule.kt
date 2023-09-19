package com.example.biometricbanjo.hilt

import androidx.biometric.BiometricManager
import com.example.biometricbanjo.data.biometric.BiometricRepository
import com.example.biometricbanjo.data.biometric.BiometricRepositoryImpl
import com.example.biometricbanjo.data.crypto.CryptoEngine
import com.example.biometricbanjo.data.storage.KeyValueStorage
import com.example.biometricbanjo.data.user.UserRepository
import com.example.biometricbanjo.data.user.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        keyValueStorage: KeyValueStorage
    ): UserRepository {
        return UserRepositoryImpl(keyValueStorage)
    }

    @Provides
    @Singleton
    fun provideBiometricRepository(
        biometricManager: BiometricManager,
        keyValueStorage: KeyValueStorage,
        cryptoEngine: CryptoEngine,
    ): BiometricRepository {
        return BiometricRepositoryImpl(
            biometricManager,
            keyValueStorage,
            cryptoEngine,
        )
    }
}
