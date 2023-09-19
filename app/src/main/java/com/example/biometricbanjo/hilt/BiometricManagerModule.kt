package com.example.biometricbanjo.hilt

import android.content.Context
import androidx.biometric.BiometricManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BiometricManagerModule {

    @Provides
    @Singleton
    fun provideBiometricRepository(
        @ApplicationContext context: Context,
    ): BiometricManager {
        return BiometricManager.from(context)
    }
}
