package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.entity.FarmPlotEntity
import com.example.data.local.entity.FishDexEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PetEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestEntity

import com.example.data.local.entity.UserAccountEntity

@Database(
    entities = [
        UserAccountEntity::class,
        PlayerProfileEntity::class,
        FarmPlotEntity::class,
        PetEntity::class,
        InventoryItemEntity::class,
        FishDexEntity::class,
        QuestEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rpg_realm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
