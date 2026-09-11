package com.example.easyvet.data.local

import com.example.easyvet.data.local.entity.AdvisoryEntity
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.local.entity.LabReferralEntity
import com.example.easyvet.data.local.entity.OutbreakAlertEntity
import com.example.easyvet.data.local.entity.SymptomReportEntity
import com.example.easyvet.data.local.entity.UserEntity
import com.example.easyvet.data.model.AlertSeverity
import com.example.easyvet.data.model.ContagionRisk
import com.example.easyvet.data.model.HealthStatus
import com.example.easyvet.data.model.ReferralStatus
import com.example.easyvet.data.model.Species
import com.example.easyvet.data.model.SyncStatus
import com.example.easyvet.data.model.TriageSeverity

object SeedData {

    val initialAnimals = listOf(
        AnimalEntity(
            id = "ANM-1001",
            tagNumber = "TZ-KIB-8821",
            name = "Bessie",
            species = Species.CATTLE.displayName,
            breed = "Friesian-Zebu Cross",
            ageMonths = 36,
            gender = "Female",
            weightKg = 380.0,
            isHerdGroup = false,
            herdSize = 1,
            ownerName = "Hamisi Bakari",
            ownerContact = "+255 712 345 678",
            location = "Kibaha North, Ward 3",
            healthStatus = HealthStatus.QUARANTINED,
            vaccinationHistory = listOf("FMD Vaccine (Jan 2025)", "Anthrax Spore (Aug 2024)", "LSD Vaccine (Nov 2024)"),
            lastCheckupDate = System.currentTimeMillis() - 86400000L * 2,
            notes = "Showing mild oral vesicles and salivation. Isolated in quarantine pen A."
        ),
        AnimalEntity(
            id = "ANM-1002",
            tagNumber = "TZ-KIB-8822",
            name = "Kibaha Dairy Herd Alpha",
            species = Species.CATTLE.displayName,
            breed = "Boran / Ankole",
            ageMonths = 24,
            gender = "Female",
            weightKg = 320.0,
            isHerdGroup = true,
            herdSize = 25,
            ownerName = "Hamisi Bakari",
            ownerContact = "+255 712 345 678",
            location = "Kibaha Central Farm",
            healthStatus = HealthStatus.HEALTHY,
            vaccinationHistory = listOf("FMD Annual (Jan 2025)", "CBPP (Sep 2024)"),
            lastCheckupDate = System.currentTimeMillis() - 86400000L * 5,
            notes = "Lactating dairy group. Daily milk yield 180 liters."
        ),
        AnimalEntity(
            id = "ANM-1003",
            tagNumber = "TZ-MOR-4102",
            name = "Chiku Goat Flock",
            species = Species.GOAT.displayName,
            breed = "Small East African Goat",
            ageMonths = 18,
            gender = "Female",
            weightKg = 35.0,
            isHerdGroup = true,
            herdSize = 40,
            ownerName = "Mama Chiku",
            ownerContact = "+255 784 991 200",
            location = "Morogoro Rural, Km 12",
            healthStatus = HealthStatus.SICK,
            vaccinationHistory = listOf("PPR Vaccine (Jul 2024)"),
            lastCheckupDate = System.currentTimeMillis() - 86400000L * 1,
            notes = "3 goats showing nasal discharge and coughing. Suspected PPR or contagious caprine pleuropneumonia."
        ),
        AnimalEntity(
            id = "ANM-1004",
            tagNumber = "TZ-DSM-0091",
            name = "Piglet Pen #4",
            species = Species.PIG.displayName,
            breed = "Large White Cross",
            ageMonths = 8,
            gender = "Male",
            weightKg = 65.0,
            isHerdGroup = true,
            herdSize = 12,
            ownerName = "John Kimaro",
            ownerContact = "+255 655 112 334",
            location = "Pugu Peri-Urban Zone",
            healthStatus = HealthStatus.HEALTHY,
            vaccinationHistory = listOf("Dewormed (Jan 2025)"),
            lastCheckupDate = System.currentTimeMillis() - 86400000L * 10,
            notes = "High biosecurity protocols maintained. Feed monitored."
        ),
        AnimalEntity(
            id = "ANM-1005",
            tagNumber = "TZ-MZA-5510",
            name = "Simba Bull",
            species = Species.CATTLE.displayName,
            breed = "Sahiwal Bull",
            ageMonths = 48,
            gender = "Male",
            weightKg = 620.0,
            isHerdGroup = false,
            herdSize = 1,
            ownerName = "Rashid Juma",
            ownerContact = "+255 713 008 776",
            location = "Mwanza South Pasture",
            healthStatus = HealthStatus.HEALTHY,
            vaccinationHistory = listOf("FMD Vaccine (Dec 2024)", "Blackleg (May 2024)"),
            lastCheckupDate = System.currentTimeMillis() - 86400000L * 14,
            notes = "Breeding bull. Excellent health score."
        )
    )

    val initialSymptomReports = listOf(
        SymptomReportEntity(
            id = "REP-2026-001",
            animalId = "ANM-1001",
            species = Species.CATTLE.displayName,
            affectedCount = 3,
            totalHerdCount = 25,
            mortalityCount = 0,
            selectedSymptoms = listOf("MOUTH_BLISTERS", "FOOT_LESIONS", "PROFUSE_SALIVATION", "HIGH_FEVER"),
            symptomOnsetDays = 2,
            latitude = -6.7783,
            longitude = 38.9221,
            locationName = "Kibaha Ward 3, Coast Region",
            triageSeverity = TriageSeverity.EMERGENCY,
            contagionRisk = ContagionRisk.CRITICAL,
            suspectedDiseases = listOf("Foot and Mouth Disease (FMD)"),
            recommendedActions = listOf(
                "Impose strict quarantine on the entire herd/farm.",
                "Halt all livestock movement to/from the village.",
                "Collect vesicular fluid/swab sample for lab confirmation.",
                "Apply mild antiseptic wash to oral and foot lesions."
            ),
            reportedAt = System.currentTimeMillis() - 86400000L * 2,
            syncStatus = SyncStatus.SYNCED,
            reporterName = "Paravet Joseph M.",
            reporterRole = "Community Livestock Officer",
            notes = "Lesions observed on tongue and interdigital space."
        ),
        SymptomReportEntity(
            id = "REP-2026-002",
            animalId = "ANM-1003",
            species = Species.GOAT.displayName,
            affectedCount = 5,
            totalHerdCount = 40,
            mortalityCount = 1,
            selectedSymptoms = listOf("RESPIRATORY_DISTRESS", "HIGH_FEVER", "LOSS_OF_APPETITE"),
            symptomOnsetDays = 3,
            latitude = -6.8278,
            longitude = 37.6591,
            locationName = "Morogoro Rural Sector 2",
            triageSeverity = TriageSeverity.HIGH,
            contagionRisk = ContagionRisk.HIGH,
            suspectedDiseases = listOf("Peste des Petits Ruminants (PPR)", "Contagious Caprine Pleuropneumonia"),
            recommendedActions = listOf(
                "Isolate dyspneic goats/sheep immediately.",
                "Notify district veterinary service for emergency PPR vaccination.",
                "Collect nasal swabs/serum for lab referral."
            ),
            reportedAt = System.currentTimeMillis() - 86400000L * 1,
            syncStatus = SyncStatus.PENDING,
            reporterName = "Grace Temba",
            reporterRole = "Lead Animal Health Technician",
            notes = "One dead goat found yesterday morning."
        )
    )

    val initialLabReferrals = listOf(
        LabReferralEntity(
            id = "LAB-2026-001",
            symptomReportId = "REP-2026-001",
            animalTag = "TZ-KIB-8821",
            species = Species.CATTLE.displayName,
            sampleType = "Vesicular Fluid & Epithelial Scrape",
            suspectedDisease = "Foot and Mouth Disease (FMD)",
            urgency = "URGENT",
            status = ReferralStatus.IN_TRANSIT,
            destinationLab = "National Veterinary Reference Laboratory, Dar es Salaam",
            collectorName = "Paravet Joseph M.",
            collectionDate = System.currentTimeMillis() - 86400000L * 1,
            testResults = null,
            resultNotes = "Sample packed in cold chain ice box (4°C). Cold chain intact.",
            trackingNumber = "EV-LAB-893012",
            qrCodeData = "EASYVET:LAB:EV-LAB-893012:TZ-KIB-8821:FMD"
        ),
        LabReferralEntity(
            id = "LAB-2026-002",
            symptomReportId = "REP-2026-002",
            animalTag = "TZ-MOR-4102",
            species = Species.GOAT.displayName,
            sampleType = "Nasal Swab & Whole Blood EDTA",
            suspectedDisease = "Peste des Petits Ruminants (PPR)",
            urgency = "HIGH",
            status = ReferralStatus.PENDING,
            destinationLab = "Zonal Veterinary Diagnostic Center, Morogoro",
            collectorName = "Grace Temba",
            collectionDate = System.currentTimeMillis() - 3600000L * 4,
            testResults = null,
            resultNotes = "Awaiting field courier pickup.",
            trackingNumber = "EV-LAB-893013",
            qrCodeData = "EASYVET:LAB:EV-LAB-893013:TZ-MOR-4102:PPR"
        )
    )

    val initialOutbreakAlerts = listOf(
        OutbreakAlertEntity(
            id = "ALT-2026-001",
            title = "Foot & Mouth Disease Outbreak Cluster",
            diseaseName = "Foot and Mouth Disease (FMD)",
            affectedSpecies = "Cattle, Buffalo, Pigs",
            severity = AlertSeverity.CRITICAL,
            latitude = -6.7783,
            longitude = 38.9221,
            radiusKm = 25.0,
            regionName = "Kibaha / Coast Region",
            activeCases = 18,
            mortalityCount = 2,
            riskScore = 88,
            advisory = "Quarantine active within 25km radius. Livestock markets suspended in Kibaha district. Report any blistering or salivation immediately.",
            reportedDate = System.currentTimeMillis() - 86400000L * 3,
            isConfirmed = true
        ),
        OutbreakAlertEntity(
            id = "ALT-2026-002",
            title = "Anthrax Suspected Spore Zone",
            diseaseName = "Anthrax (Bacillus anthracis)",
            affectedSpecies = "Cattle, Sheep, Goats",
            severity = AlertSeverity.CRITICAL,
            latitude = -6.1630,
            longitude = 35.7516,
            radiusKm = 15.0,
            regionName = "Dodoma Rural Corridor",
            activeCases = 4,
            mortalityCount = 6,
            riskScore = 95,
            advisory = "DO NOT CONSUME MEAT FROM UNINSPECTED SLAUGHTERS. Sudden animal mortality must be reported. Do not open carcasses.",
            reportedDate = System.currentTimeMillis() - 86400000L * 1,
            isConfirmed = true
        ),
        OutbreakAlertEntity(
            id = "ALT-2026-003",
            title = "Lumpy Skin Disease Vector Season Warning",
            diseaseName = "Lumpy Skin Disease (LSD)",
            affectedSpecies = "Cattle",
            severity = AlertSeverity.WARNING,
            latitude = -2.5164,
            longitude = 32.9000,
            radiusKm = 40.0,
            regionName = "Lake Victoria Zone / Mwanza",
            activeCases = 12,
            mortalityCount = 0,
            riskScore = 65,
            advisory = "High mosquito and biting fly density detected. Apply acaricide dips and vector repellent. Ring vaccination active.",
            reportedDate = System.currentTimeMillis() - 86400000L * 5,
            isConfirmed = true
        )
    )

    val initialAdvisories = listOf(
        AdvisoryEntity(
            id = "ADV-001",
            diseaseName = "Foot and Mouth Disease (FMD)",
            category = "Outbreak Control & Biosecurity",
            title = "FMD Field Outbreak Management Protocol",
            summary = "Immediate containment procedures to prevent viral spread across grazing routes and markets.",
            detailedSteps = listOf(
                "1. Isolate infected livestock in a dry, shaded quarantine corral at least 500m from healthy herds.",
                "2. Restrict all vehicle and visitor movement into farm premises. Use disinfectant footbaths (4% Sodium Carbonate or Citric Acid).",
                "3. Provide soft green forage and clean water to affected stock; wash mouth and hoof lesions with 1% potassium permanganate.",
                "4. Immediately notify the nearest Zonal Veterinary Officer for strain identification and ring vaccination."
            ),
            targetSpecies = listOf("Cattle", "Goat", "Sheep", "Pig"),
            urgencyLevel = "CRITICAL"
        ),
        AdvisoryEntity(
            id = "ADV-002",
            diseaseName = "Anthrax",
            category = "Zoonotic Emergency Protocol",
            title = "Anthrax Carcass Handling & Biosecurity Warning",
            summary = "Strict biosecurity measures for sudden livestock mortality to avoid human infection and spore contamination.",
            detailedSteps = listOf(
                "1. DO NOT OPEN OR PERFORM NECROPSY ON CARCASS! Spore formation occurs when internal fluids contact oxygen.",
                "2. Cover carcass with tarpaulins or thorn bushes to prevent scavenger birds and rodents from spreading contamination.",
                "3. Burn carcass in situ or deep bury under 2 meters of soil mixed with hydrated quicklime.",
                "4. Decontaminate soil around bleeding orifices with 10% formal-saline or heavy lime slurry."
            ),
            targetSpecies = listOf("Cattle", "Sheep", "Goat", "Horse"),
            urgencyLevel = "CRITICAL"
        ),
        AdvisoryEntity(
            id = "ADV-003",
            diseaseName = "Lumpy Skin Disease (LSD)",
            category = "Vector Control & Management",
            title = "LSD Field Vector Reduction & Treatment",
            summary = "Managing biting fly populations and supportive care for cattle suffering from skin nodules.",
            detailedSteps = listOf(
                "1. Spray cattle weekly with pyrethroid acaricides/repellents to reduce Stomoxys biting flies and mosquitoes.",
                "2. Apply antiseptic wound sprays (oxytetracycline/gentian violet) to open nodular lesions to prevent secondary bacterial fly-strike.",
                "3. Administer non-steroidal anti-inflammatory drugs (NSAIDs) for fever relief under veterinary guidance.",
                "4. Mobilize community for LSD ring vaccination ahead of seasonal rains."
            ),
            targetSpecies = listOf("Cattle", "Buffalo"),
            urgencyLevel = "HIGH"
        )
    )

    val initialUsers = listOf(
        UserEntity(
            id = "USR-001",
            fullName = "Dr. Sarah Jenkins",
            email = "vet@easyvet.com",
            passwordHash = "password123",
            phone = "+255 700 123 456",
            role = "Senior Veterinary Officer"
        ),
        UserEntity(
            id = "USR-002",
            fullName = "Hamisi Bakari",
            email = "farmer@easyvet.com",
            passwordHash = "password123",
            phone = "+255 712 345 678",
            role = "Livestock Farmer"
        )
    )
}
