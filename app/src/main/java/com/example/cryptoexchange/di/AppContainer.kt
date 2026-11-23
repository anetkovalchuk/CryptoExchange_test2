package com.example.cryptoexchange.di

import android.content.Context
import androidx.work.WorkManager
import com.example.cryptoexchange.data.local.CryptoDatabase
import com.example.cryptoexchange.data.remote.CryptoNetworkModule
import com.example.cryptoexchange.data.repository.CryptoRepositoryImpl
import com.example.cryptoexchange.data.worker.CryptoSyncScheduler
import com.example.cryptoexchange.data.worker.WorkManagerCryptoSyncManager
import com.example.cryptoexchange.domain.repository.CryptoRepository
import com.example.cryptoexchange.domain.repository.CryptoSyncManager
import com.example.cryptoexchange.domain.usecase.ObserveCryptocurrenciesUseCase
import com.example.cryptoexchange.domain.usecase.SyncCryptocurrenciesUseCase

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database = CryptoDatabase.getDatabase(appContext)
    private val cryptoDao = database.cryptoDao()

    private val cryptoApi = CryptoNetworkModule.api

    val cryptoRepository: CryptoRepository = CryptoRepositoryImpl(
        api = cryptoApi,
        dao = cryptoDao
    )

    val syncCryptocurrenciesUseCase = SyncCryptocurrenciesUseCase(
        repository = cryptoRepository
    )

    val observeCryptocurrenciesUseCase = ObserveCryptocurrenciesUseCase(
        repository = cryptoRepository
    )

    private val workManager: WorkManager = WorkManager.getInstance(appContext)

    val cryptoSyncScheduler = CryptoSyncScheduler(
        context = appContext
    )

    val cryptoSyncManager: CryptoSyncManager = WorkManagerCryptoSyncManager(
        workManager = workManager,
        syncScheduler = cryptoSyncScheduler
    )
}