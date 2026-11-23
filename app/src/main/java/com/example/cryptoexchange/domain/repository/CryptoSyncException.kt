package com.example.cryptoexchange.domain.repository

sealed class CryptoSyncException(
    message: String,
    val isRetryable: Boolean = true,
    cause: Throwable? = null
) : Exception(message, cause) {

    class Temporary(
        message: String = "Тимчасова помилка синхронізації",
        cause: Throwable? = null
    ) : CryptoSyncException(message = message, isRetryable = true, cause = cause)

    class RateLimit(
        message: String = "Перевищено ліміт запитів до API",
        cause: Throwable? = null
    ) : CryptoSyncException(message = message, isRetryable = true, cause = cause)

    class Unauthorized(
        message: String = "Помилка ключа або прав доступу до API",
        cause: Throwable? = null
    ) : CryptoSyncException(message = message, isRetryable = false, cause = cause)

    class InvalidResponse(
        message: String = "Некоректна відповідь від API",
        cause: Throwable? = null
    ) : CryptoSyncException(message = message, isRetryable = false, cause = cause)

    class InvalidData(
        message: String = "Некоректні дані від API",
        cause: Throwable? = null
    ) : CryptoSyncException(message = message, isRetryable = false, cause = cause)
}