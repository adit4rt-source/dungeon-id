package com.example.data.repository

import android.content.Context
import com.example.data.model.BaitDef
import com.example.data.model.CropDef
import com.example.data.model.FishDef
import com.example.data.model.FishingRodTier
import com.example.data.model.GameItemDef
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

typealias DataRepository = BotDataRepository

data class BotItem(
    val id: String,
    val name: String,
    val emoji: String,
    val desc: String,
    val price: Int,
    val category: String
)

data class BotIngredient(
    val id: String,
    val qty: Int
)

data class BotCraftResult(
    val type: String,
    val id: String? = null,
    val qty: Int = 1,
    val amount: Int = 0
)

data class BotCraftRecipe(
    val id: String,
    val name: String,
    val emoji: String,
    val ingredients: List<BotIngredient>,
    val result: BotCraftResult,
    val desc: String
)

data class BotCrop(
    val id: String,
    val name: String,
    val emoji: String,
    val tier: String,
    val cost: Int,
    val timeMinutes: Int,
    val minYield: Int,
    val maxYield: Int,
    val sellPrice: Int
)

data class BotFish(
    val id: String,
    val name: String,
    val tier: String,
    val emoji: String,
    val location: String
)

data class BotRod(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Int,
    val cooldown: Int,
    val rareBonus: Int,
    val tier: Int
)

data class BotBait(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Int,
    val rareBonus: Int,
    val trophyBonus: Int = 0
)

data class BotFishingLocation(
    val id: String,
    val name: String,
    val desc: String,
    val requiredRodTier: Int,
    val luckPenalty: Int,
    val bonusRare: Int,
    val tiers: List<String>,
    val monsterChance: Int,
    val isSecret: Boolean = false
)

data class BotSeaMonster(
    val id: String,
    val name: String,
    val emoji: String,
    val location: String,
    val chance: Int,
    val damage: String,
    val desc: String
)

data class BotPet(
    val id: String,
    val name: String,
    val tier: String,
    val element: String,
    val emoji: String,
    val baseHp: Int,
    val baseAtk: Int,
    val baseDef: Int,
    val bonuses: Map<String, Double> = emptyMap(),
    val desc: String = ""
)

data class BotPetEgg(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Int,
    val rates: Map<String, Int> = emptyMap()
)

data class BotLootItem(
    val itemId: String,
    val chance: Double,
    val min: Int,
    val max: Int
)

data class BotDungeon(
    val id: String,
    val name: String,
    val minLevel: Int,
    val waves: Int,
    val monsterHp: List<Int>,
    val monsterAtk: List<Int>,
    val minReward: Int,
    val maxReward: Int,
    val exp: Int,
    val cooldownMs: Long,
    val element: String,
    val penaltyCap: Int,
    val relicChance: Double,
    val loot: List<BotLootItem>
)

data class BotBoss(
    val id: String,
    val name: String,
    val minLevel: Int,
    val hp: Int,
    val atk: Int,
    val def: Int,
    val minReward: Int,
    val maxReward: Int,
    val exp: Int,
    val element: String,
    val penaltyCap: Int,
    val relicChance: Double,
    val loot: List<BotLootItem>
)

interface IBotDataRepository {
    fun getItems(): List<BotItem>
    fun getItem(id: String): BotItem?
    fun getCraftRecipes(): List<BotCraftRecipe>
    fun getCrops(): List<BotCrop>
    fun getCrop(id: String): BotCrop?
    fun getFish(): List<BotFish>
    fun getFishByLocation(location: String): List<BotFish>
    fun getRods(): List<BotRod>
    fun getBaits(): List<BotBait>
    fun getFishingLocations(): List<BotFishingLocation>
    fun getSeaMonsters(): List<BotSeaMonster>
    fun getPets(): List<BotPet>
    fun getPet(id: String): BotPet?
    fun getPetEggs(): List<BotPetEgg>
    fun getDungeons(): List<BotDungeon>
    fun getBosses(): List<BotBoss>

    // Mappers to game model abstractions
    fun mapToGameItemDefs(): List<GameItemDef>
    fun mapToCropDefs(): List<CropDef>
    fun mapToFishDefs(): List<FishDef>
    fun mapToRodTiers(): List<FishingRodTier>
    fun mapToBaits(): List<BaitDef>
}

class BotDataRepository(private val context: Context? = null) : IBotDataRepository {

    private val items = mutableListOf<BotItem>()
    private val craftRecipes = mutableListOf<BotCraftRecipe>()
    private val crops = mutableListOf<BotCrop>()
    private val fishList = mutableListOf<BotFish>()
    private val rods = mutableListOf<BotRod>()
    private val baits = mutableListOf<BotBait>()
    private val locations = mutableListOf<BotFishingLocation>()
    private val seaMonsters = mutableListOf<BotSeaMonster>()
    private val pets = mutableListOf<BotPet>()
    private val petEggs = mutableListOf<BotPetEgg>()
    private val dungeons = mutableListOf<BotDungeon>()
    private val bosses = mutableListOf<BotBoss>()

    init {
        loadData()
    }

    private fun cleanEmoji(raw: String): String {
        return if (raw.startsWith("<:") && raw.endsWith(">")) {
            // Discord custom emoji syntax <:name:id> -> provide friendly unicode fallback
            val name = raw.substringAfter("<:").substringBefore(":")
            when {
                name.contains("booster", ignoreCase = true) -> "⚡"
                name.contains("shield", ignoreCase = true) -> "🛡️"
                name.contains("stone", ignoreCase = true) -> "🪨"
                name.contains("rod", ignoreCase = true) -> "🎣"
                name.contains("charm", ignoreCase = true) -> "🍀"
                name.contains("spin", ignoreCase = true) || name.contains("token", ignoreCase = true) -> "🎰"
                name.contains("mystery", ignoreCase = true) -> "📦"
                name.contains("pesticide", ignoreCase = true) -> "🌿"
                name.contains("flower", ignoreCase = true) -> "🌸"
                name.contains("star", ignoreCase = true) -> "⭐"
                name.contains("herb", ignoreCase = true) -> "🌿"
                name.contains("berry", ignoreCase = true) -> "❄️"
                name.contains("fruit", ignoreCase = true) -> "🐉"
                name.contains("lotus", ignoreCase = true) -> "🪷"
                else -> "✨"
            }
        } else {
            raw
        }
    }

    private fun readAssetJson(fileName: String): String? {
        if (context == null) return null
        return try {
            val inputStream = context.assets.open("bot/$fileName")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line).append('\n')
                line = reader.readLine()
            }
            reader.close()
            sb.toString()
        } catch (_: Exception) {
            null
        }
    }

    private fun loadData() {
        // 1. Items & Recipes
        val itemsJson = readAssetJson("items.json")
        if (itemsJson != null) {
            try {
                val root = JSONObject(itemsJson)
                val itemsArr = root.optJSONArray("items") ?: JSONArray()
                for (i in 0 until itemsArr.length()) {
                    val obj = itemsArr.getJSONObject(i)
                    items.add(
                        BotItem(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = cleanEmoji(obj.optString("menuEmoji", obj.optString("emoji", "📦"))),
                            desc = obj.optString("desc", ""),
                            price = obj.optInt("price", 0),
                            category = obj.optString("category", "General")
                        )
                    )
                }

                val craftsArr = root.optJSONArray("craftRecipes") ?: JSONArray()
                for (i in 0 until craftsArr.length()) {
                    val obj = craftsArr.getJSONObject(i)
                    val ingArr = obj.optJSONArray("ingredients") ?: JSONArray()
                    val ingredients = mutableListOf<BotIngredient>()
                    for (j in 0 until ingArr.length()) {
                        val ingObj = ingArr.getJSONObject(j)
                        ingredients.add(BotIngredient(ingObj.getString("id"), ingObj.getInt("qty")))
                    }
                    val resObj = obj.getJSONObject("result")
                    val result = BotCraftResult(
                        type = resObj.getString("type"),
                        id = resObj.optString("id", null),
                        qty = resObj.optInt("qty", 1),
                        amount = resObj.optInt("amount", 0)
                    )
                    craftRecipes.add(
                        BotCraftRecipe(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = cleanEmoji(obj.optString("emoji", "🔨")),
                            ingredients = ingredients,
                            result = result,
                            desc = obj.optString("desc", "")
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // 2. Farming Crops
        val farmJson = readAssetJson("farming.json")
        if (farmJson != null) {
            try {
                val root = JSONObject(farmJson)
                val cropsArr = root.optJSONArray("crops") ?: JSONArray()
                for (i in 0 until cropsArr.length()) {
                    val obj = cropsArr.getJSONObject(i)
                    crops.add(
                        BotCrop(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = cleanEmoji(obj.getString("emoji")),
                            tier = obj.getString("tier"),
                            cost = obj.getInt("cost"),
                            timeMinutes = obj.getInt("time"),
                            minYield = obj.getInt("minYield"),
                            maxYield = obj.getInt("maxYield"),
                            sellPrice = obj.getInt("sellPrice")
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // 3. Fish, Rods, Baits, Locations
        val fishJson = readAssetJson("fish.json")
        if (fishJson != null) {
            try {
                val root = JSONObject(fishJson)
                val fishesArr = root.optJSONArray("fish") ?: JSONArray()
                for (i in 0 until fishesArr.length()) {
                    val obj = fishesArr.getJSONObject(i)
                    fishList.add(
                        BotFish(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            tier = obj.getString("tier"),
                            emoji = cleanEmoji(obj.optString("emoji", "🐟")),
                            location = obj.optString("location", "river")
                        )
                    )
                }

                val rodsArr = root.optJSONArray("rods") ?: JSONArray()
                for (i in 0 until rodsArr.length()) {
                    val obj = rodsArr.getJSONObject(i)
                    rods.add(
                        BotRod(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = cleanEmoji(obj.optString("emoji", "🎣")),
                            price = obj.optInt("price", 0),
                            cooldown = obj.optInt("cooldown", 10),
                            rareBonus = obj.optInt("rareBonus", 0),
                            tier = obj.optInt("tier", 0)
                        )
                    )
                }

                val baitsArr = root.optJSONArray("baits") ?: JSONArray()
                for (i in 0 until baitsArr.length()) {
                    val obj = baitsArr.getJSONObject(i)
                    baits.add(
                        BotBait(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = cleanEmoji(obj.optString("emoji", "🪱")),
                            price = obj.optInt("price", 0),
                            rareBonus = obj.optInt("rareBonus", 0),
                            trophyBonus = obj.optInt("trophyBonus", 0)
                        )
                    )
                }

                val locsArr = root.optJSONArray("locations") ?: JSONArray()
                for (i in 0 until locsArr.length()) {
                    val obj = locsArr.getJSONObject(i)
                    val tiersArr = obj.optJSONArray("tiers") ?: JSONArray()
                    val tiersList = mutableListOf<String>()
                    for (t in 0 until tiersArr.length()) {
                        tiersList.add(tiersArr.getString(t))
                    }
                    locations.add(
                        BotFishingLocation(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            desc = obj.optString("desc", ""),
                            requiredRodTier = obj.optInt("requiredRodTier", 0),
                            luckPenalty = obj.optInt("luckPenalty", 0),
                            bonusRare = obj.optInt("bonusRare", 0),
                            tiers = tiersList,
                            monsterChance = obj.optInt("monsterChance", 0),
                            isSecret = obj.optBoolean("isSecret", false)
                        )
                    )
                }

                val seaMonstersArr = root.optJSONArray("seaMonsters") ?: JSONArray()
                for (i in 0 until seaMonstersArr.length()) {
                    val obj = seaMonstersArr.getJSONObject(i)
                    seaMonsters.add(
                        BotSeaMonster(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = cleanEmoji(obj.optString("emoji", "🐙")),
                            location = obj.optString("location", "ocean"),
                            chance = obj.optInt("chance", 10),
                            damage = obj.optString("damage", "bait"),
                            desc = obj.optString("desc", "")
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // 4. Pets & Eggs
        val petsJson = readAssetJson("pets.json")
        if (petsJson != null) {
            try {
                val root = JSONObject(petsJson)
                val petsArr = root.optJSONArray("pets") ?: JSONArray()
                for (i in 0 until petsArr.length()) {
                    val obj = petsArr.getJSONObject(i)
                    pets.add(
                        BotPet(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            tier = obj.getString("tier"),
                            element = obj.optString("element", "nature"),
                            emoji = cleanEmoji(obj.optString("emoji", "🐾")),
                            baseHp = obj.optInt("baseHp", 100),
                            baseAtk = obj.optInt("baseAtk", 15),
                            baseDef = obj.optInt("baseDef", 10),
                            desc = obj.optString("desc", "")
                        )
                    )
                }

                val eggsArr = root.optJSONArray("eggs") ?: JSONArray()
                for (i in 0 until eggsArr.length()) {
                    val obj = eggsArr.getJSONObject(i)
                    petEggs.add(
                        BotPetEgg(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = cleanEmoji(obj.optString("emoji", "🥚")),
                            price = obj.optInt("price", 5000)
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // 5. Dungeons & Bosses
        val dungeonsJson = readAssetJson("dungeons.json")
        if (dungeonsJson != null) {
            try {
                val root = JSONObject(dungeonsJson)
                val dtArr = root.optJSONArray("dungeonTiers") ?: JSONArray()
                for (i in 0 until dtArr.length()) {
                    val obj = dtArr.getJSONObject(i)
                    val hpArr = obj.optJSONArray("monsterHp") ?: JSONArray()
                    val hpList = (0 until hpArr.length()).map { hpArr.getInt(it) }
                    val atkArr = obj.optJSONArray("monsterAtk") ?: JSONArray()
                    val atkList = (0 until atkArr.length()).map { atkArr.getInt(it) }
                    val rewArr = obj.optJSONArray("reward") ?: JSONArray()
                    val lootArr = obj.optJSONArray("loot") ?: JSONArray()
                    val lootList = mutableListOf<BotLootItem>()
                    for (l in 0 until lootArr.length()) {
                        val lo = lootArr.getJSONObject(l)
                        lootList.add(BotLootItem(lo.getString("item"), lo.optDouble("chance", 0.1), lo.optInt("min", 1), lo.optInt("max", 1)))
                    }
                    dungeons.add(
                        BotDungeon(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            minLevel = obj.optInt("minLevel", 1),
                            waves = obj.optInt("waves", 3),
                            monsterHp = hpList,
                            monsterAtk = atkList,
                            minReward = if (rewArr.length() > 0) rewArr.getInt(0) else 100,
                            maxReward = if (rewArr.length() > 1) rewArr.getInt(1) else 200,
                            exp = obj.optInt("exp", 10),
                            cooldownMs = obj.optLong("cooldown", 60000L),
                            element = obj.optString("element", "nature"),
                            penaltyCap = obj.optInt("penaltyCap", 500),
                            relicChance = obj.optDouble("relicChance", 0.0),
                            loot = lootList
                        )
                    )
                }

                val blArr = root.optJSONArray("bossList") ?: JSONArray()
                for (i in 0 until blArr.length()) {
                    val obj = blArr.getJSONObject(i)
                    val rewArr = obj.optJSONArray("reward") ?: JSONArray()
                    val lootArr = obj.optJSONArray("loot") ?: JSONArray()
                    val lootList = mutableListOf<BotLootItem>()
                    for (l in 0 until lootArr.length()) {
                        val lo = lootArr.getJSONObject(l)
                        lootList.add(BotLootItem(lo.getString("item"), lo.optDouble("chance", 0.2), lo.optInt("min", 1), lo.optInt("max", 1)))
                    }
                    bosses.add(
                        BotBoss(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            minLevel = obj.optInt("minLevel", 5),
                            hp = obj.optInt("hp", 2000),
                            atk = obj.optInt("atk", 40),
                            def = obj.optInt("def", 20),
                            minReward = if (rewArr.length() > 0) rewArr.getInt(0) else 1000,
                            maxReward = if (rewArr.length() > 1) rewArr.getInt(1) else 2000,
                            exp = obj.optInt("exp", 50),
                            element = obj.optString("element", "nature"),
                            penaltyCap = obj.optInt("penaltyCap", 1000),
                            relicChance = obj.optDouble("relicChance", 0.3),
                            loot = lootList
                        )
                    )
                }
            } catch (_: Exception) {}
        }
    }

    override fun getItems(): List<BotItem> = items
    override fun getItem(id: String): BotItem? = items.find { it.id == id }
    override fun getCraftRecipes(): List<BotCraftRecipe> = craftRecipes
    override fun getCrops(): List<BotCrop> = crops
    override fun getCrop(id: String): BotCrop? = crops.find { it.id == id }
    override fun getFish(): List<BotFish> = fishList
    override fun getFishByLocation(location: String): List<BotFish> = fishList.filter { it.location == location }
    override fun getRods(): List<BotRod> = rods
    override fun getBaits(): List<BotBait> = baits
    override fun getFishingLocations(): List<BotFishingLocation> = locations
    override fun getSeaMonsters(): List<BotSeaMonster> = seaMonsters
    override fun getPets(): List<BotPet> = pets
    override fun getPet(id: String): BotPet? = pets.find { it.id == id }
    override fun getPetEggs(): List<BotPetEgg> = petEggs
    override fun getDungeons(): List<BotDungeon> = dungeons
    override fun getBosses(): List<BotBoss> = bosses

    override fun mapToGameItemDefs(): List<GameItemDef> {
        return items.map { botItem ->
            val rarity = when (botItem.category.lowercase()) {
                "booster" -> ItemRarity.RARE
                "proteksi", "luck" -> ItemRarity.UNCOMMON
                "battle" -> if (botItem.id.contains("fragment") || botItem.id.contains("crystal")) ItemRarity.LEGENDARY else ItemRarity.RARE
                "special" -> if (botItem.id == "omega_core") ItemRarity.MYTHIC else ItemRarity.EPIC
                else -> ItemRarity.COMMON
            }

            val type = when (botItem.category.lowercase()) {
                "farming" -> ItemType.MATERIAL
                "fishing" -> ItemType.TOOL
                "consumable" -> ItemType.FOOD
                "pet" -> ItemType.PET_FOOD
                else -> ItemType.MATERIAL
            }

            val isUsable = botItem.category.lowercase() in listOf("booster", "consumable", "luck", "proteksi") ||
                    botItem.id.contains("potion") || botItem.id.contains("box") || botItem.id.contains("doubler")

            GameItemDef(
                id = botItem.id,
                name = botItem.name,
                itemType = type,
                rarity = rarity,
                iconEmoji = botItem.emoji,
                description = botItem.desc,
                buyPrice = if (botItem.price > 0) botItem.price else null,
                sellPrice = if (botItem.price > 0) maxOf(5, botItem.price / 2) else 10,
                isUsable = isUsable
            )
        }
    }

    override fun mapToCropDefs(): List<CropDef> {
        return crops.map { c ->
            val rarity = try {
                ItemRarity.valueOf(c.tier.uppercase())
            } catch (_: Exception) {
                ItemRarity.COMMON
            }

            CropDef(
                id = c.id,
                name = c.name,
                seedName = "Benih ${c.name}",
                growDurationSeconds = (c.timeMinutes * 4).toLong().coerceAtLeast(10L),
                expReward = maxOf(5, c.cost / 2),
                sellPrice = c.sellPrice,
                seedCost = c.cost,
                iconEmoji = c.emoji,
                rarity = rarity
            )
        }
    }

    override fun mapToFishDefs(): List<FishDef> {
        return fishList.mapIndexed { idx, f ->
            val rarity = when (f.tier.lowercase()) {
                "trash" -> ItemRarity.COMMON
                "common" -> ItemRarity.COMMON
                "uncommon" -> ItemRarity.UNCOMMON
                "rare" -> ItemRarity.RARE
                "epic" -> ItemRarity.EPIC
                "legendary" -> ItemRarity.LEGENDARY
                "mythic" -> ItemRarity.MYTHIC
                "secret" -> ItemRarity.SECRET
                "god" -> ItemRarity.GOD
                else -> ItemRarity.COMMON
            }

            val minKg = 0.5f + (idx % 10) * 0.4f
            val maxKg = minKg * 3.5f
            val basePrice = when (rarity) {
                ItemRarity.COMMON -> 15 + (idx % 5) * 5
                ItemRarity.UNCOMMON -> 40 + (idx % 5) * 10
                ItemRarity.RARE -> 100 + (idx % 10) * 20
                ItemRarity.EPIC -> 300 + (idx % 10) * 50
                ItemRarity.LEGENDARY -> 800 + (idx % 10) * 120
                ItemRarity.MYTHIC -> 2000 + (idx % 10) * 300
                ItemRarity.SECRET -> 4500 + (idx % 5) * 500
                ItemRarity.GOD -> 12000 + (idx % 5) * 2000
            }

            FishDef(
                id = f.id,
                name = f.name,
                rarity = rarity,
                minWeightKg = minKg,
                maxWeightKg = maxKg,
                basePrice = basePrice,
                expReward = maxOf(5, basePrice / 4),
                requiredRodLevel = when (rarity) {
                    ItemRarity.COMMON -> 0
                    ItemRarity.UNCOMMON -> 1
                    ItemRarity.RARE -> 2
                    ItemRarity.EPIC -> 4
                    ItemRarity.LEGENDARY -> 6
                    ItemRarity.MYTHIC -> 8
                    ItemRarity.SECRET -> 10
                    ItemRarity.GOD -> 12
                },
                iconEmoji = f.emoji,
                lore = "Ditemukan di perairan ${f.location.replace("_", " ")}. Rarity tier: ${f.tier}."
            )
        }
    }

    override fun mapToRodTiers(): List<FishingRodTier> {
        return rods.map { r ->
            FishingRodTier(
                level = r.tier,
                name = r.name,
                catchSpeedMultiplier = 1.0f + (r.rareBonus * 0.04f),
                rareBonusPercent = r.rareBonus,
                upgradeCostGold = r.price,
                upgradeCostStones = r.tier * 2,
                description = "Cooldown: ${r.cooldown}s • Rare Bonus +${r.rareBonus}%"
            )
        }
    }

    override fun mapToBaits(): List<BaitDef> {
        return baits.map { b ->
            BaitDef(
                id = b.id,
                name = b.name,
                costGold = b.price,
                rareBonus = b.rareBonus,
                iconEmoji = b.emoji,
                description = "Bonus peluang ikan langka +${b.rareBonus}%${if (b.trophyBonus > 0) " • Trophy +${b.trophyBonus}%" else ""}"
            )
        }
    }

    companion object {
        @Volatile
        private var instance: BotDataRepository? = null

        fun getInstance(context: Context? = null): BotDataRepository {
            return instance ?: synchronized(this) {
                instance ?: BotDataRepository(context).also { instance = it }
            }
        }
    }
}
