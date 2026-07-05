package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LevelEntity::class, PlayerProfileEntity::class], version = 1, exportSchema = false)
abstract class BloxDatabase : RoomDatabase() {
    abstract fun bloxDao(): BloxDao

    companion object {
        @Volatile
        private var INSTANCE: BloxDatabase? = null

        fun getDatabase(context: Context): BloxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BloxDatabase::class.java,
                    "blox_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
