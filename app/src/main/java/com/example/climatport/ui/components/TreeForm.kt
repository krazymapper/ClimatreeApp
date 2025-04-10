package com.example.climatport.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.climatport.data.TreeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeForm(
    onSave: (TreeData) -> Unit = {}
) {
    var species by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var diameter by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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