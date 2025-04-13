package com.example.climatport.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.climatree.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    challengeService: TreeChallengeService,
    authService: AuthService,
    onChallengeClick: (TreeChallenge) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var challenges by remember { mutableStateOf<List<TreeChallenge>>(emptyList()) }
    var badges by remember { mutableStateOf<List<UserBadge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showNewBadgeAnimation by remember { mutableStateOf(false) }
    var newBadge by remember { mutableStateOf<UserBadge?>(null) }
    var showStats by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        isLoading = true
        try {
            when (selectedTab) {
                0 -> {
                    challenges = challengeService.getActiveChallenges().getOrThrow()
                }
                1 -> {
                    val currentUser = authService.currentUser
                    if (currentUser != null) {
                        val newBadges = challengeService.getUserBadges(currentUser.uid).getOrThrow()
                        // Vérifier si de nouveaux badges ont été obtenus
                        if (newBadges.size > badges.size) {
                            newBadge = newBadges.last()
                            showNewBadgeAnimation = true
                        }
                        badges = newBadges
                    }
                }
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Défis et Badges") },
                actions = {
                    IconButton(onClick = { showStats = !showStats }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Statistiques")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showStats) {
                UserStats(
                    badges = badges,
                    challenges = challenges,
                    modifier = Modifier.padding(16.dp)
                )
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Défis") },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mes Badges") },
                    icon = { Icon(Icons.Default.MilitaryTech, contentDescription = null) }
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
                        0 -> ChallengeList(
                            challenges = challenges,
                            onChallengeClick = onChallengeClick
                        )
                        1 -> BadgeList(badges = badges)
                    }
                }
            }
        }
    }

    // Animation du nouveau badge
    if (showNewBadgeAnimation && newBadge != null) {
        NewBadgeAnimation(
            badge = newBadge!!,
            onDismiss = {
                showNewBadgeAnimation = false
                newBadge = null
            }
        )
    }
}

@Composable
fun UserStats(
    badges: List<UserBadge>,
    challenges: List<TreeChallenge>,
    modifier: Modifier = Modifier
) {
    val totalBadges = badges.size
    val completedChallenges = challenges.count { it.status == ChallengeStatus.COMPLETED }
    val activeChallenges = challenges.count { it.status == ChallengeStatus.ACTIVE }
    
    val badgeTypes = BadgeType.values().associateWith { type ->
        badges.count { it.badgeType == type }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistiques",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.MilitaryTech,
                    value = totalBadges.toString(),
                    label = "Badges"
                )
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    value = completedChallenges.toString(),
                    label = "Défis complétés"
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    value = activeChallenges.toString(),
                    label = "Défis actifs"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Progression des Badges",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            badgeTypes.forEach { (type, count) ->
                BadgeProgress(
                    type = type,
                    count = count,
                    total = badges.size
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun BadgeProgress(
    type: BadgeType,
    count: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = when (type) {
                    BadgeType.TREE_EXPLORER -> "Explorateur"
                    BadgeType.TREE_MASTER -> "Maître"
                    BadgeType.TREE_LEGEND -> "Légende"
                    BadgeType.URBAN_FORESTER -> "Forestier"
                    BadgeType.SPECIES_EXPERT -> "Expert"
                    BadgeType.URBAN_GARDENER -> "Jardinier"
                    BadgeType.CONSERVATION_HERO -> "Héros"
                    BadgeType.COMMUNITY_LEADER -> "Leader"
                    BadgeType.WEEKLY_CHAMPION -> "Champion"
                    BadgeType.EVENT_PARTICIPANT -> "Participant"
                    BadgeType.COMMUNITY_BUILDER -> "Bâtisseur"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$count/$total",
                style = MaterialTheme.typography.bodySmall
            )
        }
        LinearProgressIndicator(
            progress = count.toFloat() / total,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun NewBadgeAnimation(
    badge: UserBadge,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(0f) }
    var opacity by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
        opacity = 1f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .scale(scale)
                .alpha(opacity)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when (badge.badgeType) {
                        BadgeType.TREE_EXPLORER -> Icons.Default.Explore
                        BadgeType.TREE_MASTER -> Icons.Default.Star
                        BadgeType.TREE_LEGEND -> Icons.Default.EmojiEvents
                        BadgeType.URBAN_FORESTER -> Icons.Default.Park
                        BadgeType.SPECIES_EXPERT -> Icons.Default.Science
                        BadgeType.URBAN_GARDENER -> Icons.Default.Grass
                        BadgeType.CONSERVATION_HERO -> Icons.Default.Favorite
                        BadgeType.COMMUNITY_LEADER -> Icons.Default.Group
                        BadgeType.WEEKLY_CHAMPION -> Icons.Default.MilitaryTech
                        BadgeType.EVENT_PARTICIPANT -> Icons.Default.Celebration
                        BadgeType.COMMUNITY_BUILDER -> Icons.Default.Handshake
                    },
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nouveau Badge Débloqué!",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (badge.badgeType) {
                        BadgeType.TREE_EXPLORER -> "Explorateur d'Arbres"
                        BadgeType.TREE_MASTER -> "Maître des Arbres"
                        BadgeType.TREE_LEGEND -> "Légende des Arbres"
                        BadgeType.URBAN_FORESTER -> "Forestier Urbain"
                        BadgeType.SPECIES_EXPERT -> "Expert en Espèces"
                        BadgeType.URBAN_GARDENER -> "Jardinier Urbain"
                        BadgeType.CONSERVATION_HERO -> "Héros de la Conservation"
                        BadgeType.COMMUNITY_LEADER -> "Leader Communautaire"
                        BadgeType.WEEKLY_CHAMPION -> "Champion Hebdomadaire"
                        BadgeType.EVENT_PARTICIPANT -> "Participant d'Événement"
                        BadgeType.COMMUNITY_BUILDER -> "Bâtisseur de Communauté"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Fermer")
                }
            }
        }
    }
}

@Composable
fun ChallengeList(
    challenges: List<TreeChallenge>,
    onChallengeClick: (TreeChallenge) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(challenges) { challenge ->
            ChallengeCard(
                challenge = challenge,
                onClick = { onChallengeClick(challenge) }
            )
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: TreeChallenge,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Chip(
                    onClick = {},
                    colors = ChipDefaults.chipColors(
                        containerColor = when (challenge.type) {
                            ChallengeType.WEEKLY_CHALLENGE -> Color(0xFF4CAF50)
                            ChallengeType.SPECIAL_EVENT -> Color(0xFF2196F3)
                            ChallengeType.COMMUNITY_CHALLENGE -> Color(0xFF9C27B0)
                            ChallengeType.URBAN_EXPLORATION -> Color(0xFFFF9800)
                        }
                    )
                ) {
                    Text(
                        text = when (challenge.type) {
                            ChallengeType.WEEKLY_CHALLENGE -> "Hebdomadaire"
                            ChallengeType.SPECIAL_EVENT -> "Événement"
                            ChallengeType.COMMUNITY_CHALLENGE -> "Communauté"
                            ChallengeType.URBAN_EXPLORATION -> "Exploration"
                        },
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = challenge.description)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progression
            LinearProgressIndicator(
                progress = challenge.currentCount.toFloat() / challenge.targetCount,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${challenge.currentCount}/${challenge.targetCount} arbres",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${challenge.participants.size} participants",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Dates
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Début: ${SimpleDateFormat("dd/MM", Locale.getDefault()).format(challenge.startDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Fin: ${SimpleDateFormat("dd/MM", Locale.getDefault()).format(challenge.endDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun BadgeList(
    badges: List<UserBadge>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(badges) { badge ->
            BadgeCard(badge = badge)
        }
    }
}

@Composable
fun BadgeCard(
    badge: UserBadge,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (badge.badgeType) {
                    BadgeType.TREE_EXPLORER -> Icons.Default.Explore
                    BadgeType.TREE_MASTER -> Icons.Default.Star
                    BadgeType.TREE_LEGEND -> Icons.Default.EmojiEvents
                    BadgeType.URBAN_FORESTER -> Icons.Default.Park
                    BadgeType.SPECIES_EXPERT -> Icons.Default.Science
                    BadgeType.URBAN_GARDENER -> Icons.Default.Grass
                    BadgeType.CONSERVATION_HERO -> Icons.Default.Favorite
                    BadgeType.COMMUNITY_LEADER -> Icons.Default.Group
                    BadgeType.WEEKLY_CHAMPION -> Icons.Default.MilitaryTech
                    BadgeType.EVENT_PARTICIPANT -> Icons.Default.Celebration
                    BadgeType.COMMUNITY_BUILDER -> Icons.Default.Handshake
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = when (badge.badgeType) {
                        BadgeType.TREE_EXPLORER -> "Explorateur d'Arbres"
                        BadgeType.TREE_MASTER -> "Maître des Arbres"
                        BadgeType.TREE_LEGEND -> "Légende des Arbres"
                        BadgeType.URBAN_FORESTER -> "Forestier Urbain"
                        BadgeType.SPECIES_EXPERT -> "Expert en Espèces"
                        BadgeType.URBAN_GARDENER -> "Jardinier Urbain"
                        BadgeType.CONSERVATION_HERO -> "Héros de la Conservation"
                        BadgeType.COMMUNITY_LEADER -> "Leader Communautaire"
                        BadgeType.WEEKLY_CHAMPION -> "Champion Hebdomadaire"
                        BadgeType.EVENT_PARTICIPANT -> "Participant d'Événement"
                        BadgeType.COMMUNITY_BUILDER -> "Bâtisseur de Communauté"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Niveau ${badge.level}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(badge.earnedDate),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 