package com.example.easyvet.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.model.ReferralStatus
import com.example.easyvet.data.model.SyncStatus
import com.example.easyvet.data.repository.AnimalRepository
import com.example.easyvet.data.repository.LabReferralRepository
import com.example.easyvet.data.repository.RiskMapRepository
import com.example.easyvet.data.repository.SymptomReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val activeOutbreaksCount: Int = 0,
    val pendingLabSamplesCount: Int = 0,
    val totalRegisteredAnimalsCount: Int = 0,
    val pendingOfflineSyncsCount: Int = 0,
    val activeAlerts: List<OutbreakAlertEntity> = emptyList(),
    val recentReports: List<SymptomReportEntity> = emptyList(),
    val isSyncing: Boolean = false,
    val syncResultMessage: String? = null
)

private data class SyncInfo(
    val isSyncing: Boolean,
    val syncResultMessage: String?
)

class DashboardViewModel(
    private val symptomReportRepository: SymptomReportRepository,
    private val labReferralRepository: LabReferralRepository,
    private val animalRepository: AnimalRepository,
    private val riskMapRepository: RiskMapRepository
) : ViewModel() {

    private val isSyncingFlow = MutableStateFlow(false)
    private val syncResultMessageFlow = MutableStateFlow<String?>(null)

    private val syncInfoFlow = combine(isSyncingFlow, syncResultMessageFlow) { syncing, msg ->
        SyncInfo(syncing, msg)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        riskMapRepository.allAlerts,
        labReferralRepository.allReferrals,
        animalRepository.allAnimals,
        symptomReportRepository.allReports,
        syncInfoFlow
    ) { alerts, referrals, animals, reports, syncInfo ->
        val activeOutbreaks = alerts.size
        val pendingLabSamples = referrals.count {
            it.status == ReferralStatus.PENDING || it.status == ReferralStatus.IN_TRANSIT
        }
        val totalAnimals = animals.size
        val pendingSyncs = reports.count { it.syncStatus == SyncStatus.PENDING }

        DashboardUiState(
            activeOutbreaksCount = activeOutbreaks,
            pendingLabSamplesCount = pendingLabSamples,
            totalRegisteredAnimalsCount = totalAnimals,
            pendingOfflineSyncsCount = pendingSyncs,
            activeAlerts = alerts.take(4),
            recentReports = reports.take(5),
            isSyncing = syncInfo.isSyncing,
            syncResultMessage = syncInfo.syncResultMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun syncPendingReports() {
        viewModelScope.launch {
            isSyncingFlow.value = true
            syncResultMessageFlow.value = null
            try {
                val syncedCount = symptomReportRepository.syncPendingReports()
                if (syncedCount > 0) {
                    syncResultMessageFlow.value = "Successfully synced $syncedCount report(s) with central server!"
                } else {
                    syncResultMessageFlow.value = "All reports are already synced."
                }
            } catch (e: Exception) {
                syncResultMessageFlow.value = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isSyncingFlow.value = false
            }
        }
    }

    fun clearSyncMessage() {
        syncResultMessageFlow.value = null
    }

    class Factory(private val application: EasyVetApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(
                    symptomReportRepository = application.symptomReportRepository,
                    labReferralRepository = application.labReferralRepository,
                    animalRepository = application.animalRepository,
                    riskMapRepository = application.riskMapRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
