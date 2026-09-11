package com.example.easyvet.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.easyvet.data.model.HealthStatus
import com.example.easyvet.data.model.Species

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalRegistrationDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        tagNumber: String,
        name: String,
        species: String,
        breed: String,
        ageMonths: Int,
        gender: String,
        weightKg: Double,
        isHerdGroup: Boolean,
        herdSize: Int,
        ownerName: String,
        ownerContact: String,
        location: String,
        healthStatus: HealthStatus,
        initialVaccines: List<String>,
        notes: String
    ) -> Unit
) {
    var tagNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf(Species.CATTLE.displayName) }
    var breed by remember { mutableStateOf("") }
    var ageMonths by remember { mutableStateOf("24") }
    var gender by remember { mutableStateOf("Female") }
    var weightKg by remember { mutableStateOf("350") }
    var isHerdGroup by remember { mutableStateOf(false) }
    var herdSize by remember { mutableStateOf("10") }
    var ownerName by remember { mutableStateOf("") }
    var ownerContact by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedHealthStatus by remember { mutableStateOf(HealthStatus.HEALTHY) }
    var initialVaccinesInput by remember { mutableStateOf("FMD Vaccine, Anthrax Spore") }
    var notes by remember { mutableStateOf("") }

    var speciesDropdownExpanded by remember { mutableStateOf(false) }
    var healthDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Register Animal / Herd",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Assign tag ID & health records to database",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tag Number with RFID Scanner Simulation
                OutlinedTextField(
                    value = tagNumber,
                    onValueChange = { tagNumber = it },
                    label = { Text("Tag ID / RFID Barcode") },
                    placeholder = { Text("e.g. EV-TZ-8831") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            tagNumber = "RFID-${(100000..999999).random()}"
                        }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Simulate Scan RFID", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Name / Group Title
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Animal / Herd Name") },
                    placeholder = { Text("e.g. Bessie or Dairy Group Alpha") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Herd Group Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Register as Herd Group?", fontWeight = FontWeight.SemiBold)
                        Text("Toggle for multiple animals managed as a unit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isHerdGroup,
                        onCheckedChange = { isHerdGroup = it }
                    )
                }

                if (isHerdGroup) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = herdSize,
                        onValueChange = { herdSize = it },
                        label = { Text("Herd Head Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Species & Health Status Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Species Dropdown
                    ExposedDropdownMenuBox(
                        expanded = speciesDropdownExpanded,
                        onExpandedChange = { speciesDropdownExpanded = !speciesDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedSpecies,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Species") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = speciesDropdownExpanded,
                            onDismissRequest = { speciesDropdownExpanded = false }
                        ) {
                            Species.values().forEach { spec ->
                                DropdownMenuItem(
                                    text = { Text(spec.displayName) },
                                    onClick = {
                                        selectedSpecies = spec.displayName
                                        speciesDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Health Status Dropdown
                    ExposedDropdownMenuBox(
                        expanded = healthDropdownExpanded,
                        onExpandedChange = { healthDropdownExpanded = !healthDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedHealthStatus.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Health Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = healthDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = healthDropdownExpanded,
                            onDismissRequest = { healthDropdownExpanded = false }
                        ) {
                            HealthStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.label) },
                                    onClick = {
                                        selectedHealthStatus = status
                                        healthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breed, Age, Weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ageMonths,
                        onValueChange = { ageMonths = it },
                        label = { Text("Age (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Owner Details
                Text("Owner & Location Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Farmer / Owner Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ownerContact,
                        onValueChange = { ownerContact = it },
                        label = { Text("Contact Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Village / Ward") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Initial Vaccination History & Notes
                OutlinedTextField(
                    value = initialVaccinesInput,
                    onValueChange = { initialVaccinesInput = it },
                    label = { Text("Vaccination History (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Notes / Observations") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val vaxList = initialVaccinesInput.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }

                            onSubmit(
                                tagNumber,
                                name,
                                selectedSpecies,
                                breed,
                                ageMonths.toIntOrNull() ?: 12,
                                gender,
                                weightKg.toDoubleOrNull() ?: 100.0,
                                isHerdGroup,
                                herdSize.toIntOrNull() ?: 1,
                                ownerName,
                                ownerContact,
                                location,
                                selectedHealthStatus,
                                vaxList,
                                notes
                            )
                        },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Save Record")
                    }
                }
            }
        }
    }
}
