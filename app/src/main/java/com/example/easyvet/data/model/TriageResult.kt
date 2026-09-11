package com.example.easyvet.data.model

data class TriageResult(
    val severity: TriageSeverity,
    val contagionRisk: ContagionRisk,
    val suspectedDiseases: List<String>,
    val recommendedActions: List<String>,
    val quarantineRequired: Boolean,
    val immediateNotificationRequired: Boolean,
    val requiresSampleCollection: Boolean,
    val summaryMessage: String
)
