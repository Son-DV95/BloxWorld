package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AvatarItem
import com.example.data.LevelEntity
import com.example.data.PlayerProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloxMainScreen(viewModel: BloxViewModel) {
    val currentTab = viewModel.currentTab
    val levels by viewModel.levels.collectAsStateWithLifecycle()
    val profile by viewModel.playerProfile.collectAsStateWithLifecycle()

    // Screen Layout
    Scaffold(
        bottomBar = {
            if (viewModel.activePlayingLevel == null && viewModel.activeEditingLevel == null) {
                NavigationBar(
                    containerColor = Color(0xFFF3F4F9),
                    contentColor = Color(0xFF44474E)
                ) {
                    NavigationBarItem(
                        selected = currentTab == "discover",
                        onClick = { viewModel.selectTab("discover") },
                        label = { Text("Games", color = if (currentTab == "discover") Color(0xFF001D36) else Color(0xFF44474E), fontWeight = if (currentTab == "discover") FontWeight.Bold else FontWeight.Medium) },
                        icon = { Icon(Icons.Filled.Gamepad, contentDescription = "Discover", tint = if (currentTab == "discover") Color(0xFF001D36) else Color(0xFF44474E)) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD1E4FF))
                    )
                    NavigationBarItem(
                        selected = currentTab == "create",
                        onClick = { viewModel.selectTab("create") },
                        label = { Text("Create", color = if (currentTab == "create") Color(0xFF001D36) else Color(0xFF44474E), fontWeight = if (currentTab == "create") FontWeight.Bold else FontWeight.Medium) },
                        icon = { Icon(Icons.Filled.Build, contentDescription = "Create", tint = if (currentTab == "create") Color(0xFF001D36) else Color(0xFF44474E)) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD1E4FF))
                    )
                    NavigationBarItem(
                        selected = currentTab == "avatar",
                        onClick = { viewModel.selectTab("avatar") },
                        label = { Text("Avatar", color = if (currentTab == "avatar") Color(0xFF001D36) else Color(0xFF44474E), fontWeight = if (currentTab == "avatar") FontWeight.Bold else FontWeight.Medium) },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Avatar", tint = if (currentTab == "avatar") Color(0xFF001D36) else Color(0xFF44474E)) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD1E4FF))
                    )
                    NavigationBarItem(
                        selected = currentTab == "ai_helper",
                        onClick = { viewModel.selectTab("ai_helper") },
                        label = { Text("BloxAI", color = if (currentTab == "ai_helper") Color(0xFF001D36) else Color(0xFF44474E), fontWeight = if (currentTab == "ai_helper") FontWeight.Bold else FontWeight.Medium) },
                        icon = { Icon(Icons.Filled.SmartToy, contentDescription = "AI helper", tint = if (currentTab == "ai_helper") Color(0xFF001D36) else Color(0xFF44474E)) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFD1E4FF))
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Main views based on states
            if (viewModel.activePlayingLevel != null) {
                PlayroomScreen(viewModel)
            } else if (viewModel.activeEditingLevel != null) {
                StudioEditorScreen(viewModel)
            } else {
                when (currentTab) {
                    "discover" -> DiscoverScreen(levels, profile, onPlayLevel = { viewModel.startPlaying(it) })
                    "create" -> CreateScreen(levels, profile, viewModel)
                    "avatar" -> AvatarShopScreen(profile, viewModel)
                    "ai_helper" -> AiLoungeScreen(viewModel)
                }
            }
        }
    }
}

// ==========================================
// 🎮 1. DISCOVER SCREEN (GAMES HUB)
// ==========================================
@Composable
fun DiscoverScreen(
    levels: List<LevelEntity>,
    profile: PlayerProfileEntity?,
    onPlayLevel: (LevelEntity) -> Unit
) {
    val defaultProfile = profile ?: PlayerProfileEntity()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper Panel (Gold Pill Robux count)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    color = Color(0xFF74777F),
                    fontSize = 14.sp
                )
                Text(
                    text = defaultProfile.username,
                    color = Color(0xFF1A1C1E),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Robux Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD1E4FF))
                    .border(1.dp, Color(0xFF0061A4).copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = "Robux",
                    tint = Color(0xFF001D36),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${defaultProfile.robux} R$",
                    color = Color(0xFF001D36),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Discover Games",
            color = Color(0xFF1A1C1E),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Jump into user and developer created interactive worlds!",
            color = Color(0xFF74777F),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Game Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(levels) { level ->
                GameCard(level = level, onPlay = { onPlayLevel(level) })
            }
        }
    }
}

@Composable
fun GameCard(level: LevelEntity, onPlay: () -> Unit) {
    // Generate beautiful procedural gradient based on name/seed
    val gradientColors = when (level.thumbnailSeed) {
        "obby" -> listOf(Color(0xFFEA580C), Color(0xFFF97316), Color(0xFFFDBA74))
        "lava" -> listOf(Color(0xFFB91C1C), Color(0xFFEF4444), Color(0xFFFCA5A5))
        "clouds" -> listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
        else -> listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFF94A3B8))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(16.dp))
            .clickable { onPlay() }
            .testTag("game_card_${level.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Visual Banner with thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Brush.linearGradient(colors = gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                // Background aesthetics (drawn blocks or simple layout symbol)
                Icon(
                    imageVector = if (level.thumbnailSeed == "lava") Icons.Filled.LocalFireDepartment else Icons.Filled.Gamepad,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(60.dp)
                )
                
                // Overlay text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            }

            // Info panel
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = level.name,
                    color = Color(0xFF1A1C1E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = "By ${level.creator}",
                    color = Color(0xFF74777F),
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = level.description,
                    color = Color(0xFF44474E),
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Plays",
                            tint = Color(0xFF74777F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${level.plays} plays",
                            color = Color(0xFF74777F),
                            fontSize = 10.sp
                        )
                    }

                    if (level.highScore > 0.0f) {
                        Text(
                            text = "Best: %.2fs".format(level.highScore),
                            color = Color(0xFF0061A4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Play Button
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("play_button_${level.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 🛠️ 2. CREATE SCREEN (STUDIO ENTRY)
// ==========================================
@Composable
fun CreateScreen(
    levels: List<LevelEntity>,
    profile: PlayerProfileEntity?,
    viewModel: BloxViewModel
) {
    val userCreatedLevels = levels.filter { it.isUserCreated }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Blox Studio",
                color = Color(0xFF1A1C1E),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Design, build, and publish your own 2D levels. Generate immersive story lore using Gemini AI!",
                color = Color(0xFF74777F),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (userCreatedLevels.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.DashboardCustomize,
                            contentDescription = "No levels",
                            tint = Color.Gray,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No custom levels yet!",
                            color = Color(0xFF1A1C1E),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the '+' button below to open Blox Studio and build your very first speed obstacle course!",
                            color = Color(0xFF74777F),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(userCreatedLevels) { level ->
                        UserLevelItem(level = level, viewModel = viewModel)
                    }
                }
            }
        }

        // Floating action button to create level
        FloatingActionButton(
            onClick = { viewModel.openLevelCreator(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_level_fab"),
            containerColor = Color(0xFF0061A4),
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Create New")
        }
    }
}

@Composable
fun UserLevelItem(level: LevelEntity, viewModel: BloxViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level.name,
                    color = Color(0xFF1A1C1E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = level.description,
                    color = Color(0xFF74777F),
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "Plays: ${level.plays}  |  ",
                        color = Color(0xFF74777F),
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (level.highScore > 0f) "Best: %.2fs".format(level.highScore) else "Unbeaten",
                        color = Color(0xFF0061A4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Actions row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { viewModel.startPlaying(level) },
                    modifier = Modifier.background(Color(0xFF0061A4), CircleShape)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White)
                }
                IconButton(
                    onClick = { viewModel.openLevelCreator(level) },
                    modifier = Modifier.background(Color(0xFFD1E4FF), CircleShape)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF001D36))
                }
                IconButton(
                    onClick = { viewModel.deleteLevel(level.id) },
                    modifier = Modifier.background(Color(0xFFFBEAEA), CircleShape)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFBA1A1A))
                }
            }
        }
    }
}

// ==========================================
// 👕 3. AVATAR SHOP & CUSTOMIZER
// ==========================================
@Composable
fun AvatarShopScreen(profile: PlayerProfileEntity?, viewModel: BloxViewModel) {
    val defaultProfile = profile ?: PlayerProfileEntity()
    var selectedCategory by remember { mutableStateOf("hat") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper balance pane
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Avatar Customizer", color = Color(0xFF1A1C1E), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Unlock unique gears and styles with your Robux!", color = Color(0xFF74777F), fontSize = 12.sp)
            }

            // Robux Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD1E4FF))
                    .border(1.dp, Color(0xFF0061A4).copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.MonetizationOn, contentDescription = "Robux", tint = Color(0xFF001D36), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${defaultProfile.robux} R$", color = Color(0xFF001D36), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Split view layout: Top shows live avatar preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(16.dp))
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Checkerboard pattern or ambient circle background
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF0F6))
                        .align(Alignment.Center)
                )

                // Render dynamically compiled Avatar!
                AvatarCanvas(
                    profile = defaultProfile,
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category scroll tab buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("hat" to "Hats", "face" to "Faces", "shirt" to "Shirts", "pants" to "Pants").forEach { (cat, label) ->
                val active = selectedCategory == cat
                Button(
                    onClick = { selectedCategory = cat },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) Color(0xFFD1E4FF) else Color(0xFFEEF0F6),
                        contentColor = if (active) Color(0xFF001D36) else Color(0xFF44474E)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Items listing grid matching category
        val categoryItems = AvatarItem.ALL_ITEMS.filter { it.category == selectedCategory }
        val unlockedItemIds = defaultProfile.unlockedItemIds.split(",")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categoryItems) { item ->
                val unlocked = unlockedItemIds.contains(item.id)
                val equipped = when (item.category) {
                    "hat" -> defaultProfile.equippedHat == item.id
                    "face" -> defaultProfile.equippedFace == item.id
                    "shirt" -> defaultProfile.equippedShirt == item.id
                    "pants" -> defaultProfile.equippedPants == item.id
                    else -> false
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(12.dp))
                        .border(
                            width = if (equipped) 2.dp else 1.dp,
                            color = if (equipped) Color(0xFF0061A4) else Color(0xFFDDE2EA),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Colored visual icon box representative of the item
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(android.graphics.Color.parseColor(item.colorHex)).copy(alpha = 0.6f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (item.category) {
                                    "hat" -> Icons.Filled.MilitaryTech
                                    "face" -> Icons.Filled.Face
                                    "shirt" -> Icons.Filled.Checkroom
                                    else -> Icons.Filled.Checkroom
                                },
                                contentDescription = item.name,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(item.name, color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                        Text(item.description, color = Color(0xFF74777F), fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interaction Button
                        if (equipped) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFFEEF0F6),
                                    disabledContentColor = Color(0xFF74777F)
                                )
                            ) {
                                Text("Equipped", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (unlocked) {
                            Button(
                                onClick = { viewModel.equipItem(item.category, item.id) },
                                modifier = Modifier.fillMaxWidth().height(32.dp).testTag("equip_${item.id}"),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4))
                            ) {
                                Text("Equip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.purchaseItem(item) },
                                modifier = Modifier.fillMaxWidth().height(32.dp).testTag("buy_${item.id}"),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E4FF), contentColor = Color(0xFF001D36))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = Color(0xFF001D36), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${item.price} R$", color = Color(0xFF001D36), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🤖 4. BLOXAI (BUILDERMAN CHAT LOUNGE)
// ==========================================
@Composable
fun AiLoungeScreen(viewModel: BloxViewModel) {
    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0061A4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = "Builderman", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Builderman AI", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("AI Level-Dev Copilot", color = Color(0xFF0061A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.background(Color(0xFFEEF0F6), CircleShape)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Clear Chat", tint = Color(0xFF44474E))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chat Log List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF3F4F9), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.chatHistory) { msg ->
                val isBuilder = msg.sender == "builder"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isBuilder) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isBuilder) 2.dp else 16.dp,
                            bottomEnd = if (isBuilder) 16.dp else 2.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBuilder) Color(0xFFEEF0F6) else Color(0xFFD1E4FF)
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isBuilder) "🛠️ Builderman AI" else "🧑‍💻 You",
                                color = if (isBuilder) Color(0xFF0061A4) else Color(0xFF001D36),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = if (isBuilder) Color(0xFF1A1C1E) else Color(0xFF001D36),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (viewModel.isChatLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF0F6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Builderman is scripting... ⚡",
                                color = Color(0xFF74777F),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset prompts suggestion chip row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Give me an Obby layout", "Avatar item ideas").forEach { chipText ->
                Card(
                    modifier = Modifier
                        .clickable { viewModel.sendChatMessage(chipText) }
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(50)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = chipText,
                        color = Color(0xFF44474E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Text input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Ask Builderman about game building...", color = Color(0xFF74777F), fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1A1C1E),
                    unfocusedTextColor = Color(0xFF1A1C1E),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF0061A4),
                    unfocusedBorderColor = Color(0xFFDDE2EA)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input")
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage(messageText)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(if (messageText.isNotBlank()) Color(0xFF0061A4) else Color(0xFFEEF0F6), CircleShape)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank()) Color.White else Color(0xFF74777F)
                )
            }
        }
    }
}

// ==========================================
// 🛠️ 5. STUDIO LEVEL EDITOR SCREEN
// ==========================================
@Composable
fun StudioEditorScreen(viewModel: BloxViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Upper action options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.exitEditor() },
                    modifier = Modifier.background(Color(0xFFEEF0F6), CircleShape)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Exit", tint = Color(0xFF44474E))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Studio", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.generateLevelLore() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E4FF), contentColor = Color(0xFF001D36)),
                    enabled = !viewModel.isGeneratingLore
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (viewModel.isGeneratingLore) "Writing..." else "AI Lore", fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { viewModel.saveAndPublishLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4), contentColor = Color.White),
                    enabled = viewModel.editorLevelName.isNotBlank()
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Name & description panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                OutlinedTextField(
                    value = viewModel.editorLevelName,
                    onValueChange = { viewModel.editorLevelName = it },
                    label = { Text("Level Name", color = Color(0xFF74777F), fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedBorderColor = Color(0xFF0061A4),
                        unfocusedBorderColor = Color(0xFFDDE2EA)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("editor_name_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = viewModel.editorLevelDescription,
                    onValueChange = { viewModel.editorLevelDescription = it },
                    label = { Text("Level Story Description", color = Color(0xFF74777F), fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1A1C1E),
                        unfocusedTextColor = Color(0xFF1A1C1E),
                        focusedBorderColor = Color(0xFF0061A4),
                        unfocusedBorderColor = Color(0xFFDDE2EA)
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // The GRID board (Click-To-Paint)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(8.dp))
        ) {
            val cellW = maxWidth / 20
            val cellH = maxHeight / 12

            // Custom drag/click drawing grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val col = (offset.x / cellW.toPx()).toInt()
                            val row = (offset.y / cellH.toPx()).toInt()
                            viewModel.drawTile(row, col)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val col = (change.position.x / cellW.toPx()).toInt()
                            val row = (change.position.y / cellH.toPx()).toInt()
                            viewModel.drawTile(row, col)
                        }
                    }
            ) {
                // Background grid lines and drawing content
                for (row in 0..11) {
                    for (col in 0..19) {
                        val idx = row * 20 + col
                        val tile = viewModel.editorGrid.getOrNull(idx) ?: '.'

                        Box(
                            modifier = Modifier
                                .offset(x = cellW * col, y = cellH * row)
                                .size(cellW, cellH)
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f))
                                .background(getTileColor(tile))
                        ) {
                            if (tile != '.') {
                                Text(
                                    text = tile.toString(),
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Studio Palette tools (M3 segment list)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3F4F9), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(12.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                'B' to "Brick",
                'S' to "Spike",
                'T' to "Spring",
                'P' to "Speed",
                'C' to "Coin",
                'A' to "Start",
                'F' to "Goal",
                '.' to "Eraser"
            ).forEach { (char, label) ->
                val active = viewModel.editorSelectedTile == char
                Card(
                    onClick = { viewModel.editorSelectedTile = char },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("palette_$char"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) Color(0xFFD1E4FF) else Color(0xFFEEF0F6)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (char == '.') "❌" else char.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (active) Color(0xFF001D36) else Color(0xFF44474E)
                        )
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color(0xFF001D36) else Color(0xFF74777F)
                        )
                    }
                }
            }
        }
    }
}

private fun getTileColor(tile: Char): Color {
    return when (tile) {
        'B' -> Color(0xFF5A6170) // Solid Grey Brick Wall
        'S' -> Color(0xFFE53935) // Red spikes/lava
        'T' -> Color(0xFF1E88E5) // Blue spring
        'P' -> Color(0xFF8E24AA) // Purple speedpad
        'C' -> Color(0xFFFBC02D) // Golden Robux Coin
        'A' -> Color(0xFF4CAF50) // Green spawn point
        'F' -> Color(0xFFFF8F00) // Orange finish flag
        else -> Color.Transparent // Air
    }
}

// ==========================================
// 🎮 6. GAME PLAYROOM SCREEN (2D PLATFORMER)
// ==========================================
@Composable
fun PlayroomScreen(viewModel: BloxViewModel) {
    val level = viewModel.activePlayingLevel ?: return
    val profile by viewModel.playerProfile.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 2D World Board Rendering (Game canvas layout)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val cellW = maxWidth / 20
            val cellH = maxHeight / 12

            // Background art matching theme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = when (level.thumbnailSeed) {
                                "lava" -> listOf(Color(0xFF3F0F0F), Color(0xFF0F0505))
                                "clouds" -> listOf(Color(0xFF0F2D3F), Color(0xFF070F1A))
                                else -> listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                            }
                        )
                    )
            )

            // Render Tiles
            for (row in 0..11) {
                for (col in 0..19) {
                    val idx = row * 20 + col
                    val tile = viewModel.activeGameGrid.getOrNull(idx) ?: '.'

                    if (tile != '.') {
                        Box(
                            modifier = Modifier
                                .offset(x = cellW * col, y = cellH * row)
                                .size(cellW, cellH)
                        ) {
                            // Render tiles visual styles
                            when (tile) {
                                'B' -> { // Solid Brick Block with design
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF64748B))
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    )
                                }
                                'S' -> { // Spike triangle
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val path = Path().apply {
                                            moveTo(size.width * 0.1f, size.height)
                                            lineTo(size.width * 0.5f, size.height * 0.1f)
                                            lineTo(size.width * 0.9f, size.height)
                                            close()
                                        }
                                        drawPath(path, Color(0xFFEF4444))
                                    }
                                }
                                'T' -> { // Spring Trampoline
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(cellH * 0.4f)
                                            .align(Alignment.BottomCenter)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF3B82F6))
                                            .border(1.dp, Color.White)
                                    )
                                }
                                'P' -> { // Speed Pad Arrows
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(cellH * 0.25f)
                                            .align(Alignment.BottomCenter)
                                            .background(Color(0xFF8E24AA))
                                    ) {
                                        Icon(
                                            Icons.Filled.KeyboardDoubleArrowRight,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.fillMaxSize().scale(0.8f)
                                        )
                                    }
                                }
                                'C' -> { // Coin Gold Spinner
                                    Box(
                                        modifier = Modifier
                                            .size(cellW * 0.6f)
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFC107))
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.MonetizationOn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.fillMaxSize().padding(1.dp)
                                        )
                                    }
                                }
                                'F' -> { // Finish flag pole & red banner
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Pole
                                        drawRect(Color.White, Offset(size.width * 0.3f, size.height * 0.1f), Size(size.width * 0.08f, size.height * 0.9f))
                                        // Flag Banner
                                        val flagPath = Path().apply {
                                            moveTo(size.width * 0.38f, size.height * 0.1f)
                                            lineTo(size.width * 0.9f, size.height * 0.3f)
                                            lineTo(size.width * 0.38f, size.height * 0.5f)
                                            close()
                                        }
                                        drawPath(flagPath, Color(0xFFF59E0B))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Render Player Avatar at their Float (playerX, playerY) coordinate!
            AvatarCanvas(
                profile = profile,
                modifier = Modifier
                    .offset(
                        x = cellW * viewModel.playerX,
                        y = cellH * viewModel.playerY
                    )
                    .size(cellW * 0.85f, cellH * 0.95f)
            )
        }

        // --- On-Screen overlay HUD ---
        // Top bar displaying Game metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Time: %.2fs".format(viewModel.gameTimeElapsed),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Coins: ${viewModel.gameCoinsCollected} 🪙",
                    color = Color(0xFFFFC107),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Exit level button
            IconButton(
                onClick = { viewModel.exitGame() },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .testTag("exit_game_button")
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Exit Level", tint = Color.White)
            }
        }

        // Play feedback notification banner (Bounce, speed up, coin)
        if (viewModel.playStatusMessage.isNotEmpty()) {
            Text(
                text = viewModel.playStatusMessage,
                color = if (viewModel.playStatusMessage.contains("OOF")) Color.Red else Color.Green,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        // --- Game controls overlaid at the bottom ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Directional Buttons (Left & Right)
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Button
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    viewModel.setLeftMove(true)
                                    tryAwaitRelease()
                                    viewModel.setLeftMove(false)
                                }
                            )
                        }
                        .testTag("control_left")
                ) {
                    Icon(Icons.Filled.ArrowLeft, contentDescription = "Left", tint = Color.White, modifier = Modifier.size(40.dp))
                }

                // Right Button
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    viewModel.setRightMove(true)
                                    tryAwaitRelease()
                                    viewModel.setRightMove(false)
                                }
                            )
                        }
                        .testTag("control_right")
                ) {
                    Icon(Icons.Filled.ArrowRight, contentDescription = "Right", tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }

            // Jump Action button (large circle)
            Button(
                onClick = { viewModel.jump() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316).copy(alpha = 0.8f)),
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .testTag("control_jump")
            ) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = "Jump", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        // Victory celebration overlay
        AnimatedVisibility(
            visible = viewModel.isLevelCompleted,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp)
                    .border(1.dp, Color(0xFFDDE2EA), RoundedCornerShape(16.dp))
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = "Winner!", tint = Color(0xFF0061A4), modifier = Modifier.size(72.dp))
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("VICTORY!", color = Color(0xFF1A1C1E), fontWeight = FontWeight.Black, fontSize = 26.sp)
                    Text("You completed the Obby successfully!", color = Color(0xFF74777F), fontSize = 12.sp, textAlign = TextAlign.Center)

                    Divider(color = Color(0xFFDDE2EA), modifier = Modifier.padding(vertical = 12.dp))

                    Text("Completion Time: %.2fs".format(viewModel.gameTimeElapsed), color = Color(0xFF1A1C1E), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Coins Collected: ${viewModel.gameCoinsCollected} 🪙", color = Color(0xFF0061A4), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Reward info
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E4FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Reward: +${viewModel.gameCoinsCollected + 20} ROBUX!",
                            color = Color(0xFF001D36),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.exitGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                        modifier = Modifier.fillMaxWidth().testTag("victory_ok_button")
                    ) {
                        Text("AWESOME!", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
