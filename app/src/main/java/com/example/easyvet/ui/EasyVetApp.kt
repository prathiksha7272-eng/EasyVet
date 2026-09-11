package com.example.easyvet.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.ui.advisories.AdvisoriesScreen
import com.example.easyvet.ui.advisories.AdvisoriesViewModel
import com.example.easyvet.ui.auth.AuthScreen
import com.example.easyvet.ui.auth.AuthViewModel
import com.example.easyvet.ui.dashboard.DashboardScreen
import com.example.easyvet.ui.dashboard.DashboardViewModel
import com.example.easyvet.ui.lab.LabReferralScreen
import com.example.easyvet.ui.lab.LabReferralViewModel
import com.example.easyvet.ui.ledger.AnimalLedgerScreen
import com.example.easyvet.ui.ledger.AnimalLedgerViewModel
import com.example.easyvet.ui.riskmap.RiskMapScreen
import com.example.easyvet.ui.riskmap.RiskMapViewModel
import com.example.easyvet.ui.symptom.SymptomReportScreen
import com.example.easyvet.ui.symptom.SymptomReportViewModel
import com.example.easyvet.ui.theme.EasyVetTheme

sealed class Screen {
    data object Dashboard : Screen()
    data object SymptomReport : Screen()
    data object RiskMap : Screen()
    data object AnimalLedger : Screen()
    data class LabReferral(
        val animalTag: String = "",
        val species: String = "",
        val suspectedDisease: String = "",
        val reportId: String? = null
    ) : Screen()
    data class Advisories(
        val diseaseName: String = ""
    ) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyVetApp() {
    val context = LocalContext.current
    val app = context.applicationContext as EasyVetApplication

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Auth ViewModel
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(app)
    )
    val authState by authViewModel.uiState.collectAsState()

    // Shared ViewModels
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(app)
    )

    val symptomReportViewModel: SymptomReportViewModel = viewModel(
        factory = SymptomReportViewModel.Factory(app)
    )

    val riskMapViewModel: RiskMapViewModel = viewModel(
        factory = RiskMapViewModel.Factory(app)
    )

    val animalLedgerViewModel: AnimalLedgerViewModel = viewModel(
        factory = AnimalLedgerViewModel.Factory(app)
    )

    EasyVetTheme {
        val user = authState.currentUser

        if (user == null) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { currentScreen = Screen.Dashboard }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "EasyVet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${user.fullName} (${user.role})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { authViewModel.logout() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Sign Out",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { currentScreen = Screen.Dashboard },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.SymptomReport,
                            onClick = { currentScreen = Screen.SymptomReport },
                            icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Report") },
                            label = { Text("Report") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.RiskMap,
                            onClick = { currentScreen = Screen.RiskMap },
                            icon = { Icon(Icons.Default.Map, contentDescription = "Risk Map") },
                            label = { Text("Risk Map") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.AnimalLedger,
                            onClick = { currentScreen = Screen.AnimalLedger },
                            icon = { Icon(Icons.Default.Pets, contentDescription = "Ledger") },
                            label = { Text("Ledger") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.LabReferral,
                            onClick = { currentScreen = Screen.LabReferral() },
                            icon = { Icon(Icons.Default.Science, contentDescription = "Lab") },
                            label = { Text("Lab") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Advisories,
                            onClick = { currentScreen = Screen.Advisories() },
                            icon = { Icon(Icons.Default.Campaign, contentDescription = "Advisories") },
                            label = { Text("Advisories") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigateToSymptomReport = { currentScreen = Screen.SymptomReport },
                                onNavigateToAnimalRegistration = { currentScreen = Screen.AnimalLedger },
                                onNavigateToLabReferrals = { currentScreen = Screen.LabReferral() },
                                onNavigateToRiskMap = { currentScreen = Screen.RiskMap }
                            )
                        }

                        is Screen.SymptomReport -> {
                            SymptomReportScreen(
                                viewModel = symptomReportViewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onCreateLabReferral = { animalTag, species, suspectedDisease, reportId ->
                                    currentScreen = Screen.LabReferral(animalTag, species, suspectedDisease, reportId)
                                }
                            )
                        }

                        is Screen.RiskMap -> {
                            RiskMapScreen(
                                viewModel = riskMapViewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onNavigateToAdvisory = { diseaseName ->
                                    currentScreen = Screen.Advisories(diseaseName = diseaseName)
                                }
                            )
                        }

                        is Screen.AnimalLedger -> {
                            AnimalLedgerScreen(
                                viewModel = animalLedgerViewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onCreateLabReferral = { animalTag, species ->
                                    currentScreen = Screen.LabReferral(animalTag = animalTag, species = species)
                                },
                                onReportSymptoms = { _ ->
                                    currentScreen = Screen.SymptomReport
                                }
                            )
                        }

                        is Screen.LabReferral -> {
                            val labViewModel: LabReferralViewModel = viewModel(
                                key = "LabReferral_${screen.animalTag}_${screen.suspectedDisease}",
                                factory = LabReferralViewModel.Factory(
                                    application = app,
                                    animalTag = screen.animalTag,
                                    species = screen.species,
                                    suspectedDisease = screen.suspectedDisease,
                                    reportId = screen.reportId
                                )
                            )

                            LabReferralScreen(
                                viewModel = labViewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard }
                            )
                        }

                        is Screen.Advisories -> {
                            val advisoriesViewModel: AdvisoriesViewModel = viewModel(
                                key = "Advisories_${screen.diseaseName}",
                                factory = AdvisoriesViewModel.Factory(
                                    application = app,
                                    initialDiseaseFilter = screen.diseaseName
                                )
                            )

                            AdvisoriesScreen(
                                viewModel = advisoriesViewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard }
                            )
                        }
                    }
                }
            }
        }
    }
}
