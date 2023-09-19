package com.example.biometricbanjo.login.domain

import androidx.biometric.BiometricPrompt
import com.example.biometricbanjo.R
import com.example.biometricbanjo.common.CryptoPurpose
import com.example.biometricbanjo.utils.ResourceProvider
import javax.inject.Inject

class PromptInfoMapper @Inject constructor(private val resourceProvider: ResourceProvider) {

    operator fun invoke(purpose: CryptoPurpose): BiometricPrompt.PromptInfo {
        return if (purpose == CryptoPurpose.Encryption) {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(resourceProvider.getString(R.string.prompt_title_enroll_token))
                .setSubtitle(resourceProvider.getString(R.string.prompt_subtitle_enroll_token))
                .setNegativeButtonText(resourceProvider.getString(R.string.prompt_cancel))
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(resourceProvider.getString(R.string.prompt_title_login))
                .setSubtitle(resourceProvider.getString(R.string.prompt_subtitle_login))
                .setNegativeButtonText(resourceProvider.getString(R.string.prompt_cancel))
                .build()
        }
    }
}