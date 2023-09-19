package com.example.biometricbanjo.data.biometric

import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import com.example.biometricbanjo.biometric.BiometricAuthStatus
import com.example.biometricbanjo.biometric.BiometricInfo
import com.example.biometricbanjo.common.CryptoPurpose
import com.example.biometricbanjo.data.crypto.CryptoEngine
import com.example.biometricbanjo.data.crypto.InvalidCryptoLayerException
import com.example.biometricbanjo.data.crypto.ValidationResult
import com.example.biometricbanjo.data.storage.KeyValueStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class BiometricRepositoryImpl @Inject constructor(
    private val biometricManager: BiometricManager,
    private val keyValueStorage: KeyValueStorage,
    private val cryptoEngine: CryptoEngine,
) : BiometricRepository {

    override suspend fun getBiometricInfo(): BiometricInfo = withContext(Dispatchers.IO) {
        val isBiometricTokenPresent = isTokenPresent()
        val biometricAuthStatus = readBiometricAuthStatus()
        val cryptoValidationResult = checkInternalWithCrypto()

        BiometricInfo(
            biometricTokenPresent = isBiometricTokenPresent,
            biometricAuthStatus = biometricAuthStatus,
            keyStatus = when (cryptoValidationResult) {
                ValidationResult.OK -> BiometricInfo.KeyStatus.READY
                ValidationResult.KEY_INIT_FAIL, ValidationResult.VALIDATION_FAILED -> BiometricInfo.KeyStatus.NOT_READY
                ValidationResult.KEY_PERMANENTLY_INVALIDATED -> BiometricInfo.KeyStatus.INVALIDATED
            }
        )
    }

    private suspend fun checkInternalWithCrypto(): ValidationResult = withContext(Dispatchers.IO) {
        val validationResult = cryptoEngine.validate()
        when (validationResult) {
            ValidationResult.KEY_PERMANENTLY_INVALIDATED,
            ValidationResult.KEY_INIT_FAIL -> {
                // Delete data immediately is a policy that we have decided to implement: you have to always
                // notify this condition to the user
                clearCryptoAndData()
            }

            else -> {
                // Do nothing
            }
        }
        validationResult
    }

    override suspend fun fetchAndStoreEncryptedToken(
        cryptoObject: BiometricPrompt.CryptoObject
    ) = withContext(Dispatchers.IO) {
        validateCryptoLayer()
        // 1. fetch the token from our backend
        val token = getTokenFromBackend()
        // 2. encrypt the data using the cipher inside the cryptoObject
        val encryptedData = cryptoEngine.encrypt(token, cryptoObject)
        // 3. Store encrypted data and iv.
        storeDataAndIv(encryptedData.data, encryptedData.iv!!)
    }

    override suspend fun createCryptoObject(purpose: CryptoPurpose): BiometricPrompt.CryptoObject =
        withContext(Dispatchers.IO) {
            validateCryptoLayer()
            val iv: ByteArray? = when (purpose) {
                CryptoPurpose.Decryption -> {
                    Base64.decode(keyValueStorage.getValue(BIOMETRIC_IV_KEY), Base64.DEFAULT)
                }

                else -> null
            }
            cryptoEngine.createCryptoObject(purpose, iv)
        }

    override suspend fun decryptToken(cryptoObject: BiometricPrompt.CryptoObject): String {
        validateCryptoLayer()
        // 1. read encrypted token (string base64 encoded)
        val encToken = keyValueStorage.getValue(BIOMETRIC_TOKEN_KEY)
        // 2. decode token data on byteArray
        val encTokenData = Base64.decode(encToken, Base64.DEFAULT)
        // 3. decrypt token via cryptoEngine (using cipher inside cryptoObject
        return cryptoEngine.decrypt(encTokenData, cryptoObject)
    }


    override suspend fun clear() {
        keyValueStorage.clear()
    }

    private fun storeDataAndIv(encryptedData: ByteArray, iv: ByteArray) {
        val dataBase64 = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
        keyValueStorage.storeValue(BIOMETRIC_TOKEN_KEY, dataBase64)
        keyValueStorage.storeValue(BIOMETRIC_IV_KEY, ivBase64)
    }

    private fun readBiometricAuthStatus() =
        when (biometricManager.canAuthenticate(requiredAuthenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthStatus.NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAuthStatus.TEMPORARY_NOT_AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthStatus.AVAILABLE_BUT_NOT_ENROLLED
            else -> BiometricAuthStatus.NOT_AVAILABLE
        }

    private fun isTokenPresent(): Boolean {
        return keyValueStorage.contains(key = BIOMETRIC_TOKEN_KEY) && keyValueStorage.contains(
            BIOMETRIC_IV_KEY
        )
    }

    /**
     * This is a mock token, ideally the token should be retrieved from the server
     */
    private fun getTokenFromBackend(): String {
        val token = UUID.randomUUID().toString()
        keyValueStorage.storeValue(MOCK_TOKEN_KEY, token)
        return token
    }


    /**
     * Validate the crypto layer. In case of invalid status, this method
     * throws an [InvalidCryptoLayerException]
     */
    private suspend fun validateCryptoLayer() {
        val status = checkInternalWithCrypto()
        if (status != ValidationResult.OK) {
            throw InvalidCryptoLayerException(status)
        }
    }

    private fun clearCryptoAndData() {
        cryptoEngine.clear()
        keyValueStorage.clear()
    }

    companion object {
        const val requiredAuthenticators: Int = BIOMETRIC_STRONG
        const val BIOMETRIC_TOKEN_KEY = "BIOMETRIC_TOKEN"
        const val BIOMETRIC_IV_KEY = "BIOMETRIC_TOKEN_IV"
        const val MOCK_TOKEN_KEY = "mockTokenForFakeAuthValidation"
    }
}
