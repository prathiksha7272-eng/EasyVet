package com.example.easyvet.ui.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyvet.data.local.entity.AnimalEntity
import com.example.easyvet.data.model.HealthStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalLedgerScreen(
    viewModel: AnimalLedgerViewModel,
    onNavigateBack: () -> Unit = {},
    onCreateLabReferral: (animalTag: String, species: String) -> Unit = { _, _ -> },
    onReportSymptoms: (animalTag: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Animal & Herd Health Ledger",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.totalAnimalsCount} Total Livestock Registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowRegisterDialog(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Register Animal or Herd")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by Tag ID, Name, Owner, or Village...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Species Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val speciesList = listOf("All", "Cattle", "Buffalo", "Goat", "Sheep", "Pig", "Poultry")
                    items(speciesList) { species ->
                        FilterChip(
                            selected = uiState.selectedSpecies == species,
                            onClick = { viewModel.setSpeciesFilter(species) },
                            label = { Text(species) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Type Toggle Row: All / Individual / Herd Groups
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = uiState.selectedTypeFilter == LedgerTypeFilter.ALL,
                        onClick = { viewModel.setTypeFilter(LedgerTypeFilter.ALL) },
                        label = { Text("All Records") }
                    )
                    FilterChip(
                        selected = uiState.selectedTypeFilter == LedgerTypeFilter.INDIVIDUAL,
                        onClick = { viewModel.setTypeFilter(LedgerTypeFilter.INDIVIDUAL) },
                        label = { Text("Individuals") }
                    )
                    FilterChip(
                        selected = uiState.selectedTypeFilter == LedgerTypeFilter.HERD_GROUP,
                        onClick = { viewModel.setTypeFilter(LedgerTypeFilter.HERD_GROUP) },
                        label = { Text("Herd Groups") }
                    )
                }
            }

            // Summary Stats Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "Total Headcount",
                    value = "${uiState.totalAnimalsCount}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Sick / Quarantined",
                    value = "${uiState.sickOrQuarantinedCount}",
                    color = if (uiState.sickOrQuarantinedCount > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Herd Groups",
                    value = "${uiState.herdGroupsCount}",
                    color = Color(0xFF00897B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animal / Herd List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.filteredAnimals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No animal records found matching your filters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(uiState.filteredAnimals, key = { it.id }) { animal ->
                    AnimalCard(
                        animal = animal,
                        onClick = { viewModel.selectAnimal(animal) }
                    )
                }
            }
        }
    }

    // Animal Details Modal Sheet
    if (uiState.selectedAnimal != null) {
        AnimalDetailSheet(
            animal = uiState.selectedAnimal!!,
            onDismiss = { viewModel.selectAnimal(null) },
            onUpdateHealthStatus = { newStatus ->
                viewModel.updateAnimalHealthStatus(uiState.selectedAnimal!!, newStatus)
            },
            onAddVaccineRecord = { record ->
                viewModel.addVaccinationRecord(uiState.selectedAnimal!!, record)
            },
            onCreateLabReferral = onCreateLabReferral,
            onReportSymptoms = onReportSymptoms
        )
    }

    // Register Animal Dialog
    if (uiState.showRegisterDialog) {
        AnimalRegistrationDialog(
            onDismiss = { viewModel.setShowRegisterDialog(false) },
            onSubmit = { tag, name, spec, breed, age, gender, weight, isHerd, herdSize, owner, contact, location, status, vax, notes ->
                viewModel.registerNewAnimal(
                    tagNumber = tag,
                    name = name,
                    species = spec,
                    breed = breed,
                    ageMonths = age,
                    gender = gender,
                    weightKg = weight,
                    isHerdGroup = isHerd,
                    herdSize = herdSize,
                    ownerName = owner,
                    ownerContact = contact,
                    location = location,
                    healthStatus = status,
                    initialVaccines = vax,
                    notes = notes
                )
            }
        )
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnimalCard(
    animal: AnimalEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Species / Group Icon Badge
            Surface(
                shape = CircleShape,
                color = if (animal.isHerdGroup) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (animal.isHerdGroup) Icons.Default.Groups else Icons.Default.Pets,
                        contentDescription = null,
                        tint = if (animal.isHerdGroup) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = animal.tagNumber,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HealthStatusBadge(status = animal.healthStatus)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = animal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (animal.isHerdGroup) {
                        "${animal.species} Herd (${animal.herdSize} Animals) • ${animal.breed}"
                    } else {
                        "${animal.species} • ${animal.breed} • ${animal.ageMonths} mos"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${animal.ownerName} (${animal.location})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
