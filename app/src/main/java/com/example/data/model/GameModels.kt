package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ItemRarity(val label: String, val colorHex: Long) {
    COMMON("Common", 0xFF9E9E9E),
    UNCOMMON("Uncommon", 0xFF4CAF50),
    RARE("Rare", 0xFF2196F3),
    EPIC("Epic", 0xFFAB47BC),
    LEGENDARY("Legendary", 0xFFFFA726),
    MYTHIC("Mythic", 0xFFFF1744),
    SECRET("Secret", 0xFF9C27B0),
    GOD("God", 0xFFFFD700);

    fun composeColor(): Color = Color(colorHex)
}

enum class ItemType {
    FISH, CROP, SEED, BAIT, MATERIAL, POTION, FOOD, TOOL, CHEST, PET_FOOD, BOOSTER, SPECIAL
}

data class GameItemDef(
    val id: String,
    val name: String,
    val itemType: ItemType,
    val rarity: ItemRarity,
    val iconEmoji: String,
    val description: String,
    val buyPrice: Int? = null,
    val sellPrice: Int = 10,
    val isUsable: Boolean = false,
    val energyRestored: Int = 0,
    val hpRestored: Int = 0,
    val petExpGranted: Int = 0,
    val playerExpGranted: Int = 0
)

data class CropDef(
    val id: String,
    val name: String,
    val seedName: String,
    val growDurationSeconds: Long,
    val expReward: Int,
    val sellPrice: Int,
    val seedCost: Int,
    val iconEmoji: String,
    val rarity: ItemRarity
)

data class FishDef(
    val id: String,
    val name: String,
    val rarity: ItemRarity,
    val minWeightKg: Float,
    val maxWeightKg: Float,
    val basePrice: Int,
    val expReward: Int,
    val requiredRodLevel: Int,
    val iconEmoji: String,
    val lore: String
)

data class FishingRodTier(
    val level: Int,
    val name: String,
    val catchSpeedMultiplier: Float,
    val rareBonusPercent: Int,
    val upgradeCostGold: Int,
    val upgradeCostStones: Int,
    val description: String
)

data class BaitDef(
    val id: String,
    val name: String,
    val costGold: Int,
    val rareBonus: Int,
    val iconEmoji: String,
    val description: String
)

data class PetSpeciesDef(
    val id: String,
    val baseName: String,
    val stage2Name: String,
    val stage3Name: String,
    val baseAttack: Int,
    val baseDefense: Int,
    val attackGrowth: Float,
    val defenseGrowth: Float,
    val specialty: String,
    val iconEmojiStage1: String,
    val iconEmojiStage2: String,
    val iconEmojiStage3: String,
    val description: String
)

data class MonsterDef(
    val id: String,
    val name: String,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val expDrop: Int,
    val goldDrop: Int,
    val iconEmoji: String,
    val dropStoneChance: Float
)

data class DungeonZoneDef(
    val id: String,
    val name: String,
    val levelReq: Int,
    val energyCost: Int,
    val description: String,
    val monsters: List<MonsterDef>
)

data class CookingRecipe(
    val id: String,
    val name: String,
    val requiredCropId: String,
    val requiredCropCount: Int,
    val requiredFishId: String,
    val requiredFishCount: Int,
    val energyRestored: Int,
    val hpRestored: Int,
    val iconEmoji: String,
    val description: String
)

object GameDatabaseRegistry {
    val CROPS = listOf(
        CropDef("gandum", "Gandum", "Benih Gandum", 8L, 10, 12, 20, "🌾", ItemRarity.COMMON),
        CropDef("wortel", "Wortel", "Benih Wortel", 12L, 15, 15, 30, "🥕", ItemRarity.COMMON),
        CropDef("bayam", "Bayam", "Benih Bayam", 8L, 10, 10, 20, "🥬", ItemRarity.COMMON),
        CropDef("jagung", "Jagung", "Benih Jagung", 16L, 17, 15, 35, "🌽", ItemRarity.COMMON),
        CropDef("kentang", "Kentang", "Benih Kentang", 12L, 12, 12, 25, "🥔", ItemRarity.COMMON),
        CropDef("bawang_putih", "Bawang Putih", "Benih Bawang Putih", 16L, 15, 15, 30, "🧄", ItemRarity.COMMON),
        CropDef("tomat", "Tomat", "Benih Tomat", 32L, 35, 25, 70, "🍅", ItemRarity.UNCOMMON),
        CropDef("cabai", "Cabai", "Benih Cabai", 32L, 30, 20, 60, "🌶️", ItemRarity.UNCOMMON),
        CropDef("paprika", "Paprika", "Benih Paprika", 48L, 40, 35, 80, "🫑", ItemRarity.UNCOMMON),
        CropDef("strawberry", "Strawberry", "Benih Strawberry", 72L, 50, 40, 100, "🍓", ItemRarity.UNCOMMON),
        CropDef("bawang_merah", "Bawang Merah", "Benih Bawang Merah", 40L, 30, 20, 60, "🧅", ItemRarity.UNCOMMON),
        CropDef("terong", "Terong", "Benih Terong", 48L, 37, 30, 75, "🍆", ItemRarity.UNCOMMON),
        CropDef("anggur", "Anggur", "Benih Anggur", 80L, 100, 55, 200, "🍇", ItemRarity.RARE),
        CropDef("semangka", "Semangka", "Benih Semangka", 120L, 125, 130, 250, "🍉", ItemRarity.RARE),
        CropDef("kopi", "Kopi", "Benih Kopi", 120L, 140, 80, 280, "☕", ItemRarity.RARE),
        CropDef("kakao", "Kakao", "Benih Kakao", 140L, 125, 70, 250, "🍫", ItemRarity.RARE),
        CropDef("blueberry", "Blueberry", "Benih Blueberry", 80L, 100, 60, 200, "🫐", ItemRarity.RARE),
        CropDef("mawar", "Mawar", "Benih Mawar", 140L, 175, 130, 350, "🌹", ItemRarity.RARE),
        CropDef("bunga_matahari", "Bunga Matahari", "Benih Bunga Matahari", 200L, 300, 130, 600, "🌻", ItemRarity.EPIC),
        CropDef("jeruk", "Jeruk", "Benih Jeruk", 280L, 350, 200, 700, "🍊", ItemRarity.EPIC),
        CropDef("zaitun", "Zaitun", "Benih Zaitun", 360L, 450, 350, 900, "🫒", ItemRarity.EPIC),
        CropDef("sakura", "Sakura", "Benih Sakura", 280L, 550, 400, 1100, "🌸", ItemRarity.EPIC),
        CropDef("madu", "Madu", "Benih Madu", 200L, 350, 230, 700, "🍯", ItemRarity.EPIC),
        CropDef("hibiscus", "Hibiscus", "Benih Hibiscus", 200L, 325, 170, 650, "🌺", ItemRarity.EPIC),
        CropDef("crystal_flower", "Crystal Flower", "Benih Crystal Flower", 600L, 1750, 1400, 3500, "💠", ItemRarity.LEGENDARY),
        CropDef("star_fruit", "Star Fruit", "Benih Star Fruit", 600L, 1500, 1100, 3000, "⭐", ItemRarity.LEGENDARY),
        CropDef("mystic_herb", "Mystic Herb", "Benih Mystic Herb", 720L, 2250, 1800, 4500, "🌿", ItemRarity.LEGENDARY),
        CropDef("dragon_fruit_crop", "Dragon Fruit", "Benih Dragon Fruit", 660L, 1750, 1400, 3500, "🐉", ItemRarity.LEGENDARY),
        CropDef("lotus", "Lotus Suci", "Benih Lotus Suci", 840L, 2500, 2200, 5000, "🪷", ItemRarity.LEGENDARY),
        CropDef("ice_berry", "Ice Berry", "Benih Ice Berry", 600L, 1600, 1300, 3200, "❄️", ItemRarity.LEGENDARY)
    )

    val FISHES = listOf(
        FishDef("fish_lele", "Ikan Lele Rawa", ItemRarity.COMMON, 0.8f, 2.5f, 30, 10, 0, "🐟", "Ikan berkumis lincah penghuni kolam lumpur."),
        FishDef("fish_mas", "Ikan Mas Kolam", ItemRarity.COMMON, 1.0f, 3.2f, 40, 14, 0, "🐠", "Sisiknya berkilau oranye cerah."),
        FishDef("fish_mujair", "Ikan Mujair Liar", ItemRarity.COMMON, 0.5f, 1.8f, 32, 12, 0, "🐟", "Sangat mudah dipancing di aliran air tawar."),
        FishDef("fish_nila", "Ikan Nila Danau", ItemRarity.COMMON, 0.7f, 2.2f, 38, 15, 0, "🐟", "Ikan air tenang yang sering bergerombol."),

        FishDef("fish_gurame", "Gurame Madu", ItemRarity.UNCOMMON, 1.5f, 4.5f, 85, 28, 1, "🐠", "Dagingnya lezat dan disukai penduduk kota."),
        FishDef("fish_salmon", "Salmon Arus Deras", ItemRarity.UNCOMMON, 2.0f, 6.0f, 110, 35, 1, "🐟", "Melompat melawan arus air terjun."),
        FishDef("fish_squid", "Cumi-Cumi Bintang", ItemRarity.UNCOMMON, 1.0f, 3.5f, 95, 30, 1, "🦑", "Menyemprotkan tinta bercahaya di malam hari."),
        FishDef("fish_eel", "Belut Listrik Muara", ItemRarity.UNCOMMON, 1.2f, 3.8f, 100, 32, 1, "🐍", "Belut lincah beraliran listrik halus penembus rawa."),

        FishDef("fish_tuna", "Tuna Sirip Biru", ItemRarity.RARE, 8.0f, 25.0f, 240, 65, 2, "🐟", "Perenang samudra berkecepatan kilat."),
        FishDef("fish_stingray", "Ikan Pari Elektrik", ItemRarity.RARE, 5.0f, 18.0f, 220, 60, 2, "🐡", "Bisa mengeluarkan sengatan listrik halus."),
        FishDef("fish_crab", "Kepiting Kristal Biru", ItemRarity.RARE, 2.0f, 6.5f, 210, 58, 2, "🦀", "Capitnya dilapisi kristal laut karang yang keras."),
        FishDef("fish_lobster", "Lobster Karang Emas", ItemRarity.RARE, 3.0f, 8.0f, 260, 70, 2, "🦞", "Sangat langka dan bernilai jual tinggi di restoran kota."),

        FishDef("fish_arwana", "Arwana Emas Super", ItemRarity.EPIC, 4.0f, 12.0f, 550, 130, 4, "🐲", "Simbol kemakmuran mitologi kuno."),
        FishDef("fish_hammerhead", "Hiu Martil Bayi", ItemRarity.EPIC, 15.0f, 45.0f, 620, 150, 4, "🦈", "Bentuk kepalanya unik dan tenaganya sangat kuat."),
        FishDef("fish_swordfish", "Ikan Pedang Samudra", ItemRarity.EPIC, 25.0f, 75.0f, 580, 140, 4, "🗡️", "Moncong runcingnya mampu membelah gelombang badai."),

        FishDef("fish_coelacanth", "Coelacanth Purba", ItemRarity.LEGENDARY, 20.0f, 60.0f, 1200, 300, 6, "🦖", "Fosil hidup dari zaman pra-sejarah laut dalam."),
        FishDef("fish_golden_koi", "Koi Kaisar Giok", ItemRarity.LEGENDARY, 12.0f, 30.0f, 1400, 350, 6, "✨", "Konon bisa berubah menjadi naga jika melompati gerbang air."),
        FishDef("fish_golden_whale", "Paus Emas Atlantis", ItemRarity.LEGENDARY, 150.0f, 400.0f, 1600, 420, 6, "🐋", "Makhluk mitos raksasa penjaga kuil samudra."),
        FishDef("fish_angel", "Ikan Bidadari Cahaya", ItemRarity.LEGENDARY, 5.0f, 15.0f, 1750, 450, 6, "✨🐠", "Siripnya memancarkan aura suci penyembuh jiwa."),

        FishDef("fish_kraken", "Kraken Palung Gelap", ItemRarity.MYTHIC, 500.0f, 1500.0f, 2900, 800, 8, "🦑", "Legenda penguasa palung samudra terdalam."),
        FishDef("fish_leviathan", "Leviathan Air Dalam", ItemRarity.MYTHIC, 80.0f, 250.0f, 3500, 950, 8, "🌌", "Penjaga samudra mitologi dengan sisik kristal permata.")
    )

    val ROD_TIERS = listOf(
        FishingRodTier(0, "Joran Bambu", 1.00f, 0, 0, 0, "CD 12s • Rare +0%"),
        FishingRodTier(1, "Joran Fiber", 1.12f, 1500, 0, 2, "CD 10s • Rare +3%"),
        FishingRodTier(2, "Joran Carbon", 1.24f, 5000, 0, 4, "CD 9s • Rare +6%"),
        FishingRodTier(3, "Joran Titanium", 1.36f, 15000, 0, 6, "CD 8s • Rare +9%"),
        FishingRodTier(4, "Joran Pro", 1.48f, 40000, 0, 8, "CD 7s • Rare +12%"),
        FishingRodTier(5, "Joran Enchanted", 1.64f, 80000, 0, 10, "CD 6s • Rare +16%"),
        FishingRodTier(6, "Joran Mythic", 1.80f, 150000, 0, 12, "CD 5s • Rare +20%"),
        FishingRodTier(7, "Joran Celestial", 1.96f, 300000, 0, 14, "CD 4s • Rare +24%"),
        FishingRodTier(8, "Joran Divine", 2.12f, 500000, 0, 16, "CD 3s • Rare +28%"),
        FishingRodTier(9, "Joran Void", 2.28f, 1000000, 0, 18, "CD 3s • Rare +32%"),
        FishingRodTier(10, "Joran Astral", 2.52f, 2000000, 0, 20, "CD 2s • Rare +38%"),
        FishingRodTier(11, "Joran Godslayer", 2.80f, 5000000, 0, 22, "CD 2s • Rare +45%"),
        FishingRodTier(12, "Joran Omega", 3.20f, 10000000, 0, 24, "CD 1s • Rare +55%")
    )

    val BAITS = listOf(
        BaitDef("none", "Tanpa Umpan", 0, 0, "❌", "Bonus +0%"),
        BaitDef("cacing", "Cacing", 100, 2, "🪱", "Bonus +2%"),
        BaitDef("jangkrik", "Jangkrik", 200, 4, "🦗", "Bonus +4%"),
        BaitDef("udang", "Udang Kecil", 500, 6, "🦐", "Bonus +6%"),
        BaitDef("ikan_kecil", "Ikan Kecil", 800, 8, "🐟", "Bonus +8%"),
        BaitDef("cumi", "Cumi", 1200, 10, "🦑", "Bonus +10%"),
        BaitDef("golden_worm", "Golden Worm", 3000, 15, "✨", "Bonus +15%"),
        BaitDef("mystic_bait", "Mystic Bait", 8000, 22, "🔮", "Bonus +22%"),
        BaitDef("void_lure", "Void Lure", 20000, 30, "🕳️", "Bonus +30%"),
        BaitDef("celestial_bait", "Celestial Bait", 50000, 38, "🌙", "Bonus +38%"),
        BaitDef("divine_essence", "Divine Essence", 100000, 45, "✝️", "Bonus +45%"),
        BaitDef("god_lure", "God Lure", 250000, 55, "👁️‍🗨️", "Bonus +55%"),
        BaitDef("prism_lure", "Prism Lure", 150000, 50, "💎", "Bonus +50%"),
        BaitDef("omega_bait", "Omega Bait", 400000, 60, "🔱", "Bonus +60%"),
        BaitDef("trophy_chum", "Trophy Chum", 75000, 28, "🏆", "Bonus +28%")
    )

    val PET_SPECIES = listOf(
        PetSpeciesDef(
            id = "pet_wolf",
            baseName = "Serigala Salju",
            stage2Name = "Dire Wolf Es",
            stage3Name = "Fenrir Abadi",
            baseAttack = 18,
            baseDefense = 10,
            attackGrowth = 4.2f,
            defenseGrowth = 2.0f,
            specialty = "Spesialis Serangan Fisik Tinggi",
            iconEmojiStage1 = "🐺",
            iconEmojiStage2 = "❄️🐺",
            iconEmojiStage3 = "👑🐺",
            description = "Sahabat setia dari pegunungan beku dengan taring tajam."
        ),
        PetSpeciesDef(
            id = "pet_dragon",
            baseName = "Bayi Naga Api",
            stage2Name = "Naga Merah Berkobar",
            stage3Name = "Ignis Dragon Lord",
            baseAttack = 22,
            baseDefense = 8,
            attackGrowth = 5.0f,
            defenseGrowth = 1.8f,
            specialty = "Burst Damage & Semburan Api",
            iconEmojiStage1 = "🦎",
            iconEmojiStage2 = "🐲",
            iconEmojiStage3 = "🔥🐉",
            description = "Naga kecil pemarah yang menyemburkan api hangat."
        ),
        PetSpeciesDef(
            id = "pet_turtle",
            baseName = "Kura-kura Kristal",
            stage2Name = "Kura Giok Perkasa",
            stage3Name = "Genbu Benteng Alam",
            baseAttack = 8,
            baseDefense = 22,
            attackGrowth = 1.8f,
            defenseGrowth = 4.8f,
            specialty = "Pertahanan Tebal & Perlindungan HP",
            iconEmojiStage1 = "🐢",
            iconEmojiStage2 = "🛡️🐢",
            iconEmojiStage3 = "💎🐢",
            description = "Memiliki tempurung sekeras batu safir kuno."
        ),
        PetSpeciesDef(
            id = "pet_bird",
            baseName = "Burung Sprout",
            stage2Name = "Firebird Zamrud",
            stage3Name = "Phoenix Abadi",
            baseAttack = 14,
            baseDefense = 12,
            attackGrowth = 3.0f,
            defenseGrowth = 2.8f,
            specialty = "Regenerasi Stamina & Bonus EXP",
            iconEmojiStage1 = "🐣",
            iconEmojiStage2 = "🦅",
            iconEmojiStage3 = "✨🕊️",
            description = "Burung anggun yang mengepakkan bulu hangat penyembuh."
        ),
        PetSpeciesDef(
            id = "pet_cat",
            baseName = "Kucing Rimba",
            stage2Name = "Macan Bayangan",
            stage3Name = "Raja Rimba Emas",
            baseAttack = 16,
            baseDefense = 11,
            attackGrowth = 3.8f,
            defenseGrowth = 2.2f,
            specialty = "Kelincahan & Critical Strike",
            iconEmojiStage1 = "🐱",
            iconEmojiStage2 = "🐆",
            iconEmojiStage3 = "🐯",
            description = "Pemburu lincah yang menyukai ikan lezat."
        ),
        PetSpeciesDef(
            id = "pet_slime",
            baseName = "Slime Ajaib",
            stage2Name = "King Slime Kenyal",
            stage3Name = "Slime Emperor",
            baseAttack = 12,
            baseDefense = 14,
            attackGrowth = 2.8f,
            defenseGrowth = 3.2f,
            specialty = "Bonus Gold & Drop Loot Langka",
            iconEmojiStage1 = "💧",
            iconEmojiStage2 = "👑💧",
            iconEmojiStage3 = "🌈💧",
            description = "Gumpalan jeli manis berdaya magis tak terduga."
        ),
        PetSpeciesDef(
            id = "pet_kitsune",
            baseName = "Rubah Mistis",
            stage2Name = "Kitsune Taring Biru",
            stage3Name = "Kyuubi Amaterasu",
            baseAttack = 20,
            baseDefense = 10,
            attackGrowth = 4.5f,
            defenseGrowth = 2.5f,
            specialty = "Sihir Api Roh & Kerusakan Magic Tinggi",
            iconEmojiStage1 = "🦊",
            iconEmojiStage2 = "🔥🦊",
            iconEmojiStage3 = "👑🦊",
            description = "Roh rubah penjaga kuil sakral pegunungan berkekuatan api suci."
        ),
        PetSpeciesDef(
            id = "pet_griffin",
            baseName = "Bayi Griffin",
            stage2Name = "Griffin Sayap Baja",
            stage3Name = "Sky Sovereign Griffin",
            baseAttack = 19,
            baseDefense = 16,
            attackGrowth = 3.8f,
            defenseGrowth = 3.5f,
            specialty = "Pertahanan Langit & Serangan Badai Seimbang",
            iconEmojiStage1 = "🦅",
            iconEmojiStage2 = "⚔️🦅",
            iconEmojiStage3 = "👑🦅",
            description = "Singa bersayap rajawali dengan pekikan pembelah awan."
        )
    )

    val DUNGEONS = listOf(
        DungeonZoneDef(
            id = "zone_forest",
            name = "🌿 Hutan Pemula",
            levelReq = 1,
            energyCost = 5,
            description = "Hutan tenang pemula — drop Mystery Box & DNA Shard.",
            monsters = listOf(
                MonsterDef("m_slime", "Slime Hijau", hp = 70, attack = 12, defense = 3, expDrop = 25, goldDrop = 40, iconEmoji = "🟢", dropStoneChance = 0.20f),
                MonsterDef("m_boar", "Babi Hutan Ganas", hp = 95, attack = 15, defense = 5, expDrop = 45, goldDrop = 70, iconEmoji = "🐗", dropStoneChance = 0.30f),
                MonsterDef("m_goblin", "Goblin Pencuri", hp = 135, attack = 22, defense = 8, expDrop = 70, goldDrop = 110, iconEmoji = "👺", dropStoneChance = 0.40f)
            )
        ),
        DungeonZoneDef(
            id = "zone_cave",
            name = "🏔️ Gua Batu",
            levelReq = 10,
            energyCost = 10,
            description = "Gua berbatu gelap — drop Refine Stone (40%), Mystery Box & DNA Shard.",
            monsters = listOf(
                MonsterDef("m_bat", "Kelelawar Gua", hp = 140, attack = 22, defense = 10, expDrop = 100, goldDrop = 150, iconEmoji = "🦇", dropStoneChance = 0.40f),
                MonsterDef("m_spider", "Laba-Laba Gua", hp = 180, attack = 26, defense = 14, expDrop = 140, goldDrop = 210, iconEmoji = "🕷️", dropStoneChance = 0.45f),
                MonsterDef("m_golem", "Golem Batu Besi", hp = 280, attack = 40, defense = 22, expDrop = 220, goldDrop = 320, iconEmoji = "🗿", dropStoneChance = 0.60f)
            )
        ),
        DungeonZoneDef(
            id = "zone_volcano",
            name = "🌋 Gunung Api",
            levelReq = 25,
            energyCost = 15,
            description = "Lembah magma mendidih — drop Refine Stone (50%), Protection Stone & Mutation Serum.",
            monsters = listOf(
                MonsterDef("m_hound", "Hellhound Api", hp = 280, attack = 36, defense = 20, expDrop = 320, goldDrop = 450, iconEmoji = "🐕‍🦺", dropStoneChance = 0.50f),
                MonsterDef("m_wyvern", "Wyvern Magma", hp = 420, attack = 50, defense = 28, expDrop = 460, goldDrop = 680, iconEmoji = "🦅", dropStoneChance = 0.65f),
                MonsterDef("m_boss_dragon", "Dragon Lord Ignis", hp = 630, attack = 72, defense = 38, expDrop = 800, goldDrop = 1200, iconEmoji = "🔥🐲", dropStoneChance = 1.0f)
            )
        ),
        DungeonZoneDef(
            id = "zone_castle",
            name = "🏰 Kastil Gelap",
            levelReq = 50,
            energyCost = 20,
            description = "Benteng kegelapan — drop Refine Stone (60%), Protection Stone (25%) & Lucky Charm.",
            monsters = listOf(
                MonsterDef("m_knight", "Ksatria Bayangan", hp = 560, attack = 58, defense = 35, expDrop = 900, goldDrop = 1600, iconEmoji = "🤺", dropStoneChance = 0.60f),
                MonsterDef("m_demon_guard", "Prajurit Iblis", hp = 840, attack = 80, defense = 48, expDrop = 1400, goldDrop = 2400, iconEmoji = "👿", dropStoneChance = 0.75f),
                MonsterDef("m_boss_demon", "Demon King", hp = 1400, attack = 122, defense = 70, expDrop = 2600, goldDrop = 4500, iconEmoji = "👹", dropStoneChance = 1.0f)
            )
        ),
        DungeonZoneDef(
            id = "zone_void",
            name = "🌌 Void Realm",
            levelReq = 100,
            energyCost = 30,
            description = "Dimensi kosong — drop Refine Stone (70%), Protection Stone (35%), Mythic Fragment & Ancient Core.",
            monsters = listOf(
                MonsterDef("m_void_beast", "Binatang Void", hp = 1400, attack = 115, defense = 75, expDrop = 3500, goldDrop = 6000, iconEmoji = "👾", dropStoneChance = 0.70f),
                MonsterDef("m_void_emperor", "Void Emperor", hp = 2500, attack = 175, defense = 110, expDrop = 7000, goldDrop = 12000, iconEmoji = "🌑", dropStoneChance = 0.90f),
                MonsterDef("m_boss_omega", "🔱 Omega Genesis", hp = 3500, attack = 215, defense = 140, expDrop = 15000, goldDrop = 25000, iconEmoji = "🔱", dropStoneChance = 1.0f)
            )
        )
    )

    val COOKING_RECIPES = listOf(
        CookingRecipe(
            id = "cooked_pancake",
            name = "Cooked Pancake",
            requiredCropId = "gandum",
            requiredCropCount = 2,
            requiredFishId = "kentang",
            requiredFishCount = 1,
            energyRestored = 50,
            hpRestored = 100,
            iconEmoji = "🥞",
            description = "Pulihkan 100% laper & seneng pet."
        ),
        CookingRecipe(
            id = "spicy_fish_soup",
            name = "Spicy Fish Soup",
            requiredCropId = "cabai",
            requiredCropCount = 2,
            requiredFishId = "fish_lele",
            requiredFishCount = 1,
            energyRestored = 40,
            hpRestored = 75,
            iconEmoji = "🍜",
            description = "Buff pet ATK +10% selama 1 jam."
        ),
        CookingRecipe(
            id = "veggie_salad",
            name = "Veggie Salad",
            requiredCropId = "wortel",
            requiredCropCount = 3,
            requiredFishId = "kentang",
            requiredFishCount = 2,
            energyRestored = 35,
            hpRestored = 60,
            iconEmoji = "🥗",
            description = "Lindungi farm dari hama selama 6 jam."
        ),
        CookingRecipe(
            id = "grilled_fish",
            name = "Grilled Fish",
            requiredCropId = "cabai",
            requiredCropCount = 2,
            requiredFishId = "fish_mas",
            requiredFishCount = 2,
            energyRestored = 45,
            hpRestored = 90,
            iconEmoji = "🐟",
            description = "Buff mancing: +15% Rare Fish chance (1 jam)."
        ),
        CookingRecipe(
            id = "sushi_roll",
            name = "Sushi Roll",
            requiredCropId = "gandum",
            requiredCropCount = 3,
            requiredFishId = "fish_tuna",
            requiredFishCount = 1,
            energyRestored = 70,
            hpRestored = 150,
            iconEmoji = "🍣",
            description = "Buff pet: ATK & DEF +15% (1 jam)."
        ),
        CookingRecipe(
            id = "seafood_paella",
            name = "Seafood Paella",
            requiredCropId = "tomat",
            requiredCropCount = 2,
            requiredFishId = "fish_salmon",
            requiredFishCount = 1,
            energyRestored = 80,
            hpRestored = 180,
            iconEmoji = "🍛",
            description = "Buff booster: +50% money magnet (1 jam)."
        ),
        CookingRecipe(
            id = "abyssal_stew",
            name = "Abyssal Stew",
            requiredCropId = "mystic_herb",
            requiredCropCount = 1,
            requiredFishId = "fish_kraken",
            requiredFishCount = 1,
            energyRestored = 100,
            hpRestored = 300,
            iconEmoji = "🥣",
            description = "Booster: Double Player & Pet XP (1 jam)."
        ),
        CookingRecipe(
            id = "fisherman_feast",
            name = "Fisherman's Feast",
            requiredCropId = "kentang",
            requiredCropCount = 3,
            requiredFishId = "fish_eel",
            requiredFishCount = 2,
            energyRestored = 65,
            hpRestored = 130,
            iconEmoji = "🍤",
            description = "Buff mancing: Cooldown -3s (1 jam)."
        )
    )

    // Complete BOT item registry matching https://github.com/adit4rt-source/bot
    val BOT_ITEM_CATALOG = listOf(
        // 1. Battle & Upgrade Materials (Drop-Only)
        GameItemDef("refine_stone", "Refine Stone", ItemType.MATERIAL, ItemRarity.RARE, "🪨", "Material upgrade relic (+1) — HANYA dari dungeon/boss/expedition/hunt", buyPrice = null, sellPrice = 10),
        GameItemDef("protection_stone", "Protection Stone", ItemType.MATERIAL, ItemRarity.RARE, "🛡️", "Refine gagal tidak turun level — HANYA dari dungeon/boss/expedition", buyPrice = null, sellPrice = 10),
        GameItemDef("rod_part", "Rod Parts", ItemType.TOOL, ItemRarity.UNCOMMON, "🔧", "Material upgrade joran — HANYA dari mancing/expedition/hunt", buyPrice = null, sellPrice = 10),
        GameItemDef("mythic_fragment", "Mythic Fragment", ItemType.MATERIAL, ItemRarity.LEGENDARY, "🌟", "Material langka Awakening (World Boss/Expedition/Craft)", buyPrice = null, sellPrice = 25),
        GameItemDef("awakening_crystal", "Awakening Crystal", ItemType.MATERIAL, ItemRarity.LEGENDARY, "💫", "Material ultra-langka Awakening (World Boss #1 / Craft)", buyPrice = null, sellPrice = 50),
        GameItemDef("omega_core", "Omega Core", ItemType.SPECIAL, ItemRarity.MYTHIC, "🔱", "Material legendaris Awakening Cosmic — Hanya dari Boss Omega Genesis", buyPrice = null, sellPrice = 100),
        GameItemDef("nightmare_token", "Nightmare Token", ItemType.MATERIAL, ItemRarity.RARE, "🌑", "Currency Nightmare Dungeon — tukar di Nightmare Shop", buyPrice = null, sellPrice = 10),
        GameItemDef("skill_tome", "Skill Tome", ItemType.MATERIAL, ItemRarity.EPIC, "📖", "Reroll 1 battle skill pet (pilih tier). Dari Nightmare Shop", buyPrice = null, sellPrice = 20),

        // 2. Pet Mutation Lab Materials (Drop-Only)
        GameItemDef("dna_shard", "DNA Shard", ItemType.MATERIAL, ItemRarity.RARE, "🧬", "Material dasar Mutation Lab — drop dari dungeon/boss/co-op", buyPrice = null, sellPrice = 15),
        GameItemDef("mutation_serum", "Mutation Serum", ItemType.MATERIAL, ItemRarity.EPIC, "🧪", "Katalis mutation pet — drop dari dungeon co-op dan boss", buyPrice = null, sellPrice = 30),
        GameItemDef("ancient_core", "Ancient Core", ItemType.MATERIAL, ItemRarity.LEGENDARY, "🔮", "Material langka untuk mutasi pet tier tinggi", buyPrice = null, sellPrice = 50),
        GameItemDef("trait_stabilizer", "Trait Stabilizer", ItemType.MATERIAL, ItemRarity.EPIC, "🧯", "+15% peluang Mutation Lab sekali pakai", buyPrice = null, sellPrice = 25),

        // 3. Booster & Protection Items
        GameItemDef("xp_booster_2x", "XP Booster 2x", ItemType.BOOSTER, ItemRarity.RARE, "⚡", "Double XP sementara (1 jam)", buyPrice = 6000, sellPrice = 3000, isUsable = true),
        GameItemDef("xp_booster_3x", "XP Booster 3x", ItemType.BOOSTER, ItemRarity.EPIC, "⚡", "Triple XP sementara (1 jam)", buyPrice = 15000, sellPrice = 7500, isUsable = true),
        GameItemDef("streak_shield", "Streak Shield", ItemType.MATERIAL, ItemRarity.UNCOMMON, "🛡️", "OTOMATIS lindungi streak jika skip 1 hari", buyPrice = 12000, sellPrice = 6000, isUsable = true),
        GameItemDef("lucky_charm", "Lucky Charm", ItemType.MATERIAL, ItemRarity.RARE, "🍀", "+15% chance menang semua game", buyPrice = 25000, sellPrice = 12500, isUsable = true),
        GameItemDef("money_magnet", "Money Magnet", ItemType.BOOSTER, ItemRarity.EPIC, "🧲", "+50% money dari semua sumber (1 jam)", buyPrice = 20000, sellPrice = 10000, isUsable = true),
        GameItemDef("daily_doubler", "Daily Doubler", ItemType.SPECIAL, ItemRarity.UNCOMMON, "📅", "Gandakan /daily reward (sekali pakai)", buyPrice = 5000, sellPrice = 2500, isUsable = true),
        GameItemDef("tax_free_voucher", "Tax-Free Voucher", ItemType.SPECIAL, ItemRarity.COMMON, "🎫", "Gift tanpa pajak (sekali pakai)", buyPrice = 4000, sellPrice = 2000, isUsable = true),
        GameItemDef("lucky_spin_token", "Lucky Spin Token", ItemType.SPECIAL, ItemRarity.UNCOMMON, "🎰", "Jamin 2 simbol sama di slot (sekali pakai)", buyPrice = 10000, sellPrice = 5000, isUsable = true),
        GameItemDef("mystery_box", "Mystery Box", ItemType.CHEST, ItemRarity.COMMON, "📦", "Random 50-2000 money", buyPrice = 2000, sellPrice = 1000, isUsable = true),
        GameItemDef("auto_harvest_pass", "Auto-Harvest Pass", ItemType.TOOL, ItemRarity.EPIC, "🔔", "Aktifkan notifikasi panen otomatis (permanen)", buyPrice = 15000, sellPrice = 7500),

        // 4. Fishing Protection & Anti-Monster
        GameItemDef("monster_repellent", "Monster Repellent", ItemType.TOOL, ItemRarity.RARE, "🧪", "Kurangi monster chance -50% selama 5 cast", buyPrice = 20000, sellPrice = 10000, isUsable = true),
        GameItemDef("shield_charm", "Shield Charm", ItemType.TOOL, ItemRarity.UNCOMMON, "🛡️", "Block 1 serangan monster (otomatis, habis pakai)", buyPrice = 12000, sellPrice = 6000, isUsable = true),
        GameItemDef("thunder_coating", "Thunder Rod Coating", ItemType.TOOL, ItemRarity.EPIC, "⚡", "Monster langsung kabur + DROP loot! (3 cast)", buyPrice = 50000, sellPrice = 25000, isUsable = true),

        // 5. Farming & Livestock Items
        GameItemDef("pesticide", "Pestisida", ItemType.MATERIAL, ItemRarity.COMMON, "🧴", "Basmi 1 hama di 1 plot (sekali pakai)", buyPrice = 2000, sellPrice = 1000, isUsable = true),
        GameItemDef("pesticide_shield", "Pestisida Shield", ItemType.MATERIAL, ItemRarity.UNCOMMON, "🌿", "Preventif — lindungi farm dari hama selama 6 jam (sekali pakai)", buyPrice = 8000, sellPrice = 4000, isUsable = true),
        GameItemDef("premium_feed", "Pakan Premium", ItemType.MATERIAL, ItemRarity.UNCOMMON, "⭐", "Material untuk evolve hewan ternak", buyPrice = 1200, sellPrice = 600, isUsable = true),

        // 6. Cooked Food & Potions
        GameItemDef("cooked_pancake", "Cooked Pancake", ItemType.FOOD, ItemRarity.COMMON, "🥞", "Pulihkan 100% laper & seneng pet", buyPrice = null, sellPrice = 15, isUsable = true),
        GameItemDef("spicy_fish_soup", "Spicy Fish Soup", ItemType.FOOD, ItemRarity.UNCOMMON, "🍜", "Buff pet ATK +10% selama 1 jam", buyPrice = null, sellPrice = 25, isUsable = true),
        GameItemDef("veggie_salad", "Veggie Salad", ItemType.FOOD, ItemRarity.UNCOMMON, "🥗", "Lindungi farm dari hama selama 6 jam", buyPrice = null, sellPrice = 20, isUsable = true),
        GameItemDef("grilled_fish", "Grilled Fish", ItemType.FOOD, ItemRarity.UNCOMMON, "🐟", "Buff mancing: +15% Rare Fish chance (1 jam)", buyPrice = null, sellPrice = 30, isUsable = true),
        GameItemDef("sushi_roll", "Sushi Roll", ItemType.FOOD, ItemRarity.RARE, "🍣", "Buff pet: ATK & DEF +15% (1 jam)", buyPrice = null, sellPrice = 40, isUsable = true),
        GameItemDef("seafood_paella", "Seafood Paella", ItemType.FOOD, ItemRarity.RARE, "🍛", "Buff booster: +50% money magnet (1 jam)", buyPrice = null, sellPrice = 50, isUsable = true),
        GameItemDef("abyssal_stew", "Abyssal Stew", ItemType.FOOD, ItemRarity.EPIC, "🥣", "Booster: Double Player & Pet XP (1 jam)", buyPrice = null, sellPrice = 80, isUsable = true),
        GameItemDef("fisherman_feast", "Fisherman's Feast", ItemType.FOOD, ItemRarity.RARE, "🍤", "Buff mancing: Cooldown -3s (1 jam)", buyPrice = null, sellPrice = 45, isUsable = true),

        // Legacy / Classic Potions & Stones
        GameItemDef("item_upgrade_stone", "Batu Peningkat Tempa", ItemType.MATERIAL, ItemRarity.RARE, "🪨", "Batu tempa peningkat perlengkapan petualang.", buyPrice = 100, sellPrice = 50),
        GameItemDef("item_potion_hp", "Ramuan Darah Standar", ItemType.POTION, ItemRarity.UNCOMMON, "🧪", "Memulihkan +70 HP seketika saat bertarung.", buyPrice = 50, sellPrice = 25, isUsable = true, hpRestored = 70),
        GameItemDef("item_potion_stamina", "Ramuan Stamina Kilat", ItemType.POTION, ItemRarity.UNCOMMON, "⚡", "Memulihkan +25 Stamina petualangan.", buyPrice = 75, sellPrice = 35, isUsable = true, energyRestored = 25),
        GameItemDef("item_fertilizer", "Pupuk Organik", ItemType.MATERIAL, ItemRarity.COMMON, "✨", "Pupuk penyubur tanaman kebun.", buyPrice = 25, sellPrice = 12, isUsable = true)
    )
}
