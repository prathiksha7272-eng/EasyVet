package com.example.easyvet.ui.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.data.local.entity.LabReferralEntity
import com.example.easyvet.data.model.ReferralStatus
import com.example.easyvet.data.repository.LabReferralRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LabFilterGroup1(
    val statusFilter: ReferralStatus? = null,
    val searchQuery: String = "",
    val selectedReferral: LabReferralEntity? = null
)

data class LabFilterGroup2(
    val showCreateDialog: Boolean = false,
    val showStatusUpdateDialog: Boolean = false,
    val showQrDialog: Boolean = false
)

data class LabFilterParams(
    val g1: LabFilterGroup1,
    val g2: LabFilterGroup2
)

data class LabReferralUiState(
    val referrals: List<LabReferralEntity> = emptyList(),
    val filteredReferrals: List<LabReferralEntity> = emptyList(),
    val selectedStatusFilter: ReferralStatus? = null,
    val searchQuery: String = "",
    val selectedReferral: LabReferralEntity? = null,
    val showCreateDialog: Boolean = false,
    val showStatusUpdateDialog: Boolean = false,
    val showQrDialog: Boolean = false,
    val prefilledAnimalTag: String = "",
    val prefilledSpecies: String = "",
    val prefilledSuspectedDisease: String = "",
    val prefilledReportId: String? = null,
    val totalReferralsCount: Int = 0,
    val pendingOrInTransitCount: Int = 0,
    val testedResultsReadyCount: Int = 0,
    val positiveCasesCount: Int = 0
)

class LabReferralViewModel(
    private val labReferralRepository: LabReferralRepository,
    initialAnimalTag: String = "",
    initialSpecies: String = "",
    initialSuspectedDisease: String = "",
    initialReportId: String? = null
) : ViewModel() {

    private val statusFilterFlow = MutableStateFlow<ReferralStatus?>(null)
    private val searchQueryFlow = MutableStateFlow("")
    private val selectedReferralFlow = MutableStateFlow<LabReferralEntity?>(null)

    private val showCreateDialogFlow = MutableStateFlow(initialAnimalTag.isNotEmpty())
    private val showStatusUpdateDialogFlow = MutableStateFlow(false)
    private val showQrDialogFlow = MutableStateFlow(false)

    private val prefilledTagFlow = MutableStateFlow(initialAnimalTag)
    private val prefilledSpeciesFlow = MutableStateFlow(initialSpecies)
    private val prefilledDiseaseFlow = MutableStateFlow(initialSuspectedDisease)
    private val prefilledReportIdFlow = MutableStateFlow(initialReportId)

    private val g1Flow = combine(
        statusFilterFlow,
        searchQueryFlow,
        selectedReferralFlow
    ) { status, search, referral ->
        LabFilterGroup1(status, search, referral)
    }

    private val g2Flow = combine(
        showCreateDialogFlow,
        showStatusUpdateDialogFlow,
        showQrDialogFlow
    ) { create, update, qr ->
        LabFilterGroup2(create, update, qr)
    }

    private val filterParamsFlow = combine(
        g1Flow,
        g2Flow
    ) { g1, g2 ->
        LabFilterParams(g1, g2)
    }

    val uiState: StateFlow<LabReferralUiState> = combine(
        labReferralRepository.allReferrals,
        filterParamsFlow,
        prefilledTagFlow,
        prefilledSpeciesFlow,
        prefilledDiseaseFlow
    ) { referrals, params, pTag, pSpecies, pDisease ->
        val g1 = params.g1
        val g2 = params.g2

        val filtered = referrals.filter { referral ->
            val matchesStatus = g1.statusFilter == null || referral.status == g1.statusFilter
            val matchesSearch = g1.searchQuery.isBlank() ||
                    referral.trackingNumber.contains(g1.searchQuery, ignoreCase = true) ||
                    referral.animalTag.contains(g1.searchQuery, ignoreCase = true) ||
                    referral.suspectedDisease.contains(g1.searchQuery, ignoreCase = true) ||
                    referral.destinationLab.contains(g1.searchQuery, ignoreCase = true) ||
                    referral.sampleType.contains(g1.searchQuery, ignoreCase = true)

            matchesStatus && matchesSearch
        }

        val total = referrals.size
        val pendingInTransit = referrals.count {
            it.status == ReferralStatus.PENDING || it.status == ReferralStatus.IN_TRANSIT || it.status == ReferralStatus.RECEIVED_AT_LAB
        }
        val testedReady = referrals.count {
            it.status == ReferralStatus.TESTING || it.status == ReferralStatus.RESULT_READY || it.status == ReferralStatus.COMPLETED
        }
        val positiveCases = referrals.count {
            it.testResults?.contains("Positive", ignoreCase = true) == true
        }

        LabReferralUiState(
            referrals = referrals,
            filteredReferrals = filtered,
            selectedStatusFilter = g1.statusFilter,
            searchQuery = g1.searchQuery,
            selectedReferral = g1.selectedReferral,
            showCreateDialog = g2.showCreateDialog,
            showStatusUpdateDialog = g2.showStatusUpdateDialog,
            showQrDialog = g2.showQrDialog,
            prefilledAnimalTag = pTag,
            prefilledSpecies = pSpecies,
            prefilledSuspectedDisease = pDisease,
            prefilledReportId = prefilledReportIdFlow.value,
            totalReferralsCount = total,
            pendingOrInTransitCount = pendingInTransit,
            testedResultsReadyCount = testedReady,
            positiveCasesCount = positiveCases
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LabReferralUiState()
    )

    fun setStatusFilter(status: ReferralStatus?) {
        statusFilterFlow.value = status
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun selectReferral(referral: LabReferralEntity?) {
        selectedReferralFlow.value = referral
    }

    fun setShowCreateDialog(show: Boolean) {
        showCreateDialogFlow.value = show
    }

    fun setShowStatusUpdateDialog(show: Boolean) {
        showStatusUpdateDialogFlow.value = show
    }

    fun setShowQrDialog(show: Boolean) {
        showQrDialogFlow.value = show
    }

    fun setPrefilledData(animalTag: String, species: String, suspectedDisease: String, reportId: String? = null) {
        prefilledTagFlow.value = animalTag
        prefilledSpeciesFlow.value = species
        prefilledDiseaseFlow.value = suspectedDisease
        prefilledReportIdFlow.value = reportId
        if (animalTag.isNotEmpty()) {
            showCreateDialogFlow.value = true
        }
    }

    fun createNewReferral(
        animalTag: String,
        species: String,
        sampleType: String,
        suspectedDisease: String,
        urgency: String,
        destinationLab: String,
        collectorName: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val created = labReferralRepository.createReferral(
                symptomReportId = prefilledReportIdFlow.value,
                animalTag = animalTag,
                species = species,
                sampleType = sampleType,
                suspectedDisease = suspectedDisease,
                urgency = urgency,
                destinationLab = destinationLab,
                collectorName = collectorName,
                resultNotes = notes
            )
            selectedReferralFlow.value = created
            showCreateDialogFlow.value = false
        }
    }

    fun updateReferralStatusAndResult(
        referralId: String,
        newStatus: ReferralStatus,
        resultOutcome: String?,
        resultNotes: String?
    ) {
        viewModelScope.launch {
            labReferralRepository.updateReferralStatus(
                id = referralId,
                status = newStatus,
                results = resultOutcome,
                notes = resultNotes
            )
            showStatusUpdateDialogFlow.value = false
            // Refresh selected referral if matches
            if (selectedReferralFlow.value?.id == referralId) {
                val updated = labReferralRepository.getReferralById(referralId)
                selectedReferralFlow.value = updated
            }
        }
    }

    class Factory(
        private val application: EasyVetApplication,
        private val animalTag: String = "",
        private val species: String = "",
        private val suspectedDisease: String = "",
        private val reportId: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LabReferralViewModel::class.java)) {
                return LabReferralViewModel(
                    labReferralRepository = application.labReferralRepository,
                    initialAnimalTag = animalTag,
                    initialSpecies = species,
                    initialSuspectedDisease = suspectedDisease,
                    initialReportId = reportId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
