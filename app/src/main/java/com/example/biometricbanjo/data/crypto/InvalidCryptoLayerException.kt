package com.example.biometricbanjo.data.crypto

class InvalidCryptoLayerException(validationResult: ValidationResult) : Exception() {

    val isKeyPermanentlyInvalidated = validationResult == ValidationResult.KEY_PERMANENTLY_INVALIDATED

    val isKeyInitFailed = validationResult == ValidationResult.KEY_INIT_FAIL
}
