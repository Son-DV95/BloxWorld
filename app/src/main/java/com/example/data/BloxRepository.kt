package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class BloxRepository(private val bloxDao: BloxDao) {

    val allLevels: Flow<List<LevelEntity>> = bloxDao.getAllLevels()
    val playerProfile: Flow<PlayerProfileEntity?> = bloxDao.getPlayerProfile()

    suspend fun getLevelById(id: Int): LevelEntity? {
        return bloxDao.getLevelById(id)
    }

    suspend fun saveLevel(level: LevelEntity) {
        bloxDao.insertLevel(level)
    }

    suspend fun deleteLevelById(id: Int) {
        bloxDao.deleteLevelById(id)
    }

    suspend fun updateProfile(profile: PlayerProfileEntity) {
        bloxDao.insertProfile(profile)
    }

    suspend fun incrementLevelPlays(id: Int) {
        val level = getLevelById(id)
        if (level != null) {
            bloxDao.insertLevel(level.copy(plays = level.plays + 1))
        }
    }

    suspend fun updateLevelHighScore(id: Int, time: Float) {
        val level = getLevelById(id)
        if (level != null) {
            if (level.highScore == 0.0f || time < level.highScore) {
                bloxDao.insertLevel(level.copy(highScore = time))
            }
        }
    }

    suspend fun addRobux(amount: Int) {
        val profile = bloxDao.getPlayerProfileDirect() ?: PlayerProfileEntity()
        bloxDao.insertProfile(profile.copy(robux = profile.robux + amount))
    }

    suspend fun buyItem(itemId: String, price: Int): Boolean {
        val profile = bloxDao.getPlayerProfileDirect() ?: PlayerProfileEntity()
        if (profile.robux >= price) {
            val unlockedList = profile.unlockedItemIds.split(",").toMutableList()
            if (!unlockedList.contains(itemId)) {
                unlockedList.add(itemId)
                val updatedProfile = profile.copy(
                    robux = profile.robux - price,
                    unlockedItemIds = unlockedList.joinToString(",")
                )
                bloxDao.insertProfile(updatedProfile)
                return true
            }
        }
        return false
    }

    suspend fun equipItem(category: String, itemId: String) {
        val profile = bloxDao.getPlayerProfileDirect() ?: PlayerProfileEntity()
        val updatedProfile = when (category) {
            "hat" -> profile.copy(equippedHat = itemId)
            "face" -> profile.copy(equippedFace = itemId)
            "shirt" -> profile.copy(equippedShirt = itemId)
            "pants" -> profile.copy(equippedPants = itemId)
            else -> profile
        }
        bloxDao.insertProfile(updatedProfile)
    }

    suspend fun initializeDefaultDataIfNeeded() {
        val levels = bloxDao.getAllLevels().firstOrNull() ?: emptyList()
        if (levels.isEmpty()) {
            val defaultLevels = createDefaultLevels()
            for (lvl in defaultLevels) {
                bloxDao.insertLevel(lvl)
            }
        }

        val profile = bloxDao.getPlayerProfileDirect()
        if (profile == null) {
            bloxDao.insertProfile(PlayerProfileEntity())
        }
    }

    private fun createDefaultLevels(): List<LevelEntity> {
        return listOf(
            LevelEntity(
                name = "Speed Run Obby",
                creator = "Builderman",
                description = "Sprint as fast as you can, dodge spikes, and grab Robux on your way to the top!",
                gridData = createGrid(
                    "....................",
                    "....................",
                    "....................",
                    "....................",
                    "....................",
                    "....................",
                    ".........C.C........",
                    "........BBBBB...F...",
                    "....C..........BBBBB",
                    "A..BBB..S...S.......",
                    "B.....BBBB.BBBB.....",
                    "BBBBBBBBBB.BBBBBBBBB"
                ),
                isUserCreated = false,
                thumbnailSeed = "obby"
            ),
            LevelEntity(
                name = "Lava Escape",
                creator = "Noob Master",
                description = "Floor is lava! Jump on trampolines, speed pads, and floating blocks to escape to safety!",
                gridData = createGrid(
                    "....................",
                    "..................F.",
                    "................BBB.",
                    "...........C...BB...",
                    ".........BBBB.......",
                    "......C.BB..........",
                    "....BBBBB...........",
                    "....................",
                    "........C..C........",
                    "A....T.BB.BBB.P.....",
                    "B....BBBB.BBB.BB....",
                    "BBSSSSSSBSSSBSSBSSSS"
                ),
                isUserCreated = false,
                thumbnailSeed = "lava"
            ),
            LevelEntity(
                name = "Cloud Tower Obby",
                creator = "SkyBlox",
                description = "Climb high into the sky! Use spring trampolines to leap from cloud to cloud.",
                gridData = createGrid(
                    "..........F.........",
                    "........BBBBB.......",
                    "......C.............",
                    "....BBBBB........C..",
                    "...............BBBBB",
                    ".........C..........",
                    ".......BBBBB........",
                    "................C...",
                    ".............BBBBB..",
                    "A......T............",
                    "B...BBBBBB..........",
                    "BBBBBBBBBBSSSSSSBBBB"
                ),
                isUserCreated = false,
                thumbnailSeed = "clouds"
            )
        )
    }

    private fun createGrid(vararg rows: String): String {
        val sb = StringBuilder()
        for (r in rows) {
            val rowStr = if (r.length < 20) r.padEnd(20, '.') else r.substring(0, 20)
            sb.append(rowStr)
        }
        // Fill up to 240 chars if needed
        while (sb.length < 240) {
            sb.append("....................")
        }
        return sb.toString().take(240)
    }
}
