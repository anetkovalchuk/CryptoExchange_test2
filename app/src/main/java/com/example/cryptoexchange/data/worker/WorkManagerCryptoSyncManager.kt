package com.example.cryptoexchange.data.worker

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.cryptoexchange.domain.model.SyncProgress
import com.example.cryptoexchange.domain.model.SyncStatus
import com.example.cryptoexchange.domain.repository.CryptoSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class WorkManagerCryptoSyncManager(
    private val workManager: WorkManager,
    private val syncScheduler: CryptoSyncScheduler
) : CryptoSyncManager {

    override fun observeSyncProgress(): Flow<SyncProgress> {
        return combine(
            workManager.getWorkInfosForUniqueWorkFlow(CryptoSyncScheduler.MANUAL_SYNC_WORK_NAME),
            workManager.getWorkInfosForUniqueWorkFlow(CryptoSyncScheduler.PERIODIC_SYNC_WORK_NAME)
        ) { manualInfos, periodicInfos ->
            val manualWork = selectRelevantWorkInfo(manualInfos)
            val periodicWork = selectRelevantWorkInfo(periodicInfos)

            resolveCombinedProgress(manualWork, periodicWork)
        }
    }

    override fun scheduleManualSync() {
        syncScheduler.scheduleManualSync()
    }

    private fun selectRelevantWorkInfo(workInfos: List<WorkInfo>): WorkInfo? {
        if (workInfos.isEmpty()) return null
        return workInfos.lastOrNull { it.state == WorkInfo.State.RUNNING }
            ?: workInfos.lastOrNull {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED
            }
            ?: workInfos.lastOrNull()
    }

    private fun resolveCombinedProgress(
        manualWork: WorkInfo?,
        periodicWork: WorkInfo?
    ): SyncProgress {
        val allWorks = listOfNotNull(manualWork, periodicWork)

        if (allWorks.any { it.state == WorkInfo.State.RUNNING }) {
            return SyncProgress(status = SyncStatus.RUNNING)
        }

        if (manualWork?.state == WorkInfo.State.ENQUEUED || manualWork?.state == WorkInfo.State.BLOCKED) {
            return SyncProgress(status = SyncStatus.ENQUEUED)
        }

        if (manualWork?.state == WorkInfo.State.FAILED) {
            if (periodicWork?.state == WorkInfo.State.SUCCEEDED) {
                return SyncProgress(status = SyncStatus.SUCCEEDED)
            }
            val errorMsg = manualWork.outputData
                .getString(CryptoSyncWorker.OUTPUT_MESSAGE)
                ?.takeIf { it.isNotBlank() }
                ?: "Не вдалося оновити дані"
            return SyncProgress(
                status = SyncStatus.FAILED,
                errorMessage = errorMsg
            )
        }

        if (allWorks.any { it.state == WorkInfo.State.SUCCEEDED }) {
            return SyncProgress(status = SyncStatus.SUCCEEDED)
        }

        if (periodicWork?.state == WorkInfo.State.ENQUEUED || periodicWork?.state == WorkInfo.State.BLOCKED) {
            return SyncProgress(status = SyncStatus.ENQUEUED)
        }

        return SyncProgress(status = SyncStatus.IDLE)
    }
}