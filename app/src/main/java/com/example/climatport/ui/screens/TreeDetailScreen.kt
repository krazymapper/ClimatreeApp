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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.climatree.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeDetailScreen(
    tree: TreeProfile,
    treeSocialService: TreeSocialService,
    authService: AuthService,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCommentDialog by remember { mutableStateOf(false) }
    var showMilestoneDialog by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var selectedMilestoneType by remember { mutableStateOf(MilestoneType.PLANTING) }
    var milestoneDescription by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tree.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Like the tree
                            treeSocialService.likeTree(tree.id)
                        }
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = "J'aime")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photos
            if (tree.photos.isNotEmpty()) {
                item {
                    AsyncImage(
                        model = tree.photos.first(),
                        contentDescription = "Photo de l'arbre",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Informations de base
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Informations",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow("Espèce", tree.species)
                        InfoRow("Propriétaire", tree.ownerId)
                        InfoRow(
                            "Date d'ajout",
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(tree.createdAt)
                        )
                    }
                }
            }

            // Description
            if (tree.description.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = tree.description)
                        }
                    }
                }
            }

            // Jalons
            if (tree.milestones.isNotEmpty()) {
                item {
                    Text(
                        text = "Jalons",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(tree.milestones) { milestone ->
                    MilestoneCard(milestone = milestone)
                }
            }

            // Commentaires
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commentaires",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(
                        onClick = { showCommentDialog = true }
                    ) {
                        Text("Ajouter un commentaire")
                    }
                }
            }
            items(tree.comments) { comment ->
                CommentCard(comment = comment)
            }
        }
    }

    // Dialogues
    if (showCommentDialog) {
        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text("Ajouter un commentaire") },
            text = {
                TextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Votre commentaire") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentUser = authService.currentUser
                        if (currentUser != null) {
                            treeSocialService.addComment(
                                tree.id,
                                Comment(
                                    userId = currentUser.uid,
                                    userName = currentUser.displayName ?: "Anonyme",
                                    text = newComment
                                )
                            )
                            showCommentDialog = false
                            newComment = ""
                        }
                    }
                ) {
                    Text("Publier")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showMilestoneDialog) {
        AlertDialog(
            onDismissRequest = { showMilestoneDialog = false },
            title = { Text("Ajouter un jalon") },
            text = {
                Column {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { }
                    ) {
                        MilestoneType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedMilestoneType = type
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = milestoneDescription,
                        onValueChange = { milestoneDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Description") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        treeSocialService.addMilestone(
                            tree.id,
                            Milestone(
                                type = selectedMilestoneType,
                                date = Date(),
                                description = milestoneDescription
                            )
                        )
                        showMilestoneDialog = false
                        milestoneDescription = ""
                    }
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMilestoneDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MilestoneCard(milestone: Milestone) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = milestone.type.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(milestone.date),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = milestone.description)
        }
    }
}

@Composable
fun CommentCard(comment: Comment) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = comment.userName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(comment.timestamp),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = comment.text)
        }
    }
} 