package com.example.easyvet.data.triage

import com.example.easyvet.data.model.ContagionRisk
import com.example.easyvet.data.model.Species
import com.example.easyvet.data.model.Symptom
import com.example.easyvet.data.model.TriageResult
import com.example.easyvet.data.model.TriageSeverity

object TriageEngine {

    fun evaluate(
        symptoms: List<Symptom>,
        species: Species,
        affectedCount: Int = 1,
        mortalityCount: Int = 0,
        totalHerdCount: Int = 1
    ): TriageResult {
        val selectedSet = symptoms.toSet()
        val suspectedDiseases = mutableListOf<String>()
        val actions = mutableSetOf<String>()

        var isEmergency = false
        var isHighSeverity = false
        var isMediumSeverity = false

        var isCriticalContagion = false
        var isHighContagion = false
        var isModerateContagion = false

        var quarantineRequired = false
        var immediateNotificationRequired = false
        var requiresSampleCollection = false

        // Rule 1: Sudden Mortality & Anthrax Evaluation
        if (selectedSet.contains(Symptom.SUDDEN_MORTALITY) || mortalityCount > 0) {
            isEmergency = true
            isCriticalContagion = true
            quarantineRequired = true
            immediateNotificationRequired = true
            requiresSampleCollection = true

            if (selectedSet.contains(Symptom.BLOODY_DIARRHEA) || selectedSet.contains(Symptom.HIGH_FEVER)) {
                suspectedDiseases.add("Anthrax (Bacillus anthracis)")
                actions.add("DO NOT OPEN CARCASS - Spore aerosol hazard!")
                actions.add("Notify district veterinary emergency officer immediately.")
                actions.add("Deep bury carcass with quicklime or burn completely.")
                actions.add("Quarantine pasture area; prohibit grazing.")
            } else if (species == Species.PIG) {
                suspectedDiseases.add("African Swine Fever (ASF)")
                actions.add("Strict lockdown of piggery premises.")
                actions.add("Disinfect footwear and pens with 2% sodium hydroxide.")
                actions.add("Notify swine health authority immediately.")
            } else if (species == Species.POULTRY) {
                suspectedDiseases.add("Highly Pathogenic Avian Influenza / Newcastle Disease")
                actions.add("Isolate flock house; restrict handler entry.")
                actions.add("Wear protective gloves and mask when removing dead birds.")
            } else {
                suspectedDiseases.add("Acute Hemorrhagic Septicemia / Blackleg")
                actions.add("Isolate remaining herd from pasture.")
                actions.add("Consult vet for emergency vaccination/antibiotics.")
            }
        }

        // Rule 2: Foot and Mouth Disease (FMD)
        if (selectedSet.contains(Symptom.MOUTH_BLISTERS) ||
            (selectedSet.contains(Symptom.FOOT_LESIONS) && selectedSet.contains(Symptom.PROFUSE_SALIVATION)) ||
            (selectedSet.contains(Symptom.MOUTH_BLISTERS) && selectedSet.contains(Symptom.LAMENESS))
        ) {
            isEmergency = true
            isCriticalContagion = true
            quarantineRequired = true
            immediateNotificationRequired = true
            requiresSampleCollection = true

            suspectedDiseases.add("Foot and Mouth Disease (FMD)")
            actions.add("Impose strict quarantine on the entire herd/farm.")
            actions.add("Halt all livestock movement to/from the village.")
            actions.add("Collect vesicular fluid/swab sample for lab confirmation.")
            actions.add("Apply mild antiseptic wash to oral and foot lesions.")
        }

        // Rule 3: Lumpy Skin Disease (LSD)
        if (selectedSet.contains(Symptom.SKIN_NODULES_LESIONS) &&
            (species == Species.CATTLE || species == Species.BUFFALO)
        ) {
            isHighSeverity = true
            isHighContagion = true
            quarantineRequired = true
            requiresSampleCollection = true

            suspectedDiseases.add("Lumpy Skin Disease (LSD)")
            actions.add("Isolate affected cattle in a insect-screened enclosure.")
            actions.add("Apply insect repellent spray/dip to control fly and mosquito vectors.")
            actions.add("Administer antipyretic/anti-inflammatory supportive treatment.")
            actions.add("Notify local veterinary officer for ring vaccination.")
        }

        // Rule 4: Respiratory Distress (CBPP / PPR)
        if (selectedSet.contains(Symptom.RESPIRATORY_DISTRESS)) {
            if (species == Species.CATTLE || species == Species.BUFFALO) {
                suspectedDiseases.add("Contagious Bovine Pleuropneumonia (CBPP)")
                isHighSeverity = true
                isHighContagion = true
                quarantineRequired = true
                requiresSampleCollection = true
                actions.add("Isolate coughing cattle from main herd.")
                actions.add("Collect nasal swabs/serum for CBPP laboratory testing.")
                actions.add("Avoid mixing herds at communal watering points.")
            } else if (species == Species.GOAT || species == Species.SHEEP) {
                suspectedDiseases.add("Peste des Petits Ruminants (PPR)")
                isHighSeverity = true
                isCriticalContagion = true
                quarantineRequired = true
                immediateNotificationRequired = true
                actions.add("Isolate dyspneic goats/sheep immediately.")
                actions.add("Notify district veterinary service for emergency PPR vaccination.")
            } else {
                isMediumSeverity = true
                isModerateContagion = true
                actions.add("Isolate animals displaying respiratory signs.")
            }
        }

        // Rule 5: Abortion Storm
        if (selectedSet.contains(Symptom.ABORTION)) {
            isMediumSeverity = true
            isModerateContagion = true
            quarantineRequired = true
            requiresSampleCollection = true

            suspectedDiseases.add("Brucellosis / Rift Valley Fever (RVF)")
            actions.add("Isolate aborting females; handle fetal tissues with thick rubber gloves (Zoonotic Risk!).")
            actions.add("Collect blood serum and aborted tissue for laboratory diagnosis.")
            actions.add("Do not consume unpasteurized milk from affected animals.")
        }

        // Rule 6: High Fever + Loss of Appetite + Lameness (General Infectious)
        if (selectedSet.contains(Symptom.HIGH_FEVER) && selectedSet.contains(Symptom.LOSS_OF_APPETITE)) {
            if (!isEmergency && !isHighSeverity) {
                isMediumSeverity = true
                isModerateContagion = true
            }
            if (!suspectedDiseases.contains("Tick-Borne Disease (East Coast Fever / Anaplasmosis)")) {
                suspectedDiseases.add("Tick-Borne Infection / Acute Systemic Disease")
            }
            actions.add("Check animal for ticks (ears, udder, dewlap); apply acaricide.")
            actions.add("Record temperature morning and evening.")
        }

        // Fallback for mild / single symptom
        if (suspectedDiseases.isEmpty()) {
            suspectedDiseases.add("Non-Specific Fever / Early Stage Infection / Parasitic Condition")
            actions.add("Monitor animal closely for 24-48 hours.")
            actions.add("Provide clean drinking water, shade, and palatable feed.")
            actions.add("Contact local community animal health worker if condition worsens.")
        }

        // Calculate final Severity & Contagion Risk
        val finalSeverity = when {
            isEmergency -> TriageSeverity.EMERGENCY
            isHighSeverity || mortalityCount > 0 || (affectedCount.toDouble() / totalHerdCount.coerceAtLeast(1) > 0.3) -> TriageSeverity.HIGH
            isMediumSeverity || selectedSet.size >= 3 -> TriageSeverity.MEDIUM
            else -> TriageSeverity.LOW
        }

        val finalContagionRisk = when {
            isCriticalContagion -> ContagionRisk.CRITICAL
            isHighContagion -> ContagionRisk.HIGH
            isModerateContagion -> ContagionRisk.MODERATE
            else -> ContagionRisk.LOW
        }

        val summary = when (finalSeverity) {
            TriageSeverity.EMERGENCY -> "CRITICAL ALERT: High contagion/mortality threat detected. Immediate action required!"
            TriageSeverity.HIGH -> "HIGH RISK: Serious infectious signs present. Isolate affected animals promptly."
            TriageSeverity.MEDIUM -> "MODERATE RISK: Infectious condition suspected. Quarantine and monitor closely."
            TriageSeverity.LOW -> "LOW RISK: Mild or localized symptoms. Provide supportive care and observe."
        }

        return TriageResult(
            severity = finalSeverity,
            contagionRisk = finalContagionRisk,
            suspectedDiseases = suspectedDiseases.distinct(),
            recommendedActions = actions.toList(),
            quarantineRequired = quarantineRequired,
            immediateNotificationRequired = immediateNotificationRequired,
            requiresSampleCollection = requiresSampleCollection,
            summaryMessage = summary
        )
    }
}
