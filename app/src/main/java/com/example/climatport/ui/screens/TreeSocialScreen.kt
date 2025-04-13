package com.example.climatport.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.climatree.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TreeSocialScreen(
    treeSocialService: TreeSocialService,
    authService: AuthService,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var trees by remember { mutableStateOf<List<TreeProfile>>(emptyList()) }
    var families by remember { mutableStateOf<List<TreeFamily>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedTab) {
        isLoading = true
        error = null
        try {
            when (selectedTab) {
                0 -> {
                    // Load nearby trees
                    val currentUser = authService.currentUser
                    if (currentUser != null) {
                        // In a real app, you'd get the user's location here
                        val location = GeoPoint(0.0, 0.0) // Placeholder
                        trees = treeSocialService.getNearbyTrees(location, 1000.0).getOrThrow()
                    }
                }
                1 -> {
                    // Load user's trees
                    val currentUser = authService.currentUser
                    if (currentUser != null) {
                        trees = treeSocialService.getUserTrees(currentUser.uid).getOrThrow()
                    }
                }
                2 -> {
                    // Load tree families
                    families = treeSocialService.getTreeFamilies().getOrThrow()
                }
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Découvrir") },
                icon = { Icon(Icons.Default.Explore, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Mes Arbres") },
                icon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Familles") },
                icon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null) }
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Erreur: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                when (selectedTab) {
                    0, 1 -> TreeList(trees = trees, onTreeClick = { /* Handle tree click */ })
                    2 -> FamilyList(families = families, onFamilyClick = { /* Handle family click */ })
                }
            }
        }
    }
}

@Composable
fun TreeList(
    trees: List<TreeProfile>,
    onTreeClick: (TreeProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(trees) { tree ->
            TreeCard(
                tree = tree,
                onClick = { onTreeClick(tree) }
            )
        }
    }
}

@Composable
fun TreeCard(
    tree: TreeProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = tree.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tree.species,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconText(
                    icon = Icons.Default.Favorite,
                    text = tree.likes.toString()
                )
                IconText(
                    icon = Icons.Default.Comment,
                    text = tree.comments.size.toString()
                )
                IconText(
                    icon = Icons.Default.CalendarToday,
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(tree.createdAt)
                )
            }
        }
    }
}

@Composable
fun IconText(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(text = text)
    }
}

@Composable
fun FamilyList(
    families: List<TreeFamily>,
    onFamilyClick: (TreeFamily) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(families) { family ->
            FamilyCard(
                family = family,
                onClick = { onFamilyClick(family) }
            )
        }
    }
}

@Composable
fun FamilyCard(
    family: TreeFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = family.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = family.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${family.treeIds.size} arbres dans cette famille",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 