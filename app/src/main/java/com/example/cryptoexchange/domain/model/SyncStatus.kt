package com.example.cryptoexchange.domain.model

enum class SyncStatus {
    IDLE,
    ENQUEUED,
    RUNNING,
    SUCCEEDED,
    FAILED
}

data class SyncProgress(
    val status: SyncStatus = SyncStatus.IDLE,
    val errorMessage: String? = null
)