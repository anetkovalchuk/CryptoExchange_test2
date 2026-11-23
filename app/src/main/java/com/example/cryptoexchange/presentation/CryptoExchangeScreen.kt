package com.example.cryptoexchange.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.cryptoexchange.domain.model.CryptoCurrency
import com.example.cryptoexchange.domain.model.SyncStatus

@Composable
fun CryptoExchangeRoute(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CryptoExchangeScreen(
        uiState = uiState,
        onRefreshClick = viewModel::onRefreshClicked,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoExchangeScreen(
    uiState: CryptoUiState,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActionDisabled = uiState.isLoading || uiState.isManualSyncActive || uiState.syncState == SyncStatus.RUNNING

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Біржа криптовалют",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    Button(
                        onClick = onRefreshClick,
                        enabled = !isActionDisabled,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        if (isActionDisabled) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(text = "Оновити")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SyncStatusBanner(
                syncState = uiState.syncState,
                isLoading = uiState.isLoading,
                lastUpdatedText = formatUpdatedTimestamp(uiState.lastUpdatedAtMillis),
                errorMessage = uiState.errorMessage
            )

            when {
                uiState.currencies.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.currencies,
                            key = { it.id }
                        ) { currency ->
                            CryptoCurrencyRow(currency = currency)
                        }
                    }
                }

                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Завантаження списку криптовалют...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                else -> {
                    EmptyOrErrorState(
                        syncState = uiState.syncState,
                        errorMessage = uiState.errorMessage,
                        isRetryEnabled = !isActionDisabled,
                        onRetryClick = onRefreshClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusBanner(
    syncState: SyncStatus,
    isLoading: Boolean,
    lastUpdatedText: String,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Статус: ${syncState.label()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Оновлено: $lastUpdatedText",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (isLoading || syncState == SyncStatus.RUNNING) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun CryptoCurrencyRow(
    currency: CryptoCurrency,
    modifier: Modifier = Modifier
) {
    val priceText = formatUsdPrice(currency.priceUsd)
    val isMissingPrice = priceText == "Немає даних"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${currency.rank}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(38.dp),
                maxLines = 1
            )

            CryptoLogo(
                logoUrl = currency.logoUrl,
                contentDescription = currency.name
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currency.symbol.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = priceText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMissingPrice) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CryptoLogo(
    logoUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val cleanUrl = logoUrl?.takeIf { it.isNotBlank() }

    if (cleanUrl == null) {
        LogoPlaceholder(modifier = modifier)
    } else {
        var isImageError by remember(cleanUrl) { mutableStateOf(false) }

        if (isImageError) {
            LogoPlaceholder(modifier = modifier)
        } else {
            AsyncImage(
                model = cleanUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                onError = { isImageError = true },
                modifier = modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

@Composable
private fun LogoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "₿",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun EmptyOrErrorState(
    syncState: SyncStatus,
    errorMessage: String?,
    isRetryEnabled: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when {
                !errorMessage.isNullOrBlank() -> errorMessage
                syncState == SyncStatus.ENQUEUED -> "Немає підключення до мережі. Дані оновляться автоматично, коли з'явиться інтернет."
                else -> "Дані про криптовалюти ще не завантажені"
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onRetryClick,
            enabled = isRetryEnabled
        ) {
            Text(text = "Спробувати оновити")
        }
    }
}

private fun SyncStatus.label(): String = when (this) {
    SyncStatus.IDLE -> "Очікування"
    SyncStatus.ENQUEUED -> "У черзі (очікування мережі)"
    SyncStatus.RUNNING -> "Оновлення..."
    SyncStatus.SUCCEEDED -> "Оновлено"
    SyncStatus.FAILED -> "Помилка"
}