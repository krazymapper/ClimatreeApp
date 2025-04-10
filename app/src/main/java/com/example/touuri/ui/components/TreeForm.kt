package com.example.touuri.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.touuri.data.TreeData
import com.example.touuri.data.TreeRecognitionResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeForm(
    onSave: (TreeData) -> Unit = {}
) {
    var species by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var diameter by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var healthStatus by remember { mutableStateOf(TreeData.HealthStatus.GOOD) }
    var showCamera by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<ByteArray?>(null) }

    if (showCamera) {
        TreeCamera(
            onTreeRecognized = { result, bitmap ->
                species = result.species
                showCamera = false
            },
            onError = { error ->
                // Handle error
                showCamera = false
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ajouter un arbre",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = { showCamera = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reconnaître l'arbre")
            }

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Espèce") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showCamera = true }) {
                        Icon(Icons.Default.Camera, contentDescription = "Reconnaître l'arbre")
                    }
                }
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Hauteur (m)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = diameter,
                onValueChange = { diameter = it },
                label = { Text("Diamètre (cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Âge estimé (années)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "État de santé",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TreeData.HealthStatus.values().forEach { status ->
                    FilterChip(
                        selected = healthStatus == status,
                        onClick = { healthStatus = status },
                        label = { Text(status.name) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    val treeData = TreeData(
                        species = species,
                        height = height.toDoubleOrNull(),
                        diameter = diameter.toDoubleOrNull(),
                        healthStatus = healthStatus,
                        age = age.toIntOrNull(),
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ajouter un arbre",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = species,
            onValueChange = { species = it },
            label = { Text("Espèce") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Hauteur (m)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = diameter,
            onValueChange = { diameter = it },
            label = { Text("Diamètre (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Âge estimé (années)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "État de santé",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TreeData.HealthStatus.values().forEach { status ->
                FilterChip(
                    selected = healthStatus == status,
                    onClick = { healthStatus = status },
                    label = { Text(status.name) }
                )
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = {
                val treeData = TreeData(
                    species = species,
                    height = height.toDoubleOrNull(),
                    diameter = diameter.toDoubleOrNull(),
                    healthStatus = healthStatus,
                    age = age.toIntOrNull(),
                    notes = notes
                )
                onSave(treeData)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enregistrer")
        }
    }
} 