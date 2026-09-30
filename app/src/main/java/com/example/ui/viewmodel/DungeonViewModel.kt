package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PetEntity
import com.example.data.local.entity.PlayerProfileEntity
import com.example.data.model.AcquiredLootRecord
import com.example.data.model.CombatEnemyStatus
import com.example.data.model.CombatPlayerStatus
import com.example.data.model.DungeonCombatState
import com.example.data.model.DungeonDifficulty
import com.example.data.model.DungeonRegistry
import com.example.data.model.DungeonWave
import com.example.data.model.EnemyRank
import com.example.data.model.EnemySkill
import com.example.data.model.HuntingDungeon
import com.example.data.model.HuntingEnemy
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

class DungeonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private var combatSequenceJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.gameDao())
    }

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val equippedPet: StateFlow<PetEntity?> = repository.equippedPet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Selected Difficulty
    private val _selectedDifficulty = MutableStateFlow(DungeonDifficulty.NORMAL)
    val selectedDifficulty: StateFlow<DungeonDifficulty> = _selectedDifficulty.asStateFlow()

    // Primary State-based Combat Machine
    private val _combatState = MutableStateFlow<DungeonCombatState>(DungeonCombatState.HubSelection)
    val combatState: StateFlow<DungeonCombatState> = _combatState.asStateFlow()

    // Player Health & Dungeon Progress Observable States
    private val _playerHealth = MutableStateFlow(100)
    val playerHealth: StateFlow<Int> = _playerHealth.asStateFlow()

    private val _playerMaxHealth = MutableStateFlow(100)
    val playerMaxHealth: StateFlow<Int> = _playerMaxHealth.asStateFlow()

    private val _dungeonProgress = MutableStateFlow(0.0f)
    val dungeonProgress: StateFlow<Float> = _dungeonProgress.asStateFlow()

    private val _currentWaveIndex = MutableStateFlow(0)
    val currentWaveIndex: StateFlow<Int> = _currentWaveIndex.asStateFlow()

    private val _totalWaves = MutableStateFlow(3)
    val totalWaves: StateFlow<Int> = _totalWaves.asStateFlow()

    // Accumulated Rewards throughout the dungeon run
    private val _accumulatedGold = MutableStateFlow(0)
    val accumulatedGold: StateFlow<Int> = _accumulatedGold.asStateFlow()

    private val _accumulatedExp = MutableStateFlow(0)
    val accumulatedExp: StateFlow<Int> = _accumulatedExp.asStateFlow()

    private val _accumulatedDrops = MutableStateFlow<List<AcquiredLootRecord>>(emptyList())
    val accumulatedDrops: StateFlow<List<AcquiredLootRecord>> = _accumulatedDrops.asStateFlow()

    // Status Tracking
    private val _playerStatus = MutableStateFlow(CombatPlayerStatus(100, 100))
    val playerStatus: StateFlow<CombatPlayerStatus> = _playerStatus.asStateFlow()

    private val _enemyStatus = MutableStateFlow(CombatEnemyStatus(100, 100))
    val enemyStatus: StateFlow<CombatEnemyStatus> = _enemyStatus.asStateFlow()

    private var activeDungeon: HuntingDungeon? = null
    private var turnsTakenInDungeon = 0
    private var totalDamageDealtInDungeon = 0

    private val _notificationEvents = MutableSharedFlow<String>()
    val notificationEvents: SharedFlow<String> = _notificationEvents.asSharedFlow()

    fun setDifficulty(difficulty: DungeonDifficulty) {
        if (_combatState.value is DungeonCombatState.HubSelection) {
            _selectedDifficulty.value = difficulty
        }
    }

    // --- START DUNGEON RUN ---
    fun enterDungeon(dungeon: HuntingDungeon) {
        val profile = playerProfile.value ?: return
        if (profile.level < dungeon.minPlayerLevel) {
            emitToast("Level pemain belum mencukupi (Minimal Level ${dungeon.minPlayerLevel})")
            return
        }

        viewModelScope.launch {
            if (!repository.useEnergy(dungeon.staminaCost)) {
                emitToast("Stamina tidak mencukupi (Butuh ${dungeon.staminaCost} Stamina)")
                return@launch
            }

            activeDungeon = dungeon
            turnsTakenInDungeon = 0
            totalDamageDealtInDungeon = 0
            _accumulatedGold.value = 0
            _accumulatedExp.value = 0
            _accumulatedDrops.value = emptyList()
            _currentWaveIndex.value = 0
            _totalWaves.value = dungeon.waves.size
            _dungeonProgress.value = 0.0f

            val initialPlayerStatus = CombatPlayerStatus(
                currentHp = profile.hp,
                maxHp = profile.maxHp
            )
            _playerStatus.value = initialPlayerStatus
            _playerHealth.value = profile.hp
            _playerMaxHealth.value = profile.maxHp

            loadWave(dungeon, _selectedDifficulty.value, 0)
        }
    }

    private fun loadWave(dungeon: HuntingDungeon, difficulty: DungeonDifficulty, waveIdx: Int) {
        if (waveIdx !in dungeon.waves.indices) {
            _combatState.value = DungeonCombatState.HubSelection
            return
        }
        val wave = dungeon.waves[waveIdx]
        _currentWaveIndex.value = waveIdx + 1
        _dungeonProgress.value = (waveIdx.toFloat() / dungeon.waves.size.toFloat()).coerceIn(0f, 1f)

        // Scale enemy stats according to chosen difficulty
        val scaledEnemyHp = (wave.enemy.maxHp * difficulty.statMultiplier).toInt()
        val initialEnemyStatus = CombatEnemyStatus(
            currentHp = scaledEnemyHp,
            maxHp = scaledEnemyHp
        )
        _enemyStatus.value = initialEnemyStatus

        _combatState.value = DungeonCombatState.WaveEntrance(
            dungeon = dungeon,
            difficulty = difficulty,
            wave = wave
        )

        // Transition into first turn after entrance animation
        combatSequenceJob?.cancel()
        combatSequenceJob = viewModelScope.launch {
            delay(1500)
            _combatState.value = DungeonCombatState.PlayerTurn(
                dungeon = dungeon,
                difficulty = difficulty,
                wave = wave,
                playerStatus = _playerStatus.value,
                enemyStatus = _enemyStatus.value,
                logs = listOf(
                    "⚔️ Masuk ${wave.waveName}!",
                    "💀 Musuh ${wave.enemy.name} ${wave.enemy.iconEmoji} (${wave.enemy.rank.label}) bersiap bertempur!"
                ),
                turnCount = 1
            )
        }
    }

    // --- STATE-BASED COMBAT ACTIONS ---

    fun onPlayerActionLightAttack() {
        executePlayerCombatAction(
            actionName = "Serangan Pedang Presisi",
            baseMultiplier = 1.0f,
            staminaCost = 0,
            guaranteedCrit = false
        )
    }

    fun onPlayerActionHeavySkill() {
        val profile = playerProfile.value ?: return
        if (profile.energy < 2) {
            emitToast("Stamina tidak cukup untuk jurus tebasan! (Butuh 2 Stamina)")
            return
        }

        viewModelScope.launch {
            repository.useEnergy(2)
            executePlayerCombatAction(
                actionName = "Tebasan Badai Pedang (Skill)",
                baseMultiplier = 1.85f,
                staminaCost = 2,
                guaranteedCrit = Random.nextFloat() < 0.35f
            )
        }
    }

    fun onPlayerActionPetSynergy() {
        val pet = equippedPet.value
        if (pet == null) {
            emitToast("Belum ada Pet Companion yang dipasang!")
            return
        }
        val profile = playerProfile.value ?: return
        if (profile.energy < 3) {
            emitToast("Stamina tidak cukup untuk serangan combo pet! (Butuh 3 Stamina)")
            return
        }

        viewModelScope.launch {
            repository.useEnergy(3)
            val petMultiplier = 1.5f + (pet.evolutionStage * 0.5f) + (pet.level * 0.05f)
            executePlayerCombatAction(
                actionName = "Serangan Sinergi Combo (${pet.nickname} ${pet.evolutionStage}⭐)",
                baseMultiplier = petMultiplier,
                staminaCost = 3,
                isPetAction = true
            )
        }
    }

    fun onPlayerActionGuard() {
        val currentTurn = _combatState.value as? DungeonCombatState.PlayerTurn ?: return
        val updatedPlayerStatus = currentTurn.playerStatus.copy(
            isGuarding = true,
            shieldAmount = currentTurn.playerStatus.shieldAmount + (currentTurn.playerStatus.maxHp * 0.15f).toInt()
        )
        _playerStatus.value = updatedPlayerStatus

        combatSequenceJob?.cancel()
        combatSequenceJob = viewModelScope.launch {
            val log = "🛡️ Kamu mengambil kuda-kuda bertahan! Mendapatkan +${(currentTurn.playerStatus.maxHp * 0.15f).toInt()} Shield dan pengurangan damage 50%."

            _combatState.value = DungeonCombatState.ResolvingAction(
                dungeon = currentTurn.dungeon,
                difficulty = currentTurn.difficulty,
                wave = currentTurn.wave,
                playerStatus = updatedPlayerStatus,
                enemyStatus = currentTurn.enemyStatus,
                actionDescription = "🛡️ Mengaktifkan Tameng Bertahan!",
                isCritical = false,
                damageValue = 0,
                isTargetEnemy = false
            )

            delay(1000)
            triggerEnemyTurn(currentTurn.dungeon, currentTurn.difficulty, currentTurn.wave, currentTurn.logs + log, currentTurn.turnCount)
        }
    }

    fun onPlayerActionUsePotion() {
        val currentTurn = _combatState.value as? DungeonCombatState.PlayerTurn ?: return
        viewModelScope.launch {
            val used = repository.removeItem("item_potion_hp", 1)
            if (!used) {
                emitToast("Ramuan Darah habis di tas! (Bisa beli di Toko Desa)")
                return@launch
            }

            val healAmount = (currentTurn.playerStatus.maxHp * 0.40f).toInt()
            val newHp = min(currentTurn.playerStatus.maxHp, currentTurn.playerStatus.currentHp + healAmount)
            repository.restoreHp(healAmount)

            val updatedPlayerStatus = currentTurn.playerStatus.copy(currentHp = newHp)
            _playerStatus.value = updatedPlayerStatus
            _playerHealth.value = newHp

            val log = "🧪 Meminum Ramuan Darah! Memulihkan +$healAmount HP (${newHp}/${currentTurn.playerStatus.maxHp})."

            _combatState.value = DungeonCombatState.ResolvingAction(
                dungeon = currentTurn.dungeon,
                difficulty = currentTurn.difficulty,
                wave = currentTurn.wave,
                playerStatus = updatedPlayerStatus,
                enemyStatus = currentTurn.enemyStatus,
                actionDescription = "🧪 Pulih +$healAmount HP!",
                isCritical = false,
                damageValue = healAmount,
                isTargetEnemy = false
            )

            delay(1000)
            triggerEnemyTurn(currentTurn.dungeon, currentTurn.difficulty, currentTurn.wave, currentTurn.logs + log, currentTurn.turnCount)
        }
    }

    private fun executePlayerCombatAction(
        actionName: String,
        baseMultiplier: Float,
        staminaCost: Int,
        guaranteedCrit: Boolean = false,
        isPetAction: Boolean = false
    ) {
        val currentTurn = _combatState.value as? DungeonCombatState.PlayerTurn ?: return
        val profile = playerProfile.value ?: return
        val pet = equippedPet.value

        combatSequenceJob?.cancel()
        combatSequenceJob = viewModelScope.launch {
            turnsTakenInDungeon++

            // Calculate damage
            val baseAtk = 18 + (profile.level * 4) + (pet?.let { (it.level * 2) + (it.evolutionStage * 6) } ?: 0)
            val isCrit = guaranteedCrit || (Random.nextFloat() < 0.15f)
            val critMultiplier = if (isCrit) 1.8f else 1.0f

            val rawDamage = (baseAtk * baseMultiplier * critMultiplier).toInt() + Random.nextInt(-3, 6)
            val enemyDef = (currentTurn.wave.enemy.defense * currentTurn.difficulty.statMultiplier).toInt()
            val finalDamage = max(8, rawDamage - enemyDef)

            totalDamageDealtInDungeon += finalDamage
            val newEnemyHp = max(0, currentTurn.enemyStatus.currentHp - finalDamage)

            val updatedEnemyStatus = currentTurn.enemyStatus.copy(currentHp = newEnemyHp)
            _enemyStatus.value = updatedEnemyStatus

            val log = "🗡️ $actionName menghasilkan $finalDamage DAMAGE!" + if (isCrit) " (CRITICAL HIT! 🔥)" else ""

            _combatState.value = DungeonCombatState.ResolvingAction(
                dungeon = currentTurn.dungeon,
                difficulty = currentTurn.difficulty,
                wave = currentTurn.wave,
                playerStatus = currentTurn.playerStatus,
                enemyStatus = updatedEnemyStatus,
                actionDescription = log,
                isCritical = isCrit,
                damageValue = finalDamage,
                isTargetEnemy = true
            )

            delay(1200)

            if (newEnemyHp <= 0) {
                // Enemy defeated on this wave!
                handleWaveDefeated(currentTurn.dungeon, currentTurn.difficulty, currentTurn.wave)
            } else {
                // Enemy responds with counter attack
                triggerEnemyTurn(currentTurn.dungeon, currentTurn.difficulty, currentTurn.wave, currentTurn.logs + log, currentTurn.turnCount)
            }
        }
    }

    private fun triggerEnemyTurn(
        dungeon: HuntingDungeon,
        difficulty: DungeonDifficulty,
        wave: DungeonWave,
        logs: List<String>,
        turnCount: Int
    ) {
        viewModelScope.launch {
            delay(500)
            val currentEnemyStatus = _enemyStatus.value
            val currentPlayerStatus = _playerStatus.value

            if (currentEnemyStatus.currentHp <= 0) return@launch

            // Enemy AI skill choice
            val useSkill = wave.enemy.skills.isNotEmpty() && (Random.nextFloat() < 0.40f)
            val chosenSkill: EnemySkill? = if (useSkill) wave.enemy.skills.random() else null

            val baseEnemyAtk = (wave.enemy.attack * difficulty.statMultiplier).toInt()
            val skillMultiplier = chosenSkill?.damageMultiplier ?: 1.0f
            val isEnemyCrit = Random.nextFloat() < wave.enemy.critChance

            val rawDamage = ((baseEnemyAtk * skillMultiplier * (if (isEnemyCrit) 1.5f else 1.0f))).toInt() + Random.nextInt(-2, 4)

            // Calculate defense & player guard
            var damageToPlayer = if (currentPlayerStatus.isGuarding) max(3, rawDamage / 2) else max(5, rawDamage)

            // Check shields
            var newShield = currentPlayerStatus.shieldAmount
            if (newShield > 0) {
                if (newShield >= damageToPlayer) {
                    newShield -= damageToPlayer
                    damageToPlayer = 0
                } else {
                    damageToPlayer -= newShield
                    newShield = 0
                }
            }

            val newPlayerHp = max(0, currentPlayerStatus.currentHp - damageToPlayer)
            repository.damagePlayer(damageToPlayer)
            _playerHealth.value = newPlayerHp

            // Apply status effects
            var burnTurns = currentPlayerStatus.burnTurns
            var poisonTurns = currentPlayerStatus.poisonTurns
            if (chosenSkill?.appliesBurn == true) burnTurns = 2
            if (chosenSkill?.appliesPoison == true) poisonTurns = 3

            val updatedPlayerStatus = currentPlayerStatus.copy(
                currentHp = newPlayerHp,
                shieldAmount = newShield,
                isGuarding = false,
                burnTurns = burnTurns,
                poisonTurns = poisonTurns
            )
            _playerStatus.value = updatedPlayerStatus

            val attackDesc = if (chosenSkill != null) {
                "💥 ${wave.enemy.name} melancarkan jurus [${chosenSkill.name}] ${chosenSkill.iconEmoji} menghasilkan $damageToPlayer damage!"
            } else {
                "💥 ${wave.enemy.name} menyerang biasa menghasilkan $damageToPlayer damage!"
            }

            _combatState.value = DungeonCombatState.ResolvingAction(
                dungeon = dungeon,
                difficulty = difficulty,
                wave = wave,
                playerStatus = updatedPlayerStatus,
                enemyStatus = currentEnemyStatus,
                actionDescription = attackDesc,
                isCritical = isEnemyCrit,
                damageValue = damageToPlayer,
                isTargetEnemy = false
            )

            delay(1200)

            if (newPlayerHp <= 0) {
                // Defeat
                _combatState.value = DungeonCombatState.DungeonDefeat(
                    dungeon = dungeon,
                    difficulty = difficulty,
                    waveFailed = wave.waveNumber,
                    defeatReason = "Darah pemain terkuras habis akibat serangan ganas ${wave.enemy.name}."
                )
            } else {
                // Return to Player Turn
                _combatState.value = DungeonCombatState.PlayerTurn(
                    dungeon = dungeon,
                    difficulty = difficulty,
                    wave = wave,
                    playerStatus = updatedPlayerStatus,
                    enemyStatus = currentEnemyStatus,
                    logs = logs + attackDesc,
                    turnCount = turnCount + 1
                )
            }
        }
    }

    private fun handleWaveDefeated(dungeon: HuntingDungeon, difficulty: DungeonDifficulty, wave: DungeonWave) {
        viewModelScope.launch {
            // Roll wave drops
            val earnedGold = (wave.enemy.goldReward * difficulty.statMultiplier).toInt()
            val earnedExp = (wave.enemy.expReward * difficulty.expMultiplier).toInt()

            _accumulatedGold.value += earnedGold
            _accumulatedExp.value += earnedExp

            // Roll drop table
            val newDrops = mutableListOf<AcquiredLootRecord>()
            for (drop in wave.enemy.dropTable) {
                val effectiveChance = (drop.baseChance * difficulty.dropRateMultiplier).coerceIn(0f, 1.0f)
                if (Random.nextFloat() <= effectiveChance) {
                    val count = Random.nextInt(drop.minCount, drop.maxCount + 1)
                    newDrops.add(
                        AcquiredLootRecord(
                            name = drop.name,
                            count = count,
                            iconEmoji = drop.iconEmoji,
                            rarity = drop.rarity
                        )
                    )
                    // Persist to repository inventory
                    repository.addItem(
                        itemId = drop.itemId,
                        itemType = drop.itemType,
                        name = drop.name,
                        count = count,
                        iconEmoji = drop.iconEmoji,
                        rarity = drop.rarity,
                        sellPrice = drop.sellPrice
                    )
                }
            }
            _accumulatedDrops.value = _accumulatedDrops.value + newDrops

            // Check if next wave exists or if Dungeon is Cleared
            if (wave.waveNumber < dungeon.waves.size) {
                _combatState.value = DungeonCombatState.WaveCompleted(
                    dungeon = dungeon,
                    difficulty = difficulty,
                    currentWave = wave,
                    nextWaveNumber = wave.waveNumber + 1,
                    totalWaves = dungeon.waves.size,
                    interimExpGained = earnedExp,
                    interimGoldGained = earnedGold
                )
            } else {
                // VICTORY! All waves cleared!
                val totalFinalGold = _accumulatedGold.value + (dungeon.guaranteedGold * difficulty.statMultiplier).toInt()
                val totalFinalExp = _accumulatedExp.value + (dungeon.guaranteedExp * difficulty.expMultiplier).toInt()

                repository.addGold(totalFinalGold)
                repository.addPlayerExp(totalFinalExp)
                repository.incrementQuestProgress("DUNGEON", 1)

                val stars = when {
                    turnsTakenInDungeon <= 9 -> 3
                    turnsTakenInDungeon <= 15 -> 2
                    else -> 1
                }

                _dungeonProgress.value = 1.0f
                _combatState.value = DungeonCombatState.DungeonVictory(
                    dungeon = dungeon,
                    difficulty = difficulty,
                    totalTurns = turnsTakenInDungeon,
                    totalDamageDealt = totalDamageDealtInDungeon,
                    totalExpEarned = totalFinalExp,
                    totalGoldEarned = totalFinalGold,
                    dropsAcquired = _accumulatedDrops.value,
                    starRating = stars
                )
            }
        }
    }

    fun proceedToNextWave() {
        val completedState = _combatState.value as? DungeonCombatState.WaveCompleted ?: return
        val dungeon = completedState.dungeon
        val nextWaveIdx = completedState.nextWaveNumber - 1

        loadWave(dungeon, completedState.difficulty, nextWaveIdx)
    }

    fun fleeDungeon() {
        combatSequenceJob?.cancel()
        _combatState.value = DungeonCombatState.HubSelection
        emitToast("Berhasil melarikan diri dari dungeon.")
    }

    fun exitDungeonToHub() {
        combatSequenceJob?.cancel()
        _combatState.value = DungeonCombatState.HubSelection
    }

    private fun emitToast(message: String) {
        viewModelScope.launch {
            _notificationEvents.emit(message)
        }
    }
}
