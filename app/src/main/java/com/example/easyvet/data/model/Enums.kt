package com.example.easyvet.data.model

enum class Species(val displayName: String) {
    CATTLE("Cattle"),
    BUFFALO("Buffalo"),
    GOAT("Goat"),
    SHEEP("Sheep"),
    PIG("Pig"),
    POULTRY("Poultry"),
    HORSE("Horse / Donkey"),
    OTHER("Other Livestock")
}

enum class Symptom(val displayName: String, val description: String) {
    HIGH_FEVER("High Fever", "Body temperature > 40°C / 104°F"),
    MOUTH_BLISTERS("Mouth Blisters / Ulcers", "Vesicles on tongue, lips, or gums"),
    FOOT_LESIONS("Foot / Hoof Lesions", "Blisters or sores around hooves/coronet"),
    SUDDEN_MORTALITY("Sudden Death / High Mortality", "Unexplained death of 1 or more animals"),
    RESPIRATORY_DISTRESS("Respiratory Distress", "Coughing, labored breathing, nasal discharge"),
    SKIN_NODULES_LESIONS("Skin Nodules / Lumps", "Firm cutaneous nodules or scabs"),
    PROFUSE_SALIVATION("Profuse Salivation / Foaming", "Excessive drooling or ropy saliva"),
    BLOODY_DIARRHEA("Bloody Diarrhea / Hemorrhage", "Blood in stool or unclotted dark blood from orifices"),
    ABORTION("Abortion Storm", "Premature pregnancy loss in multiple females"),
    LAMENESS("Severe Lameness", "Reluctance or inability to walk"),
    LOSS_OF_APPETITE("Loss of Appetite", "Inappetence or lethargy"),
    DECREASED_MILK("Sudden Drop in Milk Yield", "Drastic reduction in daily milk production")
}

enum class TriageSeverity(val label: String, val colorHex: Long) {
    EMERGENCY("EMERGENCY - RED", 0xFFD32F2F),
    HIGH("HIGH RISK - ORANGE", 0xFFE65100),
    MEDIUM("MODERATE - YELLOW", 0xFFF57F17),
    LOW("LOW RISK - GREEN", 0xFF388E3C)
}

enum class ContagionRisk(val label: String) {
    CRITICAL("CRITICAL CONTAGION"),
    HIGH("HIGH CONTAGION"),
    MODERATE("MODERATE CONTAGION"),
    LOW("LOW CONTAGION")
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

enum class ReferralStatus(val label: String) {
    PENDING("Sample Registered"),
    IN_TRANSIT("In Transit to Lab"),
    RECEIVED_AT_LAB("Received at Lab"),
    TESTING("Testing in Progress"),
    RESULT_READY("Result Ready"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class AlertSeverity(val label: String, val colorHex: Long) {
    CRITICAL("Critical Outbreak", 0xFFD32F2F),
    HIGH("High Risk Zone", 0xFFE65100),
    WARNING("Warning / Watch", 0xFFF57F17),
    INFO("Informational Notice", 0xFF1976D2)
}

enum class HealthStatus(val label: String) {
    HEALTHY("Healthy"),
    SICK("Sick / Symptomatic"),
    QUARANTINED("Quarantined"),
    DECEASED("Deceased")
}
