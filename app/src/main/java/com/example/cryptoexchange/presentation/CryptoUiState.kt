package com.example.cryptoexchange.presentation

import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.domain.model.SyncStatus

data class CryptoUiState(
    val currencies: List<CryptoCurrency> = emptyList(),
    val isLoading: Boolean = true,
    val syncState: SyncStatus = SyncStatus.IDLE,
    val lastUpdatedAtMillis: Long? = null,
    val errorMessage: String? = null,
    val isManualSyncActive: Boolean = false
)