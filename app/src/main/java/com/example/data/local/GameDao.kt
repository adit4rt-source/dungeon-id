package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FarmPlotEntity
import com.example.data.local.entity.FishDexEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PetEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Player
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlayer(player: PlayerProfileEntity)

    // Farm Plots
    @Query("SELECT * FROM farm_plots ORDER BY plotIndex ASC")
    fun getAllPlots(): Flow<List<FarmPlotEntity>>

    @Query("SELECT * FROM farm_plots ORDER BY plotIndex ASC")
    suspend fun getPlotsSync(): List<FarmPlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlots(plots: List<FarmPlotEntity>)

    @Update
    suspend fun updatePlot(plot: FarmPlotEntity)

    // Pets
    @Query("SELECT * FROM pets ORDER BY id ASC")
    fun getAllPets(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE isEquipped = 1 LIMIT 1")
    fun getEquippedPet(): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE isEquipped = 1 LIMIT 1")
    suspend fun getEquippedPetSync(): PetEntity?

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPetById(petId: Int): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity): Long

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query("UPDATE pets SET isEquipped = 0")
    suspend fun clearAllEquipped()

    @Query("UPDATE pets SET isEquipped = 1 WHERE id = :petId")
    suspend fun setEquippedPet(petId: Int)

    // Inventory
    @Query("SELECT * FROM inventory_items WHERE count > 0 ORDER BY itemType, name ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    // Fish Dex
    @Query("SELECT * FROM fish_dex ORDER BY fishId ASC")
    fun getAllFishDex(): Flow<List<FishDexEntity>>

    @Query("SELECT * FROM fish_dex WHERE fishId = :fishId LIMIT 1")
    suspend fun getFishDexById(fishId: String): FishDexEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFishDex(fishDex: FishDexEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllFishDex(list: List<FishDexEntity>)

    // Quests
    @Query("SELECT * FROM daily_quests ORDER BY id ASC")
    fun getAllQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM daily_quests ORDER BY id ASC")
    suspend fun getQuestsSync(): List<QuestEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)
}
