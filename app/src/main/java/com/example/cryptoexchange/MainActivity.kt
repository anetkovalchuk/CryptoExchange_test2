package com.example.cryptoexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptoexchange.presentation.CryptoExchangeRoute
import com.example.cryptoexchange.presentation.CryptoViewModel
import com.example.cryptoexchange.ui.theme.CryptoExchangeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as CryptoExchangeApplication).container

        setContent {
            CryptoExchangeTheme {
                val vm: CryptoViewModel = viewModel(
                    factory = CryptoViewModel.Factory(
                        observeCryptocurrenciesUseCase = appContainer.observeCryptocurrenciesUseCase,
                        syncManager = appContainer.cryptoSyncManager
                    )
                )
                CryptoExchangeRoute(viewModel = vm)
            }
        }
    }
}