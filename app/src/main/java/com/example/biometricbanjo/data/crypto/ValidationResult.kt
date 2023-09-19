package com.example.biometricbanjo.data.crypto


enum class ValidationResult {
    /**
     * Crypto engine is ready
     */
    OK,

    /**
     * Init of the secret key fails
     */
    KEY_INIT_FAIL,

    /**
     * Security on device change in a way that invalidate our key and we have to create a new one
     */
    KEY_PERMANENTLY_INVALIDATED,

    /**
     * Some unexpected error happen and our crypto layer is not available
     */
    VALIDATION_FAILED
}
