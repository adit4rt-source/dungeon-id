package com.example.data.repository

import com.example.data.local.GameDao
import com.example.data.local.entity.FarmPlotEntity
import com.example.data.local.entity.FishDexEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PetEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.local.entity.UserAccountEntity
import com.example.data.model.FishDef
import com.example.data.model.GameDatabaseRegistry
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import com.example.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class GameRepository(private val dao: GameDao) {

    val activeUserAccount: Flow<UserAccountEntity?> = dao.getActiveUserAccount()
    val allUserAccounts: Flow<List<UserAccountEntity>> = dao.getAllUserAccounts()

    val playerProfile: Flow<PlayerProfileEntity?> = activeUserAccount.flatMapLatest { account ->
        if (account == null) flowOf(null) else dao.getPlayerProfile(account.id)
    }

    val allPlots: Flow<List<FarmPlotEntity>> = activeUserAccount.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.getAllPlots(account.id)
    }

    val allPets: Flow<List<PetEntity>> = activeUserAccount.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.getAllPets(account.id)
    }

    val equippedPet: Flow<PetEntity?> = activeUserAccount.flatMapLatest { account ->
        if (account == null) flowOf(null) else dao.getEquippedPet(account.id)
    }

    val inventory: Flow<List<InventoryItemEntity>> = activeUserAccount.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.getAllInventory(account.id)
    }

    val fishDex: Flow<List<FishDexEntity>> = activeUserAccount.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.getAllFishDex(account.id)
    }

    val dailyQuests: Flow<List<QuestEntity>> = activeUserAccount.flatMapLatest { account ->
        if (account == null) flowOf(emptyList()) else dao.getAllQuests(account.id)
    }

    private suspend fun getActiveAccountId(): String? {
        return dao.getActiveUserAccountSync()?.id
    }

    suspend fun initializeGameIfNeeded() = withContext(Dispatchers.IO) {
        val currentAccount = dao.getActiveUserAccountSync() ?: return@withContext
        val currentProfile = dao.getPlayerProfileSync(currentAccount.id)
        if (currentProfile != null) {
            checkAndRecoverEnergy(currentProfile)
        } else {
            initializeAccountDataIfNeeded(currentAccount.id, currentAccount.username)
        }
    }

    suspend fun initializeAccountDataIfNeeded(accountId: String, username: String) = withContext(Dispatchers.IO) {
        val existingProfile = dao.getPlayerProfileSync(accountId)
        if (existingProfile == null) {
            val starterProfile = PlayerProfileEntity(
                accountId = accountId,
                name = username.ifBlank { "Petualang" },
                level = 1,
                exp = 0,
                maxExp = 100,
                hp = 100,
                maxHp = 100,
                energy = 50,
                maxEnergy = 50,
                gold = 350,
                diamonds = 20,
                upgradeStones = 4,
                fishingRodLevel = 1,
                selectedBaitId = "bait_bait_cacing",
                lastEnergyUpdateMillis = System.currentTimeMillis()
            )
            dao.insertOrUpdatePlayer(starterProfile)

            // 8 Farm plots (0..3 unlocked, 4..7 locked)
            val plots = (0 until 8).map { index ->
                FarmPlotEntity(
                    accountId = accountId,
                    plotIndex = index,
                    isUnlocked = index < 4,
                    cropId = null,
                    plantedAtMillis = 0L,
                    harvestAtMillis = 0L,
                    isWatered = false,
                    isFertilized = false,
                    currentStage = 0
                )
            }
            dao.insertPlots(plots)

            // Starter pet: Lupin (Serigala Salju)
            val starterPet = PetEntity(
                id = 0,
                accountId = accountId,
                speciesId = "pet_wolf",
                nickname = "Lupin",
                level = 1,
                exp = 0,
                maxExp = 50,
                evolutionStage = 1,
                hunger = 90,
                happiness = 95,
                isEquipped = true
            )
            dao.insertPet(starterPet)

            // Starter inventory items (Proper Indonesian IDs)
            val starterItems = listOf(
                InventoryItemEntity(accountId, "crop_seed_jagung", ItemType.SEED.name, "Benih Jagung", 5, "🌽", ItemRarity.COMMON.name, 15),
                InventoryItemEntity(accountId, "crop_seed_wortel", ItemType.SEED.name, "Benih Wortel", 3, "🥕", ItemRarity.COMMON.name, 28),
                InventoryItemEntity(accountId, "bait_bait_cacing", ItemType.BAIT.name, "Cacing Tanah", 15, "🪱", ItemRarity.COMMON.name, 5),
                InventoryItemEntity(accountId, "bait_bait_pelet", ItemType.BAIT.name, "Pelet Harum", 5, "🟤", ItemRarity.UNCOMMON.name, 15),
                InventoryItemEntity(accountId, "item_potion_hp", ItemType.POTION.name, "Ramuan Darah", 3, "🧪", ItemRarity.UNCOMMON.name, 50),
                InventoryItemEntity(accountId, "item_fertilizer", ItemType.MATERIAL.name, "Pupuk Organik", 3, "✨", ItemRarity.COMMON.name, 25)
            )
            starterItems.forEach { dao.insertItem(it) }

            // Fish Dex
            val initialDex = GameDatabaseRegistry.FISHES.map { fish ->
                FishDexEntity(
                    accountId = accountId,
                    fishId = fish.id,
                    fishName = fish.name,
                    rarity = fish.rarity.name,
                    iconEmoji = fish.iconEmoji,
                    countCaught = 0,
                    maxWeightKg = 0f,
                    isDiscovered = false
                )
            }
            dao.insertAllFishDex(initialDex)

            // Daily Quests
            val initialQuests = listOf(
                QuestEntity(accountId, "q_fish_1", "Mancing Ikan Perdana", "Tangkap 3 ekor ikan jenis apapun di danau.", "FISH", 3, 0, false, false, 120, 40, 2),
                QuestEntity(accountId, "q_farm_1", "Panen Pertama Kebun", "Tanam dan panen 4 hasil pertanian.", "FARM", 4, 0, false, false, 150, 50, 3),
                QuestEntity(accountId, "q_pet_1", "Kasih Sayang Companion", "Beri makan atau latih pet peliharaanmu 2 kali.", "PET", 2, 0, false, false, 100, 35, 2),
                QuestEntity(accountId, "q_hunt_1", "Taklukkan Hutan Lumut", "Kalahkan 3 monster di dungeon petualangan.", "DUNGEON", 3, 0, false, false, 200, 70, 5)
            )
            dao.insertQuests(initialQuests)
        } else {
            checkAndRecoverEnergy(existingProfile)
        }
    }

    private suspend fun checkAndRecoverEnergy(profile: PlayerProfileEntity) {
        val now = System.currentTimeMillis()
        val minutesPassed = ((now - profile.lastEnergyUpdateMillis) / 60000L).toInt()
        if (minutesPassed > 0 && profile.energy < profile.maxEnergy) {
            val newEnergy = min(profile.maxEnergy, profile.energy + minutesPassed)
            dao.insertOrUpdatePlayer(
                profile.copy(
                    energy = newEnergy,
                    lastEnergyUpdateMillis = now
                )
            )
        }
    }

    // --- Player Management ---
    suspend fun addGold(amount: Int) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        dao.getPlayerProfileSync(accountId)?.let {
            dao.insertOrUpdatePlayer(it.copy(gold = max(0, it.gold + amount)))
        }
    }

    suspend fun spendGold(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext false
        if (profile.gold >= amount) {
            dao.insertOrUpdatePlayer(profile.copy(gold = profile.gold - amount))
            true
        } else {
            false
        }
    }

    suspend fun spendDiamonds(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext false
        if (profile.diamonds >= amount) {
            dao.insertOrUpdatePlayer(profile.copy(diamonds = profile.diamonds - amount))
            true
        } else {
            false
        }
    }

    suspend fun useEnergy(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext false
        if (profile.energy >= amount) {
            dao.insertOrUpdatePlayer(
                profile.copy(
                    energy = profile.energy - amount,
                    lastEnergyUpdateMillis = System.currentTimeMillis()
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun restoreEnergy(amount: Int) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext
        val newEnergy = min(profile.maxEnergy, profile.energy + amount)
        dao.insertOrUpdatePlayer(profile.copy(energy = newEnergy))
    }

    suspend fun restoreHp(amount: Int) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext
        val newHp = min(profile.maxHp, profile.hp + amount)
        dao.insertOrUpdatePlayer(profile.copy(hp = newHp))
    }

    suspend fun damagePlayer(amount: Int) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext
        val newHp = max(1, profile.hp - amount)
        dao.insertOrUpdatePlayer(profile.copy(hp = newHp))
    }

    suspend fun addPlayerExp(expGain: Int) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext
        var currentExp = profile.exp + expGain
        var level = profile.level
        var maxExp = profile.maxExp
        var maxHp = profile.maxHp
        var maxEnergy = profile.maxEnergy
        var currentHp = profile.hp
        var currentEnergy = profile.energy

        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level += 1
            maxExp = (maxExp * 1.35f).toInt()
            maxHp += 15
            maxEnergy += 5
            currentHp = maxHp
            currentEnergy = maxEnergy
        }

        dao.insertOrUpdatePlayer(
            profile.copy(
                level = level,
                exp = currentExp,
                maxExp = maxExp,
                hp = currentHp,
                maxHp = maxHp,
                energy = currentEnergy,
                maxEnergy = maxEnergy
            )
        )
    }

    suspend fun upgradeFishingRod(): Result<Int> = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext Result.failure(Exception("Akun belum aktif"))
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext Result.failure(Exception("Profil tidak ditemukan"))
        val currentLevel = profile.fishingRodLevel
        if (currentLevel >= GameDatabaseRegistry.ROD_TIERS.size) {
            return@withContext Result.failure(Exception("Alat pancing sudah mencapai level maksimum!"))
        }
        val nextTier = GameDatabaseRegistry.ROD_TIERS[currentLevel]
        if (profile.gold < nextTier.upgradeCostGold) {
            return@withContext Result.failure(Exception("Koin tidak cukup! Butuh ${nextTier.upgradeCostGold} Gold"))
        }
        if (profile.upgradeStones < nextTier.upgradeCostStones) {
            return@withContext Result.failure(Exception("Batu Peningkat tidak cukup! Butuh ${nextTier.upgradeCostStones} Batu"))
        }

        dao.insertOrUpdatePlayer(
            profile.copy(
                gold = profile.gold - nextTier.upgradeCostGold,
                upgradeStones = profile.upgradeStones - nextTier.upgradeCostStones,
                fishingRodLevel = currentLevel + 1
            )
        )
        Result.success(currentLevel + 1)
    }

    // --- Inventory Management ---
    suspend fun addItem(
        itemId: String,
        itemType: ItemType,
        name: String,
        count: Int,
        iconEmoji: String,
        rarity: ItemRarity,
        sellPrice: Int
    ) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        val existing = dao.getItemById(accountId, itemId)
        if (existing != null) {
            dao.insertItem(existing.copy(count = existing.count + count))
        } else {
            dao.insertItem(
                InventoryItemEntity(
                    accountId = accountId,
                    id = itemId,
                    itemType = itemType.name,
                    name = name,
                    count = count,
                    iconEmoji = iconEmoji,
                    rarity = rarity.name,
                    sellPrice = sellPrice
                )
            )
        }
    }

    suspend fun removeItem(itemId: String, count: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        val existing = dao.getItemById(accountId, itemId) ?: return@withContext false
        if (existing.count > count) {
            dao.insertItem(existing.copy(count = existing.count - count))
            true
        } else if (existing.count == count) {
            dao.deleteItemById(accountId, itemId)
            true
        } else {
            false
        }
    }

    suspend fun getItemCount(itemId: String): Int = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext 0
        dao.getItemById(accountId, itemId)?.count ?: 0
    }

    suspend fun useConsumableItem(item: InventoryItemEntity): Result<String> = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext Result.failure(Exception("Akun belum masuk"))
        val itemId = item.id

        // 1. If it's a seed or starts with crop_seed_: auto plant in first empty unlocked plot!
        if (item.itemType == "SEED" || itemId.startsWith("crop_seed_")) {
            val plots = dao.getPlotsSync(accountId)
            val emptyPlot = plots.firstOrNull { it.isUnlocked && it.cropId == null }
                ?: return@withContext Result.failure(Exception("Semua petak kebun terisi atau belum dibuka! Buka petak baru di Kebun."))
            
            val planted = plantSeed(emptyPlot.plotIndex, itemId)
            return@withContext if (planted) {
                Result.success("🌱 ${item.name} berhasil ditanam di Petak Kebun #${emptyPlot.plotIndex + 1}!")
            } else {
                Result.failure(Exception("Gagal menanam benih (periksa jumlah benih di tas)."))
            }
        }

        // 2. If it's a crop: eat to restore HP & Stamina
        if (item.itemType == "CROP" || itemId.startsWith("item_crop_")) {
            if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
            restoreHp(25)
            restoreEnergy(15)
            return@withContext Result.success("🥗 Mengonsumsi ${item.name}! Pulih +25 HP & +15 Stamina.")
        }

        // 3. If it's a fish: eat or cook to restore HP & Stamina
        if (item.itemType == "FISH" || itemId.startsWith("item_fish_") || itemId.startsWith("fish_")) {
            if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
            restoreHp(35)
            restoreEnergy(20)
            return@withContext Result.success("🐟 Memasak & memakan ${item.name}! Pulih +35 HP & +20 Stamina.")
        }

        // 4. If it's bait: equip as active fishing bait
        if (item.itemType == "BAIT" || itemId.startsWith("bait_")) {
            val profile = dao.getPlayerProfileSync(accountId)
                ?: return@withContext Result.failure(Exception("Profil pemain tidak ditemukan"))
            dao.insertOrUpdatePlayer(profile.copy(selectedBaitId = itemId))
            return@withContext Result.success("🎣 ${item.name} berhasil dipasang sebagai umpan pancing aktif!")
        }

        // 5. If it's fertilizer
        if (itemId == "item_fertilizer") {
            val plots = dao.getPlotsSync(accountId)
            val targetPlot = plots.firstOrNull { it.cropId != null && !it.isFertilized }
                ?: return@withContext Result.failure(Exception("Tidak ada tanaman yang perlu diberi pupuk di kebun."))
            fertilizePlot(targetPlot.plotIndex)
            return@withContext Result.success("✨ Berhasil memberi pupuk pada petak #${targetPlot.plotIndex + 1}!")
        }

        when {
            itemId == "item_potion_hp" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreHp(70)
                Result.success("🧪 Meminum Ramuan Darah! Pulih +70 HP.")
            }
            itemId == "item_potion_hp_large" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreHp(200)
                Result.success("🍷 Meminum Elixir Darah Murni! Pulih +200 HP.")
            }
            itemId == "item_potion_stamina" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreEnergy(25)
                Result.success("⚡ Meminum Ramuan Stamina! Pulih +25 Stamina.")
            }
            itemId == "item_scroll_exp" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                addPlayerExp(250)
                Result.success("📜 Membaca Gulungan Berkah EXP! Mendapatkan +250 EXP!")
            }
            itemId == "item_dungeon_ticket" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreEnergy(30)
                Result.success("🎟️ Menggunakan Tiket Dungeon! Pulih +30 Stamina petualangan!")
            }
            itemId == "pet_food_kibble" || itemId == "pet_food_jerky" || itemId == "pet_food_nectar" -> {
                val pet = dao.getEquippedPetSync(accountId) ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                val hungerBoost = when (itemId) {
                    "pet_food_kibble" -> 35
                    "pet_food_jerky" -> 60
                    else -> 100
                }
                val expBoost = when (itemId) {
                    "pet_food_kibble" -> 30
                    "pet_food_jerky" -> 70
                    else -> 160
                }
                feedPet(pet.id, itemId, hungerBoost, expBoost)
                Result.success("🍖 Memberi makan ${pet.nickname}! (+${hungerBoost}% Kenyang & +${expBoost} EXP Pet)")
            }
            itemId == "item_pet_potion_energy" -> {
                val pet = dao.getEquippedPetSync(accountId) ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                dao.updatePet(pet.copy(hunger = 100, happiness = 100, exp = pet.exp + 50))
                Result.success("⚡ Kebugaran dan kegembiraan ${pet.nickname} pulih 100%!")
            }
            itemId == "item_pet_evo_crystal" -> {
                val pet = dao.getEquippedPetSync(accountId) ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                dao.updatePet(pet.copy(exp = pet.exp + 300, happiness = 100))
                Result.success("🔮 Menyalurkan Kristal Jiwa ke ${pet.nickname}! Mendapatkan +300 EXP Pet!")
            }
            itemId == "item_pet_awakening_stone" -> {
                val pet = dao.getEquippedPetSync(accountId) ?: return@withContext Result.failure(Exception("Pasang pet peliharaan terlebih dahulu!"))
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item habis"))
                dao.updatePet(pet.copy(exp = pet.exp + 800, happiness = 100))
                Result.success("💎 Menyalurkan Batu Kebangkitan ke ${pet.nickname}! Mendapatkan +800 EXP Pet!")
            }
            itemId == "item_chest_wooden" -> {
                val keyCount = getItemCount("item_key_wooden")
                if (keyCount <= 0) return@withContext Result.failure(Exception("Butuh 1 Kunci Kayu (🗝️) untuk membuka peti ini!"))
                removeItem("item_chest_wooden", 1)
                removeItem("item_key_wooden", 1)
                val goldReward = Random.nextInt(150, 320)
                addGold(goldReward)
                addPlayerExp(25)
                addItem("crop_seed_wortel", ItemType.SEED, "Benih Wortel", 2, "🥕", ItemRarity.COMMON, 28)
                Result.success("📦 Peti Kayu Terbuka! Mendapatkan +$goldReward Gold, 2 Benih Wortel & +25 EXP!")
            }
            itemId == "item_chest_iron" -> {
                val keyCount = getItemCount("item_key_iron")
                if (keyCount <= 0) return@withContext Result.failure(Exception("Butuh 1 Kunci Besi (🔑) untuk membuka peti ini!"))
                removeItem("item_chest_iron", 1)
                removeItem("item_key_iron", 1)
                val goldReward = Random.nextInt(400, 750)
                addGold(goldReward)
                addPlayerExp(50)
                addItem("item_upgrade_stone", ItemType.MATERIAL, "Batu Peningkat Tempa", 1, "🪨", ItemRarity.RARE, 100)
                addItem("item_fertilizer", ItemType.MATERIAL, "Pupuk Organik", 2, "✨", ItemRarity.COMMON, 25)
                Result.success("🧰 Peti Besi Terbuka! Mendapatkan +$goldReward Gold, 1 Batu Tempa & 2 Pupuk!")
            }
            itemId == "item_chest_gold" -> {
                val keyCount = getItemCount("item_key_gold")
                if (keyCount <= 0) return@withContext Result.failure(Exception("Butuh 1 Kunci Emas (✨🔑) untuk membuka peti ini!"))
                removeItem("item_chest_gold", 1)
                removeItem("item_key_gold", 1)
                val goldReward = Random.nextInt(1500, 2500)
                addGold(goldReward)
                addPlayerExp(150)
                val profile = dao.getPlayerProfileSync(accountId)
                if (profile != null) {
                    dao.insertOrUpdatePlayer(profile.copy(diamonds = profile.diamonds + 15, upgradeStones = profile.upgradeStones + 3))
                }
                addItem("item_gem_ruby", ItemType.MATERIAL, "Permata Rubi Membara", 1, "🔻", ItemRarity.EPIC, 750)
                Result.success("🎁 Peti Emas Terbuka! Hadiah Megah: +$goldReward Gold, +15 Diamond, 3 Batu Tempa & 1 Permata Rubi!")
            }
            item.itemType == "FOOD" -> {
                if (!removeItem(itemId, 1)) return@withContext Result.failure(Exception("Item tidak ditemukan"))
                restoreEnergy(35)
                restoreHp(60)
                Result.success("🍲 Menikmati hidangan ${item.name}! Pulih +35 Stamina & +60 HP.")
            }
            else -> {
                Result.failure(Exception("Item ${item.name} belum bisa dipakai langsung."))
            }
        }
    }

    suspend fun craftBlacksmithRecipe(recipeKey: String): Result<String> = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext Result.failure(Exception("Akun belum aktif"))
        when (recipeKey) {
            "forge_stone" -> {
                val ironCount = getItemCount("item_iron_ore")
                if (ironCount < 3) return@withContext Result.failure(Exception("Butuh 3 Bijih Besi Padat (⚙️)"))
                if (!spendGold(50)) return@withContext Result.failure(Exception("Biaya tempa butuh 50 Gold"))
                removeItem("item_iron_ore", 3)
                addItem("item_upgrade_stone", ItemType.MATERIAL, "Batu Peningkat Tempa", 1, "🪨", ItemRarity.RARE, 100)
                addPlayerExp(20)
                val profile = dao.getPlayerProfileSync(accountId)
                if (profile != null) {
                    dao.insertOrUpdatePlayer(profile.copy(upgradeStones = profile.upgradeStones + 1))
                }
                Result.success("🔨 Berhasil menempa 1 Batu Peningkat Tempa!")
            }
            "forge_gold_ingot" -> {
                val stoneCount = getItemCount("item_upgrade_stone")
                if (stoneCount < 2) return@withContext Result.failure(Exception("Butuh 2 Batu Peningkat Tempa (🪨)"))
                if (!spendGold(100)) return@withContext Result.failure(Exception("Biaya tempa butuh 100 Gold"))
                removeItem("item_upgrade_stone", 2)
                addItem("item_gold_ingot", ItemType.MATERIAL, "Batangan Emas Murni", 1, "🪙", ItemRarity.RARE, 300)
                addPlayerExp(40)
                Result.success("🔨 Berhasil melebur 1 Batangan Emas Murni!")
            }
            "forge_dragon_armor" -> {
                val scaleCount = getItemCount("item_dragon_scale")
                val stoneCount = getItemCount("item_upgrade_stone")
                if (scaleCount < 1 || stoneCount < 2) return@withContext Result.failure(Exception("Butuh 1 Sisik Naga & 2 Batu Tempa"))
                if (!spendGold(250)) return@withContext Result.failure(Exception("Biaya tempa butuh 250 Gold"))
                removeItem("item_dragon_scale", 1)
                removeItem("item_upgrade_stone", 2)
                addItem("item_dragon_armor", ItemType.MATERIAL, "Zirah Sisik Naga Purba", 1, "🛡️", ItemRarity.LEGENDARY, 1600)
                addPlayerExp(100)
                Result.success("🛡️ Berhasil menempa Zirah Sisik Naga Purba legendaris!")
            }
            else -> Result.failure(Exception("Resep tidak ditemukan"))
        }
    }

    // --- Farming Management ---
    suspend fun unlockPlot(plotIndex: Int, cost: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        if (!spendGold(cost)) return@withContext false
        val plots = dao.getPlotsSync(accountId)
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        dao.updatePlot(target.copy(isUnlocked = true))
        true
    }

    suspend fun plantSeed(plotIndex: Int, cropIdOrSeed: String): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false

        // Normalize crop ID from seed ID or crop name
        val raw = cropIdOrSeed.removePrefix("crop_seed_").removePrefix("item_crop_").removePrefix("item_")
        val normalizedId = when (raw.lowercase()) {
            "crop_corn", "corn" -> "jagung"
            "crop_carrot", "carrot" -> "wortel"
            "crop_wheat", "wheat" -> "gandum"
            "crop_spinach", "spinach" -> "bayam"
            "crop_potato", "potato" -> "kentang"
            "crop_tomato", "tomato" -> "tomat"
            "crop_chili", "chili" -> "cabai"
            "crop_melon", "melon" -> "semangka"
            "crop_dragonfruit", "dragonfruit" -> "dragon_fruit_crop"
            "crop_aether", "aether" -> "crystal_flower"
            else -> raw.removePrefix("crop_")
        }

        val cropDef = GameDatabaseRegistry.CROPS.find { it.id == normalizedId || it.id == raw }
            ?: return@withContext false

        // Look for any matching seed in inventory
        val candidateSeedIds = listOf(
            "crop_seed_${cropDef.id}",
            "crop_seed_crop_${cropDef.id}",
            cropIdOrSeed,
            "crop_seed_$raw"
        ).distinct()

        var removed = false
        for (seedId in candidateSeedIds) {
            if (removeItem(seedId, 1)) {
                removed = true
                break
            }
        }
        if (!removed) return@withContext false

        val plots = dao.getPlotsSync(accountId)
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        val now = System.currentTimeMillis()
        val durationMillis = cropDef.growDurationSeconds * 1000L

        dao.updatePlot(
            target.copy(
                cropId = cropDef.id,
                plantedAtMillis = now,
                harvestAtMillis = now + durationMillis,
                isWatered = false,
                isFertilized = false,
                currentStage = 1
            )
        )
        true
    }

    suspend fun waterPlot(plotIndex: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        val plots = dao.getPlotsSync(accountId)
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        if (target.cropId == null || target.isWatered) return@withContext false

        val now = System.currentTimeMillis()
        val remaining = max(0L, target.harvestAtMillis - now)
        val acceleratedHarvest = now + (remaining * 0.8f).toLong()

        dao.updatePlot(
            target.copy(
                isWatered = true,
                harvestAtMillis = acceleratedHarvest
            )
        )
        true
    }

    suspend fun fertilizePlot(plotIndex: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        val plots = dao.getPlotsSync(accountId)
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext false
        if (target.cropId == null || target.isFertilized) return@withContext false

        if (!removeItem("item_fertilizer", 1)) return@withContext false

        val now = System.currentTimeMillis()
        val remaining = max(0L, target.harvestAtMillis - now)
        val acceleratedHarvest = now + (remaining * 0.7f).toLong()

        dao.updatePlot(
            target.copy(
                isFertilized = true,
                harvestAtMillis = acceleratedHarvest
            )
        )
        true
    }

    suspend fun harvestPlot(plotIndex: Int): Result<String> = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext Result.failure(Exception("Akun belum aktif"))
        val plots = dao.getPlotsSync(accountId)
        val target = plots.find { it.plotIndex == plotIndex } ?: return@withContext Result.failure(Exception("Petak tidak ada"))
        val cropId = target.cropId ?: return@withContext Result.failure(Exception("Tidak ada tanaman"))
        val cropDef = GameDatabaseRegistry.CROPS.find { it.id == cropId } ?: return@withContext Result.failure(Exception("Tanaman tidak dikenal"))

        val now = System.currentTimeMillis()
        if (now < target.harvestAtMillis) {
            return@withContext Result.failure(Exception("Tanaman belum matang untuk dipanen!"))
        }

        val baseYield = Random.nextInt(1, 3)
        val totalYield = if (target.isFertilized) baseYield * 2 else baseYield

        addItem(
            itemId = "item_crop_${cropDef.id}",
            itemType = ItemType.CROP,
            name = cropDef.name,
            count = totalYield,
            iconEmoji = cropDef.iconEmoji,
            rarity = cropDef.rarity,
            sellPrice = cropDef.sellPrice
        )

        if (Random.nextFloat() < 0.35f) {
            addItem(
                itemId = "crop_seed_${cropDef.id}",
                itemType = ItemType.SEED,
                name = cropDef.seedName,
                count = 1,
                iconEmoji = cropDef.iconEmoji,
                rarity = cropDef.rarity,
                sellPrice = cropDef.seedCost / 2
            )
        }

        addPlayerExp(cropDef.expReward)

        dao.updatePlot(
            target.copy(
                cropId = null,
                plantedAtMillis = 0L,
                harvestAtMillis = 0L,
                isWatered = false,
                isFertilized = false,
                currentStage = 0
            )
        )

        incrementQuestProgress("FARM", totalYield)
        Result.success("Panen berhasil! Mendapatkan $totalYield ${cropDef.name} +${cropDef.expReward} EXP")
    }

    // --- Fishing Management ---
    suspend fun recordCatchFish(fish: FishDef, weightKg: Float): Int = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext 0
        val weightMultiplier = (weightKg / fish.minWeightKg).coerceIn(1.0f, 2.5f)
        val finalPrice = (fish.basePrice * weightMultiplier).toInt()

        addItem(
            itemId = "item_fish_${fish.id}",
            itemType = ItemType.FISH,
            name = "${fish.name} (${String.format("%.1f", weightKg)}kg)",
            count = 1,
            iconEmoji = fish.iconEmoji,
            rarity = fish.rarity,
            sellPrice = finalPrice
        )

        val existingDex = dao.getFishDexById(accountId, fish.id)
        if (existingDex != null) {
            dao.insertOrUpdateFishDex(
                existingDex.copy(
                    countCaught = existingDex.countCaught + 1,
                    maxWeightKg = max(existingDex.maxWeightKg, weightKg),
                    isDiscovered = true
                )
            )
        } else {
            dao.insertOrUpdateFishDex(
                FishDexEntity(
                    accountId = accountId,
                    fishId = fish.id,
                    fishName = fish.name,
                    rarity = fish.rarity.name,
                    iconEmoji = fish.iconEmoji,
                    countCaught = 1,
                    maxWeightKg = weightKg,
                    isDiscovered = true
                )
            )
        }

        addPlayerExp(fish.expReward)
        incrementQuestProgress("FISH", 1)
        finalPrice
    }

    // --- Pet Management ---
    suspend fun feedPet(
        petId: Int,
        foodItemId: String,
        customHungerBoost: Int = 30,
        customExpBoost: Int = 15
    ): Result<String> = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext Result.failure(Exception("Akun belum aktif"))
        val pet = dao.getPetById(accountId, petId) ?: return@withContext Result.failure(Exception("Pet tidak ditemukan"))
        if (!removeItem(foodItemId, 1)) return@withContext Result.failure(Exception("Bahan makanan habis"))

        val newHunger = min(100, pet.hunger + customHungerBoost)
        val newHappiness = min(100, pet.happiness + 15)
        val expGain = customExpBoost

        var currentExp = pet.exp + expGain
        var level = pet.level
        var maxExp = pet.maxExp
        var stage = pet.evolutionStage

        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level += 1
            maxExp = (maxExp * 1.3f).toInt()
        }

        if (level >= 25 && stage < 3) {
            stage = 3
        } else if (level >= 10 && stage < 2) {
            stage = 2
        }

        dao.updatePet(
            pet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                exp = currentExp,
                level = level,
                maxExp = maxExp,
                evolutionStage = stage
            )
        )

        incrementQuestProgress("PET", 1)
        Result.success("Pet merasa kenyang & senang! +$expGain Pet EXP")
    }

    suspend fun trainPet(petId: Int): Result<String> = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext Result.failure(Exception("Akun belum aktif"))
        val pet = dao.getPetById(accountId, petId) ?: return@withContext Result.failure(Exception("Pet tidak ditemukan"))
        if (pet.hunger < 15) {
            return@withContext Result.failure(Exception("Pet terlalu lapar untuk berlatih! Beri makan terlebih dahulu."))
        }
        if (!useEnergy(5)) {
            return@withContext Result.failure(Exception("Stamina pemain tidak cukup (butuh 5)"))
        }

        val newHunger = max(0, pet.hunger - 15)
        val newHappiness = min(100, pet.happiness + 20)
        val expGain = 35

        var currentExp = pet.exp + expGain
        var level = pet.level
        var maxExp = pet.maxExp
        var stage = pet.evolutionStage

        while (currentExp >= maxExp) {
            currentExp -= maxExp
            level += 1
            maxExp = (maxExp * 1.3f).toInt()
        }

        if (level >= 25 && stage < 3) {
            stage = 3
        } else if (level >= 10 && stage < 2) {
            stage = 2
        }

        dao.updatePet(
            pet.copy(
                hunger = newHunger,
                happiness = newHappiness,
                exp = currentExp,
                level = level,
                maxExp = maxExp,
                evolutionStage = stage
            )
        )

        incrementQuestProgress("PET", 1)
        Result.success("Latihan selesai! Pet semakin kuat. +$expGain Pet EXP")
    }

    suspend fun evolvePetManual(petId: Int): Result<String> = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext Result.failure(Exception("Akun belum aktif"))
        val pet = dao.getPetById(accountId, petId) ?: return@withContext Result.failure(Exception("Pet tidak ditemukan"))
        val profile = dao.getPlayerProfileSync(accountId) ?: return@withContext Result.failure(Exception("Profil kosong"))

        val requiredLevel = if (pet.evolutionStage == 1) 10 else 25
        if (pet.level < requiredLevel) {
            return@withContext Result.failure(Exception("Pet harus mencapai Level $requiredLevel untuk berevolusi!"))
        }
        if (pet.evolutionStage >= 3) {
            return@withContext Result.failure(Exception("Pet sudah mencapai evolusi puncak!"))
        }

        val requiredStones = if (pet.evolutionStage == 1) 2 else 5
        if (profile.upgradeStones < requiredStones) {
            return@withContext Result.failure(Exception("Butuh $requiredStones Batu Peningkat untuk evolusi!"))
        }

        dao.insertOrUpdatePlayer(profile.copy(upgradeStones = profile.upgradeStones - requiredStones))
        val nextStage = pet.evolutionStage + 1
        dao.updatePet(pet.copy(evolutionStage = nextStage))

        Result.success("✨ Selamat! Pet berhasil berevolusi ke Stage $nextStage!")
    }

    suspend fun equipPet(petId: Int) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        dao.clearAllEquipped(accountId)
        dao.setEquippedPet(accountId, petId)
    }

    suspend fun adoptPet(speciesId: String, nickname: String, costGold: Int): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        if (!spendGold(costGold)) return@withContext false
        val newPet = PetEntity(
            id = 0,
            accountId = accountId,
            speciesId = speciesId,
            nickname = nickname,
            level = 1,
            exp = 0,
            maxExp = 50,
            evolutionStage = 1,
            hunger = 80,
            happiness = 90,
            isEquipped = false
        )
        dao.insertPet(newPet)
        true
    }

    // --- Quests ---
    suspend fun incrementQuestProgress(type: String, amount: Int) = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext
        val quests = dao.getQuestsSync(accountId)
        for (q in quests) {
            if (q.questType == type && !q.isCompleted) {
                val newCount = min(q.targetCount, q.currentCount + amount)
                val completed = newCount >= q.targetCount
                dao.updateQuest(
                    q.copy(
                        currentCount = newCount,
                        isCompleted = completed
                    )
                )
            }
        }
    }

    suspend fun claimQuest(questId: String): Boolean = withContext(Dispatchers.IO) {
        val accountId = getActiveAccountId() ?: return@withContext false
        val quests = dao.getQuestsSync(accountId)
        val target = quests.find { it.id == questId } ?: return@withContext false
        if (!target.isCompleted || target.isClaimed) return@withContext false

        addGold(target.rewardGold)
        addPlayerExp(target.rewardExp)
        if (target.rewardDiamonds > 0) {
            val p = dao.getPlayerProfileSync(accountId)
            if (p != null) {
                dao.insertOrUpdatePlayer(p.copy(diamonds = p.diamonds + target.rewardDiamonds))
            }
        }

        dao.updateQuest(target.copy(isClaimed = true))
        true
    }

    // --- USER ACCOUNT MANAGEMENT ---
    suspend fun registerAccount(
        username: String,
        email: String,
        password: String,
        discordId: String? = null
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim().lowercase()
        val trimmedDiscord = discordId?.trim()?.ifBlank { null }

        val uErr = SecurityUtils.validateUsername(trimmedUsername)
        if (uErr != null) return@withContext Result.failure(Exception(uErr))

        val eErr = SecurityUtils.validateEmail(trimmedEmail)
        if (eErr != null) return@withContext Result.failure(Exception(eErr))

        val pErr = SecurityUtils.validatePassword(password)
        if (pErr != null) return@withContext Result.failure(Exception(pErr))

        val existingUser = dao.getUserAccountByUsername(trimmedUsername)
        if (existingUser != null) {
            return@withContext Result.failure(Exception("Username '$trimmedUsername' sudah terdaftar!"))
        }

        val existingEmail = dao.getUserAccountByEmail(trimmedEmail)
        if (existingEmail != null) {
            return@withContext Result.failure(Exception("Email '$trimmedEmail' sudah terdaftar!"))
        }

        val hash = SecurityUtils.hashPassword(password)
        val safeUsername = trimmedUsername.lowercase().replace(" ", "_")
        val accountId = "usr_${System.currentTimeMillis()}_$safeUsername"
        val linkedList = mutableListOf("LOCAL", "EMAIL", "USERNAME")
        if (trimmedDiscord != null) linkedList.add("DISCORD")

        dao.clearActiveUserAccounts()
        val newAccount = UserAccountEntity(
            id = accountId,
            username = trimmedUsername,
            email = trimmedEmail,
            passwordHash = hash,
            discordId = trimmedDiscord,
            provider = if (trimmedDiscord != null) "DISCORD" else "LOCAL",
            avatarUrl = null,
            isCurrentActive = true,
            linkedProviders = linkedList.joinToString(","),
            createdAtMillis = System.currentTimeMillis(),
            lastLoginMillis = System.currentTimeMillis()
        )
        dao.insertOrUpdateUserAccount(newAccount)
        initializeAccountDataIfNeeded(newAccount.id, newAccount.username)

        Result.success(newAccount)
    }

    suspend fun loginWithUsernameOrEmail(
        identifier: String,
        password: String
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(Exception("Username atau email tidak boleh kosong!"))
        }
        if (password.isBlank()) {
            return@withContext Result.failure(Exception("Password tidak boleh kosong!"))
        }

        val account = dao.getUserAccountByUsernameOrEmail(trimmed)
            ?: return@withContext Result.failure(Exception("Akun '$trimmed' tidak ditemukan! Silakan daftar terlebih dahulu."))

        if (account.passwordHash == null) {
            return@withContext Result.failure(Exception("Akun ini terdaftar lewat ${account.provider}. Silakan login lewat provider tersebut."))
        }

        if (!SecurityUtils.verifyPassword(password, account.passwordHash)) {
            return@withContext Result.failure(Exception("Password salah! Periksa kembali kata sandi."))
        }

        dao.clearActiveUserAccounts()
        val updated = account.copy(
            isCurrentActive = true,
            lastLoginMillis = System.currentTimeMillis()
        )
        dao.insertOrUpdateUserAccount(updated)
        initializeAccountDataIfNeeded(updated.id, updated.username)

        Result.success(updated)
    }

    suspend fun loginWithDiscordAccount(
        discordTag: String,
        email: String? = null
    ): Result<UserAccountEntity> = withContext(Dispatchers.IO) {
        val trimmedTag = discordTag.trim()
        val dErr = SecurityUtils.validateDiscordTag(trimmedTag)
        if (dErr != null) return@withContext Result.failure(Exception(dErr))

        var account = dao.getUserAccountByDiscord(trimmedTag)
        if (account == null) {
            val username = trimmedTag.split("#").firstOrNull() ?: trimmedTag
            val safeDiscord = trimmedTag.lowercase().replace("#", "_").replace(" ", "_")
            val generatedEmail = email?.trim()?.ifBlank { null } ?: "$safeDiscord@discord.realm"
            val accountId = "usr_dc_${System.currentTimeMillis()}_$safeDiscord"

            account = UserAccountEntity(
                id = accountId,
                username = username,
                email = generatedEmail,
                discordId = trimmedTag,
                provider = "DISCORD",
                avatarUrl = null,
                isCurrentActive = true,
                linkedProviders = "DISCORD",
                createdAtMillis = System.currentTimeMillis(),
                lastLoginMillis = System.currentTimeMillis()
            )
            dao.clearActiveUserAccounts()
            dao.insertOrUpdateUserAccount(account)
            initializeAccountDataIfNeeded(account.id, account.username)
        } else {
            dao.clearActiveUserAccounts()
            val updated = account.copy(
                isCurrentActive = true,
                lastLoginMillis = System.currentTimeMillis()
            )
            dao.insertOrUpdateUserAccount(updated)
            initializeAccountDataIfNeeded(updated.id, updated.username)
            account = updated
        }

        Result.success(account)
    }

    suspend fun linkDiscordToCurrentAccount(discordTag: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedTag = discordTag.trim()
        val dErr = SecurityUtils.validateDiscordTag(trimmedTag)
        if (dErr != null) return@withContext Result.failure(Exception(dErr))

        val existing = dao.getUserAccountByDiscord(trimmedTag)
        if (existing != null) {
            return@withContext Result.failure(Exception("Tag Discord '$trimmedTag' sudah terhubung ke akun lain!"))
        }

        val active = dao.getActiveUserAccountSync()
            ?: return@withContext Result.failure(Exception("Tidak ada akun aktif!"))

        val currentLinked = active.linkedProviders.split(",").filter { it.isNotBlank() }.toMutableList()
        if (!currentLinked.contains("DISCORD")) {
            currentLinked.add("DISCORD")
        }

        val updated = active.copy(
            discordId = trimmedTag,
            linkedProviders = currentLinked.joinToString(",")
        )
        dao.insertOrUpdateUserAccount(updated)
        Result.success("Berhasil menautkan Discord ($trimmedTag) ke akun ${active.username}!")
    }

    suspend fun loginWithProvider(
        provider: String,
        username: String,
        email: String,
        avatarUrl: String? = null
    ): UserAccountEntity = withContext(Dispatchers.IO) {
        val safeIdentifier = if (email.isNotBlank()) email.lowercase().replace("@", "_at_").replace(".", "_") else username.lowercase().replace(" ", "_")
        val accountId = "${provider.lowercase()}_$safeIdentifier"
        dao.clearActiveUserAccounts()

        val existing = dao.getUserAccountById(accountId)
        val updatedLinkedProviders = if (existing != null) {
            val list = existing.linkedProviders.split(",").filter { it.isNotBlank() }.toMutableList()
            if (!list.contains(provider)) list.add(provider)
            list.joinToString(",")
        } else {
            provider
        }

        val account = UserAccountEntity(
            id = accountId,
            username = username,
            email = email,
            provider = provider,
            avatarUrl = avatarUrl,
            isCurrentActive = true,
            linkedProviders = updatedLinkedProviders,
            createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
            lastLoginMillis = System.currentTimeMillis()
        )
        dao.insertOrUpdateUserAccount(account)
        initializeAccountDataIfNeeded(account.id, account.username)
        account
    }

    suspend fun loginAsGuest(): UserAccountEntity = withContext(Dispatchers.IO) {
        dao.clearActiveUserAccounts()
        val guestId = "guest_default"
        var guest = dao.getUserAccountById(guestId)
        if (guest == null) {
            guest = UserAccountEntity(
                id = guestId,
                username = "Tamu Petualang",
                email = "guest@rpgrealm.local",
                provider = "GUEST",
                isCurrentActive = true,
                linkedProviders = "GUEST"
            )
            dao.insertOrUpdateUserAccount(guest)
        } else {
            dao.setActiveUserAccount(guestId)
        }
        initializeAccountDataIfNeeded(guest.id, guest.username)
        guest
    }

    suspend fun linkProviderToCurrentAccount(provider: String): Result<String> = withContext(Dispatchers.IO) {
        val active = dao.getActiveUserAccountSync() ?: return@withContext Result.failure(Exception("Tidak ada akun aktif"))
        val currentLinked = active.linkedProviders.split(",").filter { it.isNotBlank() }
        if (currentLinked.contains(provider)) {
            return@withContext Result.failure(Exception("Akun ini sudah terhubung dengan $provider!"))
        }

        val newLinked = (currentLinked + provider).joinToString(",")
        val updated = active.copy(linkedProviders = newLinked)
        dao.insertOrUpdateUserAccount(updated)
        Result.success("Berhasil menghubungkan akun dengan $provider!")
    }

    suspend fun switchUserAccount(accountId: String): Boolean = withContext(Dispatchers.IO) {
        val target = dao.getUserAccountById(accountId) ?: return@withContext false
        dao.clearActiveUserAccounts()
        dao.setActiveUserAccount(target.id)
        initializeAccountDataIfNeeded(target.id, target.username)
        true
    }

    suspend fun logoutCurrentAccount() = withContext(Dispatchers.IO) {
        dao.clearActiveUserAccounts()
    }
}
