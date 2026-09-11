package com.example.easyvet.ui.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.easyvet.data.model.Species

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLabReferralDialog(
    prefilledTag: String = "",
    prefilledSpecies: String = "",
    prefilledDisease: String = "",
    onDismiss: () -> Unit,
    onSubmit: (
        animalTag: String,
        species: String,
        sampleType: String,
        suspectedDisease: String,
        urgency: String,
        destinationLab: String,
        collectorName: String,
        notes: String?
    ) -> Unit
) {
    var animalTag by remember { mutableStateOf(prefilledTag.ifBlank { "TZ-KIB-8821" }) }
    var species by remember { mutableStateOf(prefilledSpecies.ifBlank { Species.CATTLE.displayName }) }
    var sampleType by remember { mutableStateOf("Vesicular Fluid & Epithelial Scrape") }
    var suspectedDisease by remember { mutableStateOf(prefilledDisease.ifBlank { "Foot and Mouth Disease (FMD)" }) }
    var urgency by remember { mutableStateOf("HIGH") }
    var destinationLab by remember { mutableStateOf("National Veterinary Reference Laboratory, Dar es Salaam") }
    var collectorName by remember { mutableStateOf("Dr. Joseph M. (Field Paravet)") }
    var notes by remember { mutableStateOf("Packed in cold chain specimen kit (4°C).") }

    var sampleTypeExpanded by remember { mutableStateOf(false) }
    var urgencyExpanded by remember { mutableStateOf(false) }

    val sampleTypeOptions = listOf(
        "Vesicular Fluid & Epithelial Scrape",
        "Nasal / Oropharyngeal Swab",
        "Whole Blood EDTA / Serum",
        "Tissue Specimen / Biopsy",
        "Fecal Swab / Milk Sample",
        "Skin Nodular Biopsy"
    )

    val urgencyOptions = listOf("URGENT", "HIGH", "NORMAL", "ROUTINE")

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
                            text = "New Lab Sample Referral",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate sample tracking code & diagnostic request",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animal Tag ID & Species
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = animalTag,
                        onValueChange = { animalTag = it },
                        label = { Text("Animal Tag / ID") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = { Text("Species") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sample Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = sampleTypeExpanded,
                    onExpandedChange = { sampleTypeExpanded = !sampleTypeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = sampleType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sample Type / Specimen") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sampleTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sampleTypeExpanded,
                        onDismissRequest = { sampleTypeExpanded = false }
                    ) {
                        sampleTypeOptions.forEach { typeOption ->
                            DropdownMenuItem(
                                text = { Text(typeOption) },
                                onClick = {
                                    sampleType = typeOption
                                    sampleTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Suspected Disease
                OutlinedTextField(
                    value = suspectedDisease,
                    onValueChange = { suspectedDisease = it },
                    label = { Text("Suspected Pathogen / Disease") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Urgency Dropdown & Destination Lab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = urgencyExpanded,
                        onExpandedChange = { urgencyExpanded = !urgencyExpanded },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        OutlinedTextField(
                            value = urgency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Urgency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = urgencyExpanded,
                            onDismissRequest = { urgencyExpanded = false }
                        ) {
                            urgencyOptions.forEach { urg ->
                                DropdownMenuItem(
                                    text = { Text(urg) },
                                    onClick = {
                                        urgency = urg
                                        urgencyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = collectorName,
                        onValueChange = { collectorName = it },
                        label = { Text("Collector Officer") },
                        modifier = Modifier.weight(1.1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = destinationLab,
                    onValueChange = { destinationLab = it },
                    label = { Text("Destination Diagnostic Lab") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Packaging & Cold Chain Notes") },
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
                            onSubmit(
                                animalTag,
                                species,
                                sampleType,
                                suspectedDisease,
                                urgency,
                                destinationLab,
                                collectorName,
                                notes
                            )
                        },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Create Referral")
                    }
                }
            }
        }
    }
}
