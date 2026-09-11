package com.example.easyvet.data.triage

import com.example.easyvet.data.model.ContagionRisk
import com.example.easyvet.data.model.Species
import com.example.easyvet.data.model.Symptom
import com.example.easyvet.data.model.TriageSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriageEngineTest {

    @Test
    fun evaluate_footAndMouthDisease_returnsEmergencyAndCriticalContagion() {
        val result = TriageEngine.evaluate(
            symptoms = listOf(Symptom.MOUTH_BLISTERS, Symptom.FOOT_LESIONS, Symptom.PROFUSE_SALIVATION),
            species = Species.CATTLE,
            affectedCount = 4,
            totalHerdCount = 20
        )

        assertEquals(TriageSeverity.EMERGENCY, result.severity)
        assertEquals(ContagionRisk.CRITICAL, result.contagionRisk)
        assertTrue(result.suspectedDiseases.any { it.contains("Foot and Mouth Disease") })
        assertTrue(result.quarantineRequired)
        assertTrue(result.immediateNotificationRequired)
        assertTrue(result.requiresSampleCollection)
    }

    @Test
    fun evaluate_suddenMortality_returnsEmergencyAndAnthrax() {
        val result = TriageEngine.evaluate(
            symptoms = listOf(Symptom.SUDDEN_MORTALITY, Symptom.BLOODY_DIARRHEA),
            species = Species.CATTLE,
            affectedCount = 2,
            mortalityCount = 2,
            totalHerdCount = 10
        )

        assertEquals(TriageSeverity.EMERGENCY, result.severity)
        assertEquals(ContagionRisk.CRITICAL, result.contagionRisk)
        assertTrue(result.suspectedDiseases.any { it.contains("Anthrax") })
        assertTrue(result.recommendedActions.any { it.contains("DO NOT OPEN CARCASS") })
    }

    @Test
    fun evaluate_lumpySkinDisease_returnsHighSeverity() {
        val result = TriageEngine.evaluate(
            symptoms = listOf(Symptom.SKIN_NODULES_LESIONS, Symptom.HIGH_FEVER),
            species = Species.CATTLE,
            affectedCount = 2,
            totalHerdCount = 15
        )

        assertEquals(TriageSeverity.HIGH, result.severity)
        assertTrue(result.suspectedDiseases.any { it.contains("Lumpy Skin Disease") })
    }

    @Test
    fun evaluate_mildSymptoms_returnsLowOrMediumSeverity() {
        val result = TriageEngine.evaluate(
            symptoms = listOf(Symptom.LOSS_OF_APPETITE),
            species = Species.GOAT,
            affectedCount = 1,
            totalHerdCount = 10
        )

        assertTrue(result.severity == TriageSeverity.LOW || result.severity == TriageSeverity.MEDIUM)
    }
}
