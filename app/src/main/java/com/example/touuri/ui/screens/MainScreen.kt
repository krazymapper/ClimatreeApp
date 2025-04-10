package com.example.touuri.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.touuri.data.FirebaseService
import com.example.touuri.data.OsmAuthService
import com.example.touuri.ui.components.MapView
import com.example.touuri.ui.components.TreeForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Carte", "Ajouter un arbre", "Exporter")
    val firebaseService = remember { FirebaseService() }
    val osmService = remember { OsmAuthService(LocalContext.current) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Touuri") },
                actions = {
                    if (!osmService.isAuthenticated()) {
                        IconButton(onClick = { /* TODO: Show OSM login dialog */ }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Connexion OSM")
                        }
                    }
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
                0 -> MapView(
                    onTreeSelected = { tree ->
                        // TODO: Show tree details
                    }
                )
                1 -> TreeForm(
                    onSave = { treeData ->
                        // Save to Firebase
                        firebaseService.saveTree(treeData)
                        
                        // If OSM is authenticated, also save to OSM
                        if (osmService.isAuthenticated()) {
                            osmService.uploadTreeData(treeData) { success, osmId ->
                                if (success && osmId != null) {
                                    // Update tree in Firebase with OSM ID
                                    val updatedTree = treeData.copy(osmId = osmId)
                                    firebaseService.saveTree(updatedTree)
                                }
                            }
                        }
                    }
                )
                2 -> ExportScreen()
            }
        }
    }
}

@Composable
fun ExportScreen() {
    var isLoading by remember { mutableStateOf(false) }
    val firebaseService = remember { FirebaseService() }
    val exportService = remember { ExportService(LocalContext.current) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                isLoading = true
                // TODO: Implement export with coroutines
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Exporter en CSV")
            }
        }
    }
} 