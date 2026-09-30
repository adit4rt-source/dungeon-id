package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class DungeonDifficulty(
    val label: String,
    val statMultiplier: Float,
    val dropRateMultiplier: Float,
    val expMultiplier: Float,
    val colorHex: Long
) {
    NORMAL("Normal", 1.0f, 1.0f, 1.0f, 0xFF4CAF50),
    HEROIC("Heroic", 1.5f, 1.6f, 1.8f, 0xFFFFA726),
    MYTHIC("Mythic", 2.4f, 2.8f, 3.2f, 0xFFFF1744);

    fun composeColor(): Color = Color(colorHex)
}

enum class EnemyRank(val label: String, val badgeColorHex: Long) {
    MINION("Kroco", 0xFF9E9E9E),
    ELITE("Elite", 0xFFAB47BC),
    MINI_BOSS("Mini-Boss", 0xFFFFA726),
    DUNGEON_BOSS("Raja Dungeon", 0xFFFF1744);

    fun composeColor(): Color = Color(badgeColorHex)
}

data class EnemySkill(
    val id: String,
    val name: String,
    val description: String,
    val damageMultiplier: Float,
    val appliesBurn: Boolean = false,
    val appliesPoison: Boolean = false,
    val appliesStun: Boolean = false,
    val iconEmoji: String = "⚡"
)

data class DungeonLootDrop(
    val itemId: String,
    val name: String,
    val itemType: ItemType,
    val iconEmoji: String,
    val rarity: ItemRarity,
    val baseChance: Float, // 0.0f to 1.0f
    val minCount: Int = 1,
    val maxCount: Int = 1,
    val sellPrice: Int = 50
)

data class HuntingEnemy(
    val id: String,
    val name: String,
    val rank: EnemyRank,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val critChance: Float,
    val iconEmoji: String,
    val skills: List<EnemySkill>,
    val goldReward: Int,
    val expReward: Int,
    val dropTable: List<DungeonLootDrop>,
    val description: String
)

data class DungeonWave(
    val waveNumber: Int,
    val totalWaves: Int,
    val waveName: String,
    val enemy: HuntingEnemy,
    val floorModifierText: String = "Medan Tempur Standar"
)

data class HuntingDungeon(
    val id: String,
    val name: String,
    val themeDescription: String,
    val minPlayerLevel: Int,
    val staminaCost: Int,
    val iconEmoji: String,
    val bannerGradientHexes: Pair<Long, Long>,
    val waves: List<DungeonWave>,
    val guaranteedGold: Int,
    val guaranteedExp: Int,
    val possibleKeyDrops: List<String>
)

data class CombatPlayerStatus(
    val currentHp: Int,
    val maxHp: Int,
    val shieldAmount: Int = 0,
    val isGuarding: Boolean = false,
    val burnTurns: Int = 0,
    val poisonTurns: Int = 0,
    val stunTurns: Int = 0,
    val bonusAttack: Int = 0
)

data class CombatEnemyStatus(
    val currentHp: Int,
    val maxHp: Int,
    val shieldAmount: Int = 0,
    val isTelegraphingSkill: EnemySkill? = null,
    val burnTurns: Int = 0,
    val poisonTurns: Int = 0,
    val stunTurns: Int = 0
)

data class AcquiredLootRecord(
    val name: String,
    val count: Int,
    val iconEmoji: String,
    val rarity: ItemRarity
)

sealed class DungeonCombatState {
    object HubSelection : DungeonCombatState()

    data class WaveEntrance(
        val dungeon: HuntingDungeon,
        val difficulty: DungeonDifficulty,
        val wave: DungeonWave
    ) : DungeonCombatState()

    data class PlayerTurn(
        val dungeon: HuntingDungeon,
        val difficulty: DungeonDifficulty,
        val wave: DungeonWave,
        val playerStatus: CombatPlayerStatus,
        val enemyStatus: CombatEnemyStatus,
        val logs: List<String>,
        val turnCount: Int
    ) : DungeonCombatState()

    data class ResolvingAction(
        val dungeon: HuntingDungeon,
        val difficulty: DungeonDifficulty,
        val wave: DungeonWave,
        val playerStatus: CombatPlayerStatus,
        val enemyStatus: CombatEnemyStatus,
        val actionDescription: String,
        val isCritical: Boolean = false,
        val damageValue: Int = 0,
        val isTargetEnemy: Boolean = true
    ) : DungeonCombatState()

    data class WaveCompleted(
        val dungeon: HuntingDungeon,
        val difficulty: DungeonDifficulty,
        val currentWave: DungeonWave,
        val nextWaveNumber: Int,
        val totalWaves: Int,
        val interimExpGained: Int,
        val interimGoldGained: Int
    ) : DungeonCombatState()

    data class DungeonVictory(
        val dungeon: HuntingDungeon,
        val difficulty: DungeonDifficulty,
        val totalTurns: Int,
        val totalDamageDealt: Int,
        val totalExpEarned: Int,
        val totalGoldEarned: Int,
        val dropsAcquired: List<AcquiredLootRecord>,
        val starRating: Int // 1 to 3 stars
    ) : DungeonCombatState()

    data class DungeonDefeat(
        val dungeon: HuntingDungeon,
        val difficulty: DungeonDifficulty,
        val waveFailed: Int,
        val defeatReason: String
    ) : DungeonCombatState()
}

object DungeonRegistry {

    private val SKILL_BITE = EnemySkill("sk_bite", "Gigitan Tajam", "Menyerang taring menimbulkan luka berat", 1.25f, iconEmoji = "🦷")
    private val SKILL_POISON_FANG = EnemySkill("sk_poison", "Taring Racun", "Menyuntikkan racun mengikis HP pemain", 1.1f, appliesPoison = true, iconEmoji = "🧪")
    private val SKILL_FIRE_BREATH = EnemySkill("sk_fire", "Semburan Api Lahar", "Menyemburkan api menyebabkan efek terbakar 2 giliran", 1.5f, appliesBurn = true, iconEmoji = "🔥")
    private val SKILL_EARTH_SLAM = EnemySkill("sk_slam", "Hentakan Guncangan", "Menghantam tanah dengan kekuatan dahsyat", 1.4f, appliesStun = true, iconEmoji = "💥")
    private val SKILL_INFERNO_EXPLOSION = EnemySkill("sk_inferno", "Ledakan Magma Purba", "Jurus pamungkas naga meluluhlantakkan pertahanan", 2.0f, appliesBurn = true, iconEmoji = "🌋")

    // Dungeon 1: Hutan Lumut Liar
    fun buildMossyForestDungeon(): HuntingDungeon {
        val dropStones = DungeonLootDrop("item_upgrade_stone", "Batu Peningkat Tempa", ItemType.MATERIAL, "🪨", ItemRarity.RARE, 0.40f, 1, 2)
        val dropHerbs = DungeonLootDrop("crop_seed_crop_carrot", "Benih Wortel", ItemType.SEED, "🥕", ItemRarity.COMMON, 0.50f, 1, 2)
        val dropWood = DungeonLootDrop("item_fertilizer", "Pupuk Organik", ItemType.MATERIAL, "✨", ItemRarity.COMMON, 0.35f, 1, 1)

        val wave1Enemy = HuntingEnemy(
            id = "e_slime_green",
            name = "Slime Beracun Liar",
            rank = EnemyRank.MINION,
            maxHp = 70,
            attack = 12,
            defense = 4,
            speed = 10,
            critChance = 0.05f,
            iconEmoji = "🟢",
            skills = listOf(SKILL_POISON_FANG),
            goldReward = 45,
            expReward = 30,
            dropTable = listOf(dropHerbs, dropWood),
            description = "Gumpalan cairan hutan yang asam dan suka melompat."
        )

        val wave2Enemy = HuntingEnemy(
            id = "e_goblin_thief",
            name = "Goblin Pencuri Senjata",
            rank = EnemyRank.ELITE,
            maxHp = 130,
            attack = 20,
            defense = 8,
            speed = 18,
            critChance = 0.12f,
            iconEmoji = "👺",
            skills = listOf(SKILL_BITE),
            goldReward = 90,
            expReward = 65,
            dropTable = listOf(dropStones, dropHerbs),
            description = "Prajurit goblin licik bersenjatakan belati berkarat."
        )

        val wave3Boss = HuntingEnemy(
            id = "e_boss_treant",
            name = "Raja Treant Lumut Kuno",
            rank = EnemyRank.DUNGEON_BOSS,
            maxHp = 260,
            attack = 28,
            defense = 14,
            speed = 8,
            critChance = 0.15f,
            iconEmoji = "🪵",
            skills = listOf(SKILL_EARTH_SLAM, SKILL_BITE),
            goldReward = 220,
            expReward = 150,
            dropTable = listOf(dropStones, dropWood, DungeonLootDrop("crop_seed_crop_melon", "Benih Semangka Emas", ItemType.SEED, "🍉", ItemRarity.RARE, 0.70f, 1, 2)),
            description = "Penjaga purba hutan lumut berwujud pohon raksasa."
        )

        return HuntingDungeon(
            id = "dungeon_forest",
            name = "Hutan Lumut Liar",
            themeDescription = "Hutan rimbun yang lembab dengan koloni monster rawa dan penguasa kayu kuno.",
            minPlayerLevel = 1,
            staminaCost = 5,
            iconEmoji = "🌲",
            bannerGradientHexes = Pair(0xFF1B5E20, 0xFF0D3311),
            waves = listOf(
                DungeonWave(1, 3, "Lantai 1: Rawa Slime", wave1Enemy, "Kabut racun tipis menyelimuti rawa"),
                DungeonWave(2, 3, "Lantai 2: Sarang Goblin", wave2Enemy, "Jebakan berduri di semak-semak"),
                DungeonWave(3, 3, "Lantai 3: Altar Pohon Purba (Boss)", wave3Boss, "Aura pelindung alam kuno aktif")
            ),
            guaranteedGold = 120,
            guaranteedExp = 90,
            possibleKeyDrops = listOf("Batu Peningkat Tempa 🪨", "Benih Semangka Emas 🍉", "Pupuk Organik ✨")
        )
    }

    // Dungeon 2: Gua Kristal Kegelapan
    fun buildCrystalCaveDungeon(): HuntingDungeon {
        val dropStones = DungeonLootDrop("item_upgrade_stone", "Batu Peningkat Tempa", ItemType.MATERIAL, "🪨", ItemRarity.RARE, 0.65f, 1, 3)
        val dropPotion = DungeonLootDrop("item_potion_hp", "Ramuan Darah Segar", ItemType.POTION, "🧪", ItemRarity.UNCOMMON, 0.45f, 1, 2)
        val dropAetherSeed = DungeonLootDrop("crop_seed_crop_aether", "Biji Aether Murni", ItemType.SEED, "🌸", ItemRarity.LEGENDARY, 0.30f, 1, 1)

        val wave1Enemy = HuntingEnemy(
            id = "e_crystal_bat",
            name = "Kelelawar Sonar Kristal",
            rank = EnemyRank.MINION,
            maxHp = 180,
            attack = 32,
            defense = 12,
            speed = 25,
            critChance = 0.10f,
            iconEmoji = "🦇",
            skills = listOf(SKILL_BITE),
            goldReward = 110,
            expReward = 85,
            dropTable = listOf(dropPotion),
            description = "Sayapnya dilapisi kristal tajam yang mendesis di kegelapan."
        )

        val wave2Enemy = HuntingEnemy(
            id = "e_shadow_spider",
            name = "Laba-Laba Bayangan Hitam",
            rank = EnemyRank.ELITE,
            maxHp = 270,
            attack = 42,
            defense = 18,
            speed = 20,
            critChance = 0.16f,
            iconEmoji = "🕷️",
            skills = listOf(SKILL_POISON_FANG, SKILL_BITE),
            goldReward = 180,
            expReward = 140,
            dropTable = listOf(dropStones, dropPotion),
            description = "Memintal jaring perekat berdaya racun tinggi."
        )

        val wave3Boss = HuntingEnemy(
            id = "e_boss_golem",
            name = "Titan Golem Kristal Safir",
            rank = EnemyRank.DUNGEON_BOSS,
            maxHp = 480,
            attack = 55,
            defense = 32,
            speed = 10,
            critChance = 0.18f,
            iconEmoji = "🗿",
            skills = listOf(SKILL_EARTH_SLAM),
            goldReward = 420,
            expReward = 320,
            dropTable = listOf(dropStones, dropAetherSeed, dropPotion),
            description = "Monster batu safir raksasa dengan tempurung tak tertembus."
        )

        return HuntingDungeon(
            id = "dungeon_crystal_cave",
            name = "Gua Kristal Kegelapan",
            themeDescription = "Gua stalaktit berkilau kristal gelap tempat monster bertulang baja bersembunyi.",
            minPlayerLevel = 5,
            staminaCost = 10,
            iconEmoji = "💎",
            bannerGradientHexes = Pair(0xFF0D47A1, 0xFF051C42),
            waves = listOf(
                DungeonWave(1, 3, "Lantai 1: Koridor Stalaktit", wave1Enemy, "Tetesan air gua menggema di dinding"),
                DungeonWave(2, 3, "Lantai 2: Jurang Jaring Laba-Laba", wave2Enemy, "Jaring lengket mengurangi kelincahan"),
                DungeonWave(3, 3, "Lantai 3: Inti Kristal Safir (Boss)", wave3Boss, "Getaran kristal memperkuat pertahanan")
            ),
            guaranteedGold = 280,
            guaranteedExp = 220,
            possibleKeyDrops = listOf("Batu Peningkat Tempa x3 🪨", "Biji Aether Murni 🌸", "Ramuan Darah 🧪")
        )
    }

    // Dungeon 3: Lembah Naga Lahar
    fun buildVolcanoDragonDungeon(): HuntingDungeon {
        val dropStones = DungeonLootDrop("item_upgrade_stone", "Batu Peningkat Tempa", ItemType.MATERIAL, "🪨", ItemRarity.RARE, 0.85f, 2, 4)
        val dropDragonFruit = DungeonLootDrop("crop_seed_crop_dragonfruit", "Benih Buah Naga Api", ItemType.SEED, "🐉", ItemRarity.EPIC, 0.60f, 1, 2)
        val dropElixir = DungeonLootDrop("item_potion_hp", "Ramuan Elixir Naga", ItemType.POTION, "🧪", ItemRarity.EPIC, 0.70f, 2, 3)

        val wave1Enemy = HuntingEnemy(
            id = "e_hellhound",
            name = "Hellhound Bersirip Magma",
            rank = EnemyRank.MINION,
            maxHp = 350,
            attack = 58,
            defense = 24,
            speed = 28,
            critChance = 0.15f,
            iconEmoji = "🐕‍🦺",
            skills = listOf(SKILL_FIRE_BREATH),
            goldReward = 260,
            expReward = 210,
            dropTable = listOf(dropDragonFruit),
            description = "Anjing neraka berkepala api dengan cakar pijar membara."
        )

        val wave2Enemy = HuntingEnemy(
            id = "e_wyvern_scout",
            name = "Wyvern Pengintai Lahar",
            rank = EnemyRank.ELITE,
            maxHp = 520,
            attack = 74,
            defense = 30,
            speed = 34,
            critChance = 0.20f,
            iconEmoji = "🦅",
            skills = listOf(SKILL_FIRE_BREATH, SKILL_BITE),
            goldReward = 410,
            expReward = 360,
            dropTable = listOf(dropStones, dropElixir),
            description = "Naga terbang pemburu yang menyambar mangsa dari langit lahar."
        )

        val wave3Boss = HuntingEnemy(
            id = "e_boss_ignis_lord",
            name = "Kaisar Naga Lahar Ignis",
            rank = EnemyRank.DUNGEON_BOSS,
            maxHp = 920,
            attack = 96,
            defense = 44,
            speed = 25,
            critChance = 0.25f,
            iconEmoji = "🔥🐲",
            skills = listOf(SKILL_INFERNO_EXPLOSION, SKILL_FIRE_BREATH, SKILL_EARTH_SLAM),
            goldReward = 1100,
            expReward = 850,
            dropTable = listOf(dropStones, dropDragonFruit, dropElixir),
            description = "Penguasa lahar purba yang tertidur di dalam kawah kiamat."
        )

        return HuntingDungeon(
            id = "dungeon_volcano",
            name = "Lembah Naga Lahar",
            themeDescription = "Kawah gunung berapi aktif bersuhu ekstrem dengan ancaman kawanan naga magma purba.",
            minPlayerLevel = 10,
            staminaCost = 15,
            iconEmoji = "🌋",
            bannerGradientHexes = Pair(0xFFB71C1C, 0xFF4A0A0A),
            waves = listOf(
                DungeonWave(1, 3, "Lantai 1: Lereng Abu Membara", wave1Enemy, "Udara panas membakar kulit"),
                DungeonWave(2, 3, "Lantai 2: Sarang Sarang Naga Terbang", wave2Enemy, "Hujan percikan magma cair"),
                DungeonWave(3, 3, "Lantai 3: Kawah Singgasana Naga (Boss)", wave3Boss, "Panas neraka menyelimuti arena!")
            ),
            guaranteedGold = 650,
            guaranteedExp = 550,
            possibleKeyDrops = listOf("Batu Peningkat Tempa x4 🪨", "Benih Buah Naga Api 🐉", "Elixir Darah Murni 🧪")
        )
    }

    val ALL_DUNGEONS: List<HuntingDungeon> by lazy {
        listOf(
            buildMossyForestDungeon(),
            buildCrystalCaveDungeon(),
            buildVolcanoDragonDungeon()
        )
    }
}
