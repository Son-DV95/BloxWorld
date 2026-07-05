package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "builder"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class BloxViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BloxDatabase.getDatabase(application)
    private val repository = BloxRepository(db.bloxDao())

    // UI States
    var currentTab by mutableStateOf("discover")
        private set

    val levels: StateFlow<List<LevelEntity>> = repository.allLevels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Play States
    var activePlayingLevel by mutableStateOf<LevelEntity?>(null)
        private set
    var activeGameGrid by mutableStateOf<String>("") // Mutable copy of grid during play
    var playerX by mutableStateOf(0.0f)
    var playerY by mutableStateOf(0.0f)
    var vx by mutableStateOf(0.0f)
    var vy by mutableStateOf(0.0f)
    var isOnGround by mutableStateOf(false)
    var isLevelCompleted by mutableStateOf(false)
    var gameTimeElapsed by mutableStateOf(0.0f)
    var gameCoinsCollected by mutableStateOf(0)
    var showOofEffect by mutableStateOf(false)
    var playStatusMessage by mutableStateOf("")

    // Input states for controls
    private var leftPressed = false
    private var rightPressed = false

    // Active Edit States
    var activeEditingLevel by mutableStateOf<LevelEntity?>(null)
        private set
    var editorLevelName by mutableStateOf("")
    var editorLevelDescription by mutableStateOf("")
    var editorGrid by mutableStateOf(CharArray(240) { '.' })
    var editorSelectedTile by mutableStateOf('B') // B = Wall, S = Spikes, T = Trampoline, P = Speed Pad, C = Coin, F = Flag, A = Spawn
    var isGeneratingLore by mutableStateOf(false)

    // AI Lounge States
    var chatHistory = mutableStateListOf<ChatMessage>()
        private set
    var isChatLoading by mutableStateOf(false)

    // Game loop control
    private var gameJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
            // Add initial welcome chat message
            if (chatHistory.isEmpty()) {
                chatHistory.add(
                    ChatMessage(
                        sender = "builder",
                        text = "Hello! I am Builderman, your Roblox AI assistant. Ask me anything about creating levels, character customization, or game design, and let's build something epic together!"
                    )
                )
            }
        }
    }

    // --- Tab Navigation ---
    fun selectTab(tab: String) {
        currentTab = tab
        // Exit any modes
        activePlayingLevel = null
        activeEditingLevel = null
        stopGameLoop()
    }

    // --- Shop & Inventory Actions ---
    fun purchaseItem(item: AvatarItem) {
        viewModelScope.launch {
            val success = repository.buyItem(item.id, item.price)
            if (success) {
                // Auto-equip purchased item
                repository.equipItem(item.category, item.id)
            }
        }
    }

    fun equipItem(category: String, itemId: String) {
        viewModelScope.launch {
            repository.equipItem(category, itemId)
        }
    }

    // --- Game Logic Engine (The 2D Physics Loop) ---
    fun startPlaying(level: LevelEntity) {
        activePlayingLevel = level
        activeGameGrid = level.gridData
        isLevelCompleted = false
        gameTimeElapsed = 0.0f
        gameCoinsCollected = 0
        showOofEffect = false
        playStatusMessage = ""
        
        // Find Spawn Point 'A' in the grid. If not found, start at (1.0, 10.0)
        val spawnIdx = level.gridData.indexOf('A')
        if (spawnIdx != -1) {
            val row = spawnIdx / 20
            val col = spawnIdx % 20
            playerX = col.toFloat()
            playerY = row.toFloat()
        } else {
            playerX = 1.0f
            playerY = 10.0f
        }
        
        vx = 0.0f
        vy = 0.0f
        isOnGround = false
        leftPressed = false
        rightPressed = false

        viewModelScope.launch {
            repository.incrementLevelPlays(level.id)
        }

        startGameLoop()
    }

    fun exitGame() {
        stopGameLoop()
        activePlayingLevel = null
    }

    fun setLeftMove(pressed: Boolean) {
        leftPressed = pressed
    }

    fun setRightMove(pressed: Boolean) {
        rightPressed = pressed
    }

    fun jump() {
        if (isOnGround) {
            vy = -0.32f
            isOnGround = false
        }
    }

    private fun startGameLoop() {
        stopGameLoop()
        gameJob = viewModelScope.launch {
            val frameDelay = 16L // ~60 FPS
            while (activePlayingLevel != null && !isLevelCompleted) {
                delay(frameDelay)
                updatePhysics()
            }
        }
    }

    private fun stopGameLoop() {
        gameJob?.cancel()
        gameJob = null
    }

    private fun updatePhysics() {
        if (showOofEffect) return // Pause briefly during oof/death animation

        gameTimeElapsed += 0.016f

        // Horizontal velocity
        val targetVx = if (leftPressed) -0.12f else if (rightPressed) 0.12f else 0.0f
        // Apply acceleration / deceleration
        vx = vx * 0.7f + targetVx * 0.3f

        // Gravity
        val gravity = 0.018f
        vy += gravity
        if (vy > 0.4f) vy = 0.4f // Terminal velocity

        // Proposed X & Y
        val newX = playerX + vx
        val newY = playerY + vy

        // Check horizontal collision against solids
        val pWidth = 0.6f
        val pHeight = 0.85f

        // Collision Check: Ground & Wall solids ('B')
        var finalX = newX
        var finalY = newY

        // We check 4 corners of the bounding box
        // Left wall collision
        if (checkSolidCollision(newX, playerY, pWidth, pHeight)) {
            // Revert X, stop velocity
            vx = 0.0f
            finalX = playerX
        } else {
            finalX = newX
        }

        // Vertical collision
        var collidedVertically = false
        if (checkSolidCollision(finalX, newY, pWidth, pHeight)) {
            collidedVertically = true
            if (vy > 0) {
                // Falling down -> landed on ground
                isOnGround = true
                vy = 0.0f
                // Snap playerY to integer block grid surface
                finalY = Math.floor(newY.toDouble()).toFloat()
            } else if (vy < 0) {
                // Heading up -> hit cell roof
                vy = 0.0f
                finalY = playerY
            }
        } else {
            finalY = newY
            // If we fall without solid block beneath, we are not on ground
            if (!checkSolidCollision(finalX, newY + 0.05f, pWidth, pHeight)) {
                isOnGround = false
            }
        }

        playerX = finalX
        playerY = finalY

        // Boundary checks (Clamp to level coordinates 0 to 20)
        if (playerX < 0f) { playerX = 0f; vx = 0f }
        if (playerX > 19.4f) { playerX = 19.4f; vx = 0f }

        // Fall out of bounds (Death)
        if (playerY >= 12.0f) {
            triggerOof()
            return
        }

        // Handle tile interactions (Speedpad, Trampoline, Spike, Coins, Goal)
        checkTileInteractions()
    }

    private fun checkSolidCollision(x: Float, y: Float, width: Float, height: Float): Boolean {
        // Sample points within the player's bounding box
        val points = listOf(
            x to y,                             // Top-left
            x + width to y,                     // Top-right
            x to y + height,                    // Bottom-left
            x + width to y + height,            // Bottom-right
            x + width / 2 to y + height / 2      // Center
        )

        for (pt in points) {
            val cellX = pt.first.toInt()
            val cellY = pt.second.toInt()

            if (cellX in 0..19 && cellY in 0..11) {
                val idx = cellY * 20 + cellX
                val tile = activeGameGrid.getOrNull(idx) ?: '.'
                if (tile == 'B') {
                    return true
                }
            }
        }
        return false
    }

    private fun checkTileInteractions() {
        val pWidth = 0.6f
        val pHeight = 0.85f
        
        // Find player center coordinate
        val centerX = playerX + pWidth / 2f
        val centerY = playerY + pHeight / 2f

        val cellX = centerX.toInt()
        val cellY = centerY.toInt()

        if (cellX in 0..19 && cellY in 0..11) {
            val idx = cellY * 20 + cellX
            val tile = activeGameGrid.getOrNull(idx) ?: '.'

            when (tile) {
                'S' -> { // Spikes
                    triggerOof()
                }
                'T' -> { // Trampoline (Spring boost)
                    vy = -0.42f
                    isOnGround = false
                    playStatusMessage = "BOUNCE! 🚀"
                    viewModelScope.launch {
                        delay(1200)
                        if (playStatusMessage == "BOUNCE! 🚀") playStatusMessage = ""
                    }
                }
                'P' -> { // Speed Pad
                    vx *= 1.8f
                    playStatusMessage = "SPEED RUN! ⚡"
                    viewModelScope.launch {
                        delay(1200)
                        if (playStatusMessage == "SPEED RUN! ⚡") playStatusMessage = ""
                    }
                }
                'C' -> { // Coin (Robux coin)
                    // Remove coin from active grid
                    val gridChars = activeGameGrid.toCharArray()
                    gridChars[idx] = '.'
                    activeGameGrid = String(gridChars)
                    gameCoinsCollected += 10
                    playStatusMessage = "+10 ROBUX! 🪙"
                    viewModelScope.launch {
                        delay(1000)
                        if (playStatusMessage == "+10 ROBUX! 🪙") playStatusMessage = ""
                    }
                }
                'F' -> { // Finish flag! (Victory)
                    completeLevel()
                }
            }
        }
    }

    private fun triggerOof() {
        showOofEffect = true
        playStatusMessage = "OOF! 💥"
        
        // Reset player to spawn point after delay
        viewModelScope.launch {
            delay(800)
            showOofEffect = false
            playStatusMessage = ""
            
            // Re-fetch spawn
            val spawnIdx = activePlayingLevel?.gridData?.indexOf('A') ?: -1
            if (spawnIdx != -1) {
                playerX = (spawnIdx % 20).toFloat()
                playerY = (spawnIdx / 20).toFloat()
            } else {
                playerX = 1.0f
                playerY = 10.0f
            }
            vx = 0.0f
            vy = 0.0f
            isOnGround = false
        }
    }

    private fun completeLevel() {
        isLevelCompleted = true
        stopGameLoop()
        
        val level = activePlayingLevel ?: return
        viewModelScope.launch {
            // Reward Robux
            val earned = gameCoinsCollected + 20
            repository.addRobux(earned)
            repository.updateLevelHighScore(level.id, gameTimeElapsed)
        }
    }

    // --- Blox Studio (Level Creator) Actions ---
    fun openLevelCreator(levelToEdit: LevelEntity? = null) {
        if (levelToEdit != null) {
            activeEditingLevel = levelToEdit
            editorLevelName = levelToEdit.name
            editorLevelDescription = levelToEdit.description
            editorGrid = levelToEdit.gridData.toCharArray()
        } else {
            activeEditingLevel = null
            editorLevelName = "My New Obby"
            editorLevelDescription = "Can you beat this super fun obstacle course?"
            // Clear grid with air, set ground at the bottom row, and spawn point
            val newGrid = CharArray(240) { '.' }
            // Floor
            for (col in 0..19) {
                newGrid[11 * 20 + col] = 'B'
            }
            // Spawn
            newGrid[10 * 20 + 1] = 'A'
            // Goal
            newGrid[10 * 20 + 18] = 'F'
            editorGrid = newGrid
        }
        editorSelectedTile = 'B'
    }

    fun exitEditor() {
        activeEditingLevel = null
    }

    fun drawTile(row: Int, col: Int) {
        if (row in 0..11 && col in 0..19) {
            val idx = row * 20 + col
            val gridCopy = editorGrid.copyOf()
            gridCopy[idx] = editorSelectedTile
            editorGrid = gridCopy
        }
    }

    fun generateLevelLore() {
        if (editorLevelName.isBlank()) return
        isGeneratingLore = true
        viewModelScope.launch {
            // Count components to describe to Gemini
            val spikes = editorGrid.count { it == 'S' }
            val speedpads = editorGrid.count { it == 'P' }
            val trampolines = editorGrid.count { it == 'T' }
            val coins = editorGrid.count { it == 'C' }

            val prompt = """
                Write a short, engaging, and enthusiastic description for a Roblox obby level.
                Level Name: "$editorLevelName"
                Features:
                - Spikes/Lava: $spikes
                - Speedpads: $speedpads
                - Trampolines/Springs: $trampolines
                - Robux Coins: $coins
                Keep the description brief (2-3 sentences max) using high-energy gamer terms like "obbies", "oof", "speedruns", "extreme". Do not include Markdown blocks.
            """.trimIndent()

            val response = GeminiService.generateText(
                prompt = prompt,
                systemInstruction = "You are Builderman, the developer assistant of BloxWorld. You write fun, action-packed description summaries for player-made levels."
            )
            editorLevelDescription = response.trim().removeSurrounding("\"")
            isGeneratingLore = false
        }
    }

    fun saveAndPublishLevel() {
        if (editorLevelName.isBlank()) return

        viewModelScope.launch {
            val gridStr = String(editorGrid)
            val entity = LevelEntity(
                id = activeEditingLevel?.id ?: 0, // 0 triggers auto-increment
                name = editorLevelName,
                creator = playerProfile.value?.username ?: "Creator",
                description = editorLevelDescription.ifBlank { "An awesome custom platformer obstacle course!" },
                gridData = gridStr,
                isUserCreated = true,
                thumbnailSeed = "custom"
            )
            repository.saveLevel(entity)
            activeEditingLevel = null
            selectTab("discover")
        }
    }

    fun deleteLevel(id: Int) {
        viewModelScope.launch {
            repository.deleteLevelById(id)
        }
    }

    // --- Builderman AI Chat Lounge ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        
        chatHistory.add(ChatMessage(sender = "user", text = text))
        isChatLoading = true

        viewModelScope.launch {
            val system = """
                You are Builderman, the legendary AI Developer assistant in BloxWorld (a 2D Roblox platformer/creator). 
                Keep your responses short, cheerful, friendly, and helpful. Focus on Roblox game-dev ideas, obstacle suggestions (spikes, trampolines, speedpads), and customized code script concepts in 2D platformer physics.
                Greet users as "Bloxer" or "Dev"!
            """.trimIndent()

            val response = GeminiService.generateText(prompt = text, systemInstruction = system)
            chatHistory.add(ChatMessage(sender = "builder", text = response))
            isChatLoading = false
        }
    }

    fun clearChat() {
        chatHistory.clear()
        chatHistory.add(
            ChatMessage(
                sender = "builder",
                text = "Chat cleared! What shall we discuss now? I can help you design a 'Lava Spikes' challenge, suggest colors for your avatar, or write game lore!"
            )
        )
    }
}

// Inline extension function to create mutable lists in state easily
fun <T> mutableStateListOf(vararg elements: T) = androidx.compose.runtime.mutableStateListOf<T>().apply { addAll(elements) }
