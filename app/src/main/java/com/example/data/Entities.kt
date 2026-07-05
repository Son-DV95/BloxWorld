package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val creator: String,
    val description: String,
    val gridData: String, // String of length 240 (12 rows x 20 columns)
    val isUserCreated: Boolean = false,
    val plays: Int = 0,
    val highScore: Float = 0.0f, // 0.0 means uncompleted
    val thumbnailSeed: String = ""
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1, // Single profile
    val username: String = "BloxPlayer",
    val robux: Int = 50, // Initial currency
    val unlockedItemIds: String = "smile,admin_hoodie,jeans", // Comma-separated unlocked item IDs
    val equippedHat: String = "none",
    val equippedFace: String = "smile",
    val equippedShirt: String = "admin_hoodie",
    val equippedPants: String = "jeans"
)
