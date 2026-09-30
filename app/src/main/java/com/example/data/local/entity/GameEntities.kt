package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val provider: String, // "GOOGLE", "FACEBOOK", "DISCORD", "GUEST"
    val avatarUrl: String? = null,
    val isCurrentActive: Boolean = false,
    val linkedProviders: String = provider,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastLoginMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val accountId: String = "guest_default",
    val name: String = "Petualang",
    val level: Int = 1,
    val exp: Int = 0,
    val maxExp: Int = 100,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val energy: Int = 50,
    val maxEnergy: Int = 50,
    val gold: Int = 250,
    val diamonds: Int = 15,
    val upgradeStones: Int = 3,
    val fishingRodLevel: Int = 1,
    val selectedBaitId: String = "bait_cacing",
    val lastEnergyUpdateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "farm_plots")
data class FarmPlotEntity(
    @PrimaryKey val plotIndex: Int,
    val isUnlocked: Boolean = false,
    val cropId: String? = null,
    val plantedAtMillis: Long = 0L,
    val harvestAtMillis: Long = 0L,
    val isWatered: Boolean = false,
    val isFertilized: Boolean = false,
    val currentStage: Int = 0
)

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val speciesId: String,
    val nickname: String,
    val level: Int = 1,
    val exp: Int = 0,
    val maxExp: Int = 50,
    val evolutionStage: Int = 1,
    val hunger: Int = 85,
    val happiness: Int = 90,
    val isEquipped: Boolean = false,
    val expeditionZoneId: String? = null,
    val expeditionReturnMillis: Long = 0L
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val itemType: String,
    val name: String,
    val count: Int,
    val iconEmoji: String,
    val rarity: String,
    val sellPrice: Int
)

@Entity(tableName = "fish_dex")
data class FishDexEntity(
    @PrimaryKey val fishId: String,
    val fishName: String,
    val rarity: String,
    val iconEmoji: String,
    val countCaught: Int = 0,
    val maxWeightKg: Float = 0.0f,
    val isDiscovered: Boolean = false
)

@Entity(tableName = "daily_quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val questType: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val rewardGold: Int,
    val rewardExp: Int,
    val rewardDiamonds: Int = 0
)
