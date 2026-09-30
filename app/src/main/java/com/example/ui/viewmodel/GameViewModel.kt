package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FarmPlotEntity
import com.example.data.local.entity.FishDexEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PetEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.model.CookingRecipe
import com.example.data.model.CropDef
import com.example.data.model.DungeonZoneDef
import com.example.data.model.FishDef
import com.example.data.model.GameDatabaseRegistry
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import com.example.data.model.MonsterDef
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

sealed class FishingState {
    object Idle : FishingState()
    object Casting : FishingState()
    object WaitingBite : FishingState()
    data class StrikeAlert(val remainingMillis: Long) : FishingState()
    data class Reeling(
        val fish: FishDef,
        val weightKg: Float,
        val tension: Float, // 0..100
        val targetZoneMin: Float,
        val targetZoneMax: Float,
        val catchProgress: Float // 0..100
    ) : FishingState()
    data class Caught(val fish: FishDef, val weightKg: Float, val rewardGold: Int, val expReward: Int) : FishingState()
    data class Escaped(val reason: String) : FishingState()
}

data class CombatState(
    val inCombat: Boolean = false,
    val zone: DungeonZoneDef? = null,
    val currentMonsterIndex: Int = 0,
    val monster: MonsterDef? = null,
    val monsterMaxHp: Int = 0,
    val monsterCurrentHp: Int = 0,
    val playerCurrentHp: Int = 100,
    val playerMaxHp: Int = 100,
    val isPlayerTurn: Boolean = true,
    val isGuarding: Boolean = false,
    val battleLogs: List<String> = emptyList(),
    val isVictory: Boolean = false,
    val isDefeat: Boolean = false,
    val lootGained: String = ""
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
        viewModelScope.launch {
            repository.initializeGameIfNeeded()
            startFarmTicker()
        }
    }

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val farmPlots: StateFlow<List<FarmPlotEntity>> = repository.allPlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pets: StateFlow<List<PetEntity>> = repository.allPets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equippedPet: StateFlow<PetEntity?> = repository.equippedPet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val inventory: StateFlow<List<InventoryItemEntity>> = repository.inventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fishDex: StateFlow<List<FishDexEntity>> = repository.fishDex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyQuests: StateFlow<List<QuestEntity>> = repository.dailyQuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents.asSharedFlow()

    // Fishing State
    private val _fishingState = MutableStateFlow<FishingState>(FishingState.Idle)
    val fishingState: StateFlow<FishingState> = _fishingState.asStateFlow()

    private var fishingJob: Job? = null
    private var reelingJob: Job? = null
    private var isHoldingReel = false

    // Combat State
    private val _combatState = MutableStateFlow(CombatState())
    val combatState: StateFlow<CombatState> = _combatState.asStateFlow()

    fun showToast(msg: String) {
        viewModelScope.launch {
            _messageEvents.emit(msg)
        }
    }

    // --- FARMING ACTIONS ---
    fun plantSeed(plotIndex: Int, cropId: String) {
        viewModelScope.launch {
            val success = repository.plantSeed(plotIndex, cropId)
            if (success) {
                val crop = GameDatabaseRegistry.CROPS.find { it.id == cropId }
                showToast("Berhasil menanam ${crop?.name ?: "Benih"}!")
            } else {
                showToast("Gagal menanam benih (periksa inventaris).")
            }
        }
    }

    fun waterPlot(plotIndex: Int) {
        viewModelScope.launch {
            val success = repository.waterPlot(plotIndex)
            if (success) {
                showToast("Petak disiram! Pertumbuhan dipercepat 20%.")
            }
        }
    }

    fun fertilizePlot(plotIndex: Int) {
        viewModelScope.launch {
            val success = repository.fertilizePlot(plotIndex)
            if (success) {
                showToast("Pupuk diberikan! Hasil panen akan meningkat 2x lipat.")
            } else {
                showToast("Pupuk tidak ada di tas (beli di Toko Pasar).")
            }
        }
    }

    fun harvestPlot(plotIndex: Int) {
        viewModelScope.launch {
            val result = repository.harvestPlot(plotIndex)
            result.onSuccess { msg ->
                showToast(msg)
            }.onFailure { err ->
                showToast(err.message ?: "Gagal panen.")
            }
        }
    }

    fun unlockPlot(plotIndex: Int, cost: Int) {
        viewModelScope.launch {
            val success = repository.unlockPlot(plotIndex, cost)
            if (success) {
                showToast("Petak tanah baru terbuka!")
            } else {
                showToast("Koin tidak cukup untuk membuka petak ($cost Gold).")
            }
        }
    }

    private fun startFarmTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                // Trigger flow refresh if needed
            }
        }
    }

    // --- FISHING ACTIONS ---
    fun startFishing(baitId: String) {
        if (_fishingState.value !is FishingState.Idle &&
            _fishingState.value !is FishingState.Caught &&
            _fishingState.value !is FishingState.Escaped
        ) return

        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            if (!repository.useEnergy(2)) {
                showToast("Stamina tidak cukup untuk memancing! (butuh 2)")
                return@launch
            }

            // Consume bait if not default
            if (baitId != "bait_cacing") {
                val hasBait = repository.removeItem("bait_$baitId", 1)
                if (!hasBait) {
                    showToast("Umpan habis! Menggunakan umpan biasa.")
                }
            } else {
                repository.removeItem("bait_$baitId", 1)
            }

            _fishingState.value = FishingState.Casting
            delay(1200)

            _fishingState.value = FishingState.WaitingBite

            // Wait for bite (2 to 5 seconds)
            val biteDelay = Random.nextLong(2200, 4800)
            delay(biteDelay)

            // Alert STRIKE! Player has 2.2 seconds to tap strike
            _fishingState.value = FishingState.StrikeAlert(2200)

            fishingJob?.cancel()
            fishingJob = launch {
                delay(2200)
                if (_fishingState.value is FishingState.StrikeAlert) {
                    _fishingState.value = FishingState.Escaped("Ikan kabur karena terlambat menarik kail!")
                }
            }
        }
    }

    fun onStrikeTap() {
        val state = _fishingState.value
        if (state !is FishingState.StrikeAlert) return
        fishingJob?.cancel()

        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            val rodLevel = profile.fishingRodLevel
            val rodTier = GameDatabaseRegistry.ROD_TIERS.getOrElse(rodLevel - 1) { GameDatabaseRegistry.ROD_TIERS[0] }

            // Determine target fish based on rod level & random roll
            val candidateFishes = GameDatabaseRegistry.FISHES.filter { it.requiredRodLevel <= rodLevel }
            // Weight probability by rarity and rod bonus
            val selectedFish = pickFishWithRarityWeights(candidateFishes, rodTier.rareBonusPercent)
            val weight = Random.nextDouble(selectedFish.minWeightKg.toDouble(), selectedFish.maxWeightKg.toDouble()).toFloat()

            // Start Reeling Mini-Game
            val targetMin = Random.nextFloat() * 40f + 10f // 10..50
            val targetMax = targetMin + 28f // zone width 28

            _fishingState.value = FishingState.Reeling(
                fish = selectedFish,
                weightKg = weight,
                tension = 45f,
                targetZoneMin = targetMin,
                targetZoneMax = targetMax,
                catchProgress = 20f
            )

            startReelingSimulation(selectedFish, weight)
        }
    }

    fun setReelButtonHolding(isHolding: Boolean) {
        isHoldingReel = isHolding
    }

    private fun startReelingSimulation(fish: FishDef, weight: Float) {
        reelingJob?.cancel()
        reelingJob = viewModelScope.launch {
            var tension = 45f
            var progress = 25f
            var zoneMin = 25f
            var zoneMax = 55f
            var fishDirection = 1f

            val rodMultiplier = GameDatabaseRegistry.ROD_TIERS.getOrElse((playerProfile.value?.fishingRodLevel ?: 1) - 1) { GameDatabaseRegistry.ROD_TIERS[0] }.catchSpeedMultiplier

            while (_fishingState.value is FishingState.Reeling) {
                delay(80)

                // Fish movement
                if (Random.nextFloat() < 0.12f) {
                    fishDirection *= -1f
                }
                zoneMin = (zoneMin + (fishDirection * Random.nextFloat() * 4.5f)).coerceIn(10f, 60f)
                zoneMax = zoneMin + 30f

                // Player tension control
                if (isHoldingReel) {
                    tension = (tension + 3.8f).coerceIn(0f, 100f)
                } else {
                    tension = (tension - 2.8f).coerceIn(0f, 100f)
                }

                // Check if tension is in sweet spot
                val isInZone = tension in zoneMin..zoneMax
                if (isInZone) {
                    progress += (2.2f * rodMultiplier)
                } else {
                    progress = (progress - 1.2f).coerceAtLeast(0f)
                }

                if (tension >= 98f) {
                    _fishingState.value = FishingState.Escaped("Tali pancing putus karena tarikan terlalu kencang!")
                    break
                }
                if (tension <= 2f && progress > 5f) {
                    progress -= 2.5f
                }

                if (progress >= 100f) {
                    // Successfully caught!
                    val rewardGold = repository.recordCatchFish(fish, weight)
                    _fishingState.value = FishingState.Caught(
                        fish = fish,
                        weightKg = weight,
                        rewardGold = rewardGold,
                        expReward = fish.expReward
                    )
                    break
                }

                _fishingState.value = FishingState.Reeling(
                    fish = fish,
                    weightKg = weight,
                    tension = tension,
                    targetZoneMin = zoneMin,
                    targetZoneMax = zoneMax,
                    catchProgress = progress
                )
            }
        }
    }

    fun resetFishing() {
        fishingJob?.cancel()
        reelingJob?.cancel()
        _fishingState.value = FishingState.Idle
    }

    private fun pickFishWithRarityWeights(candidates: List<FishDef>, bonusChance: Int): FishDef {
        val roll = Random.nextInt(100) + bonusChance
        val targetRarity = when {
            roll >= 115 -> ItemRarity.MYTHIC
            roll >= 95 -> ItemRarity.LEGENDARY
            roll >= 75 -> ItemRarity.EPIC
            roll >= 45 -> ItemRarity.RARE
            roll >= 20 -> ItemRarity.UNCOMMON
            else -> ItemRarity.COMMON
        }
        val match = candidates.filter { it.rarity == targetRarity }
        return if (match.isNotEmpty()) {
            match.random()
        } else {
            candidates.random()
        }
    }

    // --- PET ACTIONS ---
    fun feedPet(petId: Int, foodItemId: String) {
        viewModelScope.launch {
            val result = repository.feedPet(petId, foodItemId)
            result.onSuccess { msg -> showToast(msg) }
                .onFailure { err -> showToast(err.message ?: "Gagal memberi makan pet.") }
        }
    }

    fun trainPet(petId: Int) {
        viewModelScope.launch {
            val result = repository.trainPet(petId)
            result.onSuccess { msg -> showToast(msg) }
                .onFailure { err -> showToast(err.message ?: "Gagal melatih pet.") }
        }
    }

    fun evolvePet(petId: Int) {
        viewModelScope.launch {
            val result = repository.evolvePetManual(petId)
            result.onSuccess { msg -> showToast(msg) }
                .onFailure { err -> showToast(err.message ?: "Gagal evolusi pet.") }
        }
    }

    fun equipPet(petId: Int) {
        viewModelScope.launch {
            repository.equipPet(petId)
            showToast("Pet dipasang sebagai pendamping petualangan!")
        }
    }

    fun adoptNewPet(speciesId: String, nickname: String, costGold: Int) {
        viewModelScope.launch {
            val success = repository.adoptPet(speciesId, nickname, costGold)
            if (success) {
                showToast("Selamat! $nickname telah bergabung dengan petualanganmu!")
            } else {
                showToast("Koin tidak mencukupi ($costGold Gold).")
            }
        }
    }

    // --- COMBAT / ADVENTURE ACTIONS ---
    fun enterDungeon(zone: DungeonZoneDef) {
        val profile = playerProfile.value ?: return
        if (profile.level < zone.levelReq) {
            showToast("Level pemain belum cukup! Butuh Level ${zone.levelReq}")
            return
        }
        viewModelScope.launch {
            if (!repository.useEnergy(zone.energyCost)) {
                showToast("Stamina tidak cukup (butuh ${zone.energyCost})")
                return@launch
            }

            val monster = zone.monsters.random()
            _combatState.value = CombatState(
                inCombat = true,
                zone = zone,
                monster = monster,
                monsterMaxHp = monster.hp,
                monsterCurrentHp = monster.hp,
                playerCurrentHp = profile.hp,
                playerMaxHp = profile.maxHp,
                isPlayerTurn = true,
                isGuarding = false,
                battleLogs = listOf("⚔️ Memasuki ${zone.name}! Monster ${monster.name} ${monster.iconEmoji} muncul menghadang!"),
                isVictory = false,
                isDefeat = false
            )
        }
    }

    fun combatPlayerAttack() {
        val state = _combatState.value
        if (!state.inCombat || !state.isPlayerTurn || state.monster == null) return

        val profile = playerProfile.value ?: return
        val pet = equippedPet.value

        // Player base damage: 15 + level * 3
        val playerAtk = 15 + (profile.level * 3) + Random.nextInt(-3, 6)
        val petBonus = if (pet != null) {
            (pet.level * 2) + (pet.evolutionStage * 5)
        } else 0

        val totalDmg = max(5, (playerAtk + petBonus) - state.monster.defense)
        val newMonsterHp = max(0, state.monsterCurrentHp - totalDmg)

        val log = "🗡️ Kamu menyerang ${state.monster.name} menimbulkan $totalDmg damage!" +
                if (pet != null) " (Dibantu ${pet.nickname} +${petBonus} dmg)" else ""

        if (newMonsterHp <= 0) {
            // Victory
            handleCombatVictory(state.monster)
        } else {
            _combatState.value = state.copy(
                monsterCurrentHp = newMonsterHp,
                isPlayerTurn = false,
                isGuarding = false,
                battleLogs = state.battleLogs + log
            )
            triggerMonsterTurn()
        }
    }

    fun combatPetSpecialSkill() {
        val state = _combatState.value
        if (!state.inCombat || !state.isPlayerTurn || state.monster == null) return
        val pet = equippedPet.value
        if (pet == null) {
            showToast("Kamu belum memasang Pet pendamping!")
            return
        }

        viewModelScope.launch {
            if (!repository.useEnergy(3)) {
                showToast("Stamina tidak cukup untuk jurus combo! (butuh 3)")
                return@launch
            }

            val petDmg = ((pet.level * 5) + (pet.evolutionStage * 25) + Random.nextInt(10, 25))
            val finalDmg = max(10, petDmg - (state.monster.defense / 2))
            val newMonsterHp = max(0, state.monsterCurrentHp - finalDmg)

            val log = "✨ JURUS PET! ${pet.nickname} melancarkan serangan pamungkas dahsyat! Menghasilkan $finalDmg DAMAGE!"

            if (newMonsterHp <= 0) {
                handleCombatVictory(state.monster)
            } else {
                _combatState.value = state.copy(
                    monsterCurrentHp = newMonsterHp,
                    isPlayerTurn = false,
                    isGuarding = false,
                    battleLogs = state.battleLogs + log
                )
                triggerMonsterTurn()
            }
        }
    }

    fun combatGuard() {
        val state = _combatState.value
        if (!state.inCombat || !state.isPlayerTurn) return

        _combatState.value = state.copy(
            isPlayerTurn = false,
            isGuarding = true,
            battleLogs = state.battleLogs + "🛡️ Kamu mengambil posisi bertahan! Damage monster akan berkurang 50%."
        )
        triggerMonsterTurn()
    }

    fun combatUsePotion() {
        val state = _combatState.value
        if (!state.inCombat || !state.isPlayerTurn) return

        viewModelScope.launch {
            val used = repository.removeItem("item_potion_hp", 1)
            if (used) {
                val healAmount = 70
                val newHp = min(state.playerMaxHp, state.playerCurrentHp + healAmount)
                repository.restoreHp(healAmount)

                _combatState.value = state.copy(
                    playerCurrentHp = newHp,
                    battleLogs = state.battleLogs + "🧪 Meminum Ramuan Darah! HP pulih +$healAmount!"
                )
            } else {
                showToast("Ramuan Darah habis di tas!")
            }
        }
    }

    private fun triggerMonsterTurn() {
        viewModelScope.launch {
            delay(1000)
            val state = _combatState.value
            if (!state.inCombat || state.monster == null) return@launch

            val rawDmg = state.monster.attack + Random.nextInt(-2, 4)
            val finalDmg = if (state.isGuarding) max(2, rawDmg / 2) else max(4, rawDmg)

            val newPlayerHp = max(0, state.playerCurrentHp - finalDmg)
            repository.damagePlayer(finalDmg)

            val log = "💥 ${state.monster.name} menyerang balik dengan kuat! Kamu terkena $finalDmg damage."

            if (newPlayerHp <= 0) {
                _combatState.value = state.copy(
                    playerCurrentHp = 0,
                    isDefeat = true,
                    battleLogs = state.battleLogs + log + "☠️ Kamu pingsan dan dievakuasi kembali ke desa!"
                )
            } else {
                _combatState.value = state.copy(
                    playerCurrentHp = newPlayerHp,
                    isPlayerTurn = true,
                    isGuarding = false,
                    battleLogs = state.battleLogs + log
                )
            }
        }
    }

    private fun handleCombatVictory(monster: MonsterDef) {
        viewModelScope.launch {
            repository.addGold(monster.goldDrop)
            repository.addPlayerExp(monster.expDrop)
            repository.incrementQuestProgress("DUNGEON", 1)

            val dropStone = Random.nextFloat() < monster.dropStoneChance
            var lootDesc = "+${monster.goldDrop} Gold, +${monster.expDrop} EXP"
            if (dropStone) {
                repository.addItem(
                    itemId = "item_upgrade_stone",
                    itemType = ItemType.MATERIAL,
                    name = "Batu Peningkat Tempa",
                    count = 1,
                    iconEmoji = "🪨",
                    rarity = ItemRarity.RARE,
                    sellPrice = 100
                )
                // add upgrade stone to profile
                val p = playerProfile.value
                if (p != null) {
                    repository.addGold(0) // sync
                }
                lootDesc += ", +1 Batu Peningkat Tempa! 🪨"
            }

            _combatState.value = _combatState.value.copy(
                monsterCurrentHp = 0,
                isVictory = true,
                lootGained = lootDesc,
                battleLogs = _combatState.value.battleLogs + "🎉 Kemenangan! Monster ${monster.name} telah dikalahkan!"
            )
        }
    }

    fun exitCombat() {
        _combatState.value = CombatState()
    }

    // --- MARKET & CRAFTING ---
    fun buyItem(
        itemId: String,
        name: String,
        itemType: ItemType,
        iconEmoji: String,
        rarity: ItemRarity,
        costGold: Int,
        count: Int = 1
    ) {
        viewModelScope.launch {
            val totalCost = costGold * count
            if (repository.spendGold(totalCost)) {
                repository.addItem(itemId, itemType, name, count, iconEmoji, rarity, costGold / 2)
                showToast("Membeli $count $name seharga $totalCost Gold!")
            } else {
                showToast("Koin tidak cukup! Butuh $totalCost Gold.")
            }
        }
    }

    fun sellItem(item: InventoryItemEntity, count: Int = 1) {
        viewModelScope.launch {
            if (repository.removeItem(item.id, count)) {
                val totalEarned = item.sellPrice * count
                repository.addGold(totalEarned)
                showToast("Menjual $count ${item.name} seharga $totalEarned Gold!")
            }
        }
    }

    fun upgradeFishingRod() {
        viewModelScope.launch {
            val result = repository.upgradeFishingRod()
            result.onSuccess { newLevel ->
                val tier = GameDatabaseRegistry.ROD_TIERS[newLevel - 1]
                showToast("🔨 Sukses menaikkan pancing ke ${tier.name} (Lv.$newLevel)!")
            }.onFailure { err ->
                showToast(err.message ?: "Gagal meningkatkan pancing.")
            }
        }
    }

    fun cookRecipe(recipe: CookingRecipe) {
        viewModelScope.launch {
            val hasCrop = repository.getItemCount("item_crop_${recipe.requiredCropId}") >= recipe.requiredCropCount
            val hasFishOrSub = repository.getItemCount("item_${recipe.requiredFishId}") >= recipe.requiredFishCount ||
                    repository.getItemCount(recipe.requiredFishId) >= recipe.requiredFishCount

            if (!hasCrop || !hasFishOrSub) {
                showToast("Bahan masakan belum mencukupi!")
                return@launch
            }

            repository.removeItem("item_crop_${recipe.requiredCropId}", recipe.requiredCropCount)
            val removedSub = repository.removeItem("item_${recipe.requiredFishId}", recipe.requiredFishCount)
            if (!removedSub) {
                repository.removeItem(recipe.requiredFishId, recipe.requiredFishCount)
            }

            repository.restoreEnergy(recipe.energyRestored)
            repository.restoreHp(recipe.hpRestored)
            repository.addPlayerExp(25)

            showToast("🍲 Memasak ${recipe.name}! Memulihkan +${recipe.energyRestored} Stamina & +${recipe.hpRestored} HP!")
        }
    }

    fun claimQuest(questId: String) {
        viewModelScope.launch {
            val success = repository.claimQuest(questId)
            if (success) {
                showToast("🎁 Hadiah misi berhasil diklaim!")
            }
        }
    }

    fun useItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            val result = repository.useConsumableItem(item)
            result.onSuccess { msg ->
                showToast(msg)
            }.onFailure { err ->
                showToast(err.message ?: "Gagal menggunakan item.")
            }
        }
    }

    fun craftBlacksmith(recipeKey: String) {
        viewModelScope.launch {
            val result = repository.craftBlacksmithRecipe(recipeKey)
            result.onSuccess { msg ->
                showToast(msg)
            }.onFailure { err ->
                showToast(err.message ?: "Gagal menempa bahan.")
            }
        }
    }
}
