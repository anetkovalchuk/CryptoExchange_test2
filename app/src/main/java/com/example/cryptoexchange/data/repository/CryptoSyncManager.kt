package com.example.cryptoexchange.domain.repository

import com.example.cryptoexchange.domain.model.SyncProgress
import kotlinx.coroutines.flow.Flow

interface CryptoSyncManager {
    fun observeSyncProgress(): Flow<SyncProgress>
    fun scheduleManualSync()
}