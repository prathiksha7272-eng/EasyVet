package com.example.easyvet

import android.app.Application
import com.example.easyvet.data.local.EasyVetDatabase
import com.example.easyvet.data.local.UserSessionManager
import com.example.easyvet.data.repository.AdvisoryRepository
import com.example.easyvet.data.repository.AnimalRepository
import com.example.easyvet.data.repository.LabReferralRepository
import com.example.easyvet.data.repository.RiskMapRepository
import com.example.easyvet.data.repository.SymptomReportRepository
import com.example.easyvet.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EasyVetApplication : Application() {

    val database: EasyVetDatabase by lazy {
        EasyVetDatabase.getInstance(this)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }

    val userSessionManager: UserSessionManager by lazy {
        UserSessionManager(this)
    }

    val animalRepository: AnimalRepository by lazy {
        AnimalRepository(database.animalDao())
    }

    val symptomReportRepository: SymptomReportRepository by lazy {
        SymptomReportRepository(database.symptomReportDao())
    }

    val labReferralRepository: LabReferralRepository by lazy {
        LabReferralRepository(database.labReferralDao())
    }

    val riskMapRepository: RiskMapRepository by lazy {
        RiskMapRepository(database.outbreakAlertDao())
    }

    val advisoryRepository: AdvisoryRepository by lazy {
        AdvisoryRepository(database.advisoryDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-fill seed data if empty
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.ensureSeedData()
            animalRepository.ensureSeedData()
            symptomReportRepository.ensureSeedData()
            labReferralRepository.ensureSeedData()
            riskMapRepository.ensureSeedData()
            advisoryRepository.ensureSeedData()
        }
    }
}
