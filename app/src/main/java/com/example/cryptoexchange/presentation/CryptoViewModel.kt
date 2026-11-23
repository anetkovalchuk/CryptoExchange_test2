package com.example.cryptoexchange.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cryptoexchange.domain.model.SyncProgress
import com.example.cryptoexchange.domain.model.SyncStatus
import com.example.cryptoexchange.domain.repository.CryptoSyncManager
import com.example.cryptoexchange.domain.usecase.ObserveCryptocurrenciesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CryptoViewModel(
    observeCryptocurrenciesUseCase: ObserveCryptocurrenciesUseCase,
    private val syncManager: CryptoSyncManager
) : ViewModel() {

    private val isStartingSync = MutableStateFlow(false)
    private val hasObservedCache = MutableStateFlow(false)

    private val syncProgressState: StateFlow<SyncProgress> = syncManager
        .observeSyncProgress()
        .onEach { progress ->
            if (progress.status != SyncStatus.IDLE) {
                isStartingSync.value = false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SyncProgress()
        )

    val uiState: StateFlow<CryptoUiState> = combine(
        observeCryptocurrenciesUseCase().onEach {
            hasObservedCache.value = true
        },
        syncProgressState,
        isStartingSync,
        hasObservedCache
    ) { currencies, syncProgress, starting, hasLoadedCache ->
        val isQueuedOrRunning = syncProgress.status == SyncStatus.ENQUEUED ||
                syncProgress.status == SyncStatus.RUNNING

        val isActuallyRunning = syncProgress.status == SyncStatus.RUNNING

        val resolvedLoading = !hasLoadedCache || isActuallyRunning || starting

        val maxUpdatedInDb = currencies.maxOfOrNull { it.updatedAt }

        val resolvedError = when (syncProgress.status) {
            SyncStatus.FAILED -> syncProgress.errorMessage ?: "Помилка синхронізації"
            else -> null
        }

        CryptoUiState(
            currencies = currencies,
            isLoading = resolvedLoading,
            syncState = syncProgress.status,
            lastUpdatedAtMillis = maxUpdatedInDb,
            errorMessage = resolvedError,
            isManualSyncActive = isQueuedOrRunning || starting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CryptoUiState(isLoading = true)
    )

    fun onRefreshClicked() {
        if (uiState.value.isLoading || uiState.value.isManualSyncActive) return

        isStartingSync.update { true }
        syncManager.scheduleManualSync()
    }

    class Factory(
        private val observeCryptocurrenciesUseCase: ObserveCryptocurrenciesUseCase,
        private val syncManager: CryptoSyncManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return CryptoViewModel(
                observeCryptocurrenciesUseCase = observeCryptocurrenciesUseCase,
                syncManager = syncManager
            ) as T
        }
    }
}