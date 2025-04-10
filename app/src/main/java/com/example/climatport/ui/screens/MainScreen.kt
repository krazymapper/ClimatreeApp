package com.example.climatport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.climatport.data.FirebaseService
import com.example.climatport.ui.components.MapView
import com.example.climatport.ui.components.TreeForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Carte", "Ajouter un arbre", "Exporter")
    val firebaseService = remember { FirebaseService() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Climatport") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Déconnexion")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Map
                                    1 -> Icons.Default.Add
                                    else -> Icons.Default.Download
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> MapView()
                1 -> TreeForm(
                    onSave = { treeData ->
                        // TODO: Save tree data to Firebase
                    }
                )
                2 -> ExportScreen()
            }
        }
    }
}

@Composable
fun ExportScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { /* TODO: Implement export */ }
        ) {
            Text("Exporter en CSV")
        }
    }
} 