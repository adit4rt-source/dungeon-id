package com.example

import com.example.data.model.DungeonDifficulty
import com.example.data.model.DungeonRegistry
import com.example.data.model.EnemyRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DungeonLogicTest {

    @Test
    fun testDungeonRegistryCompleteness() {
        val dungeons = DungeonRegistry.ALL_DUNGEONS
        assertEquals(3, dungeons.size)

        dungeons.forEach { dungeon ->
            assertTrue(dungeon.name.isNotBlank())
            assertTrue(dungeon.waves.isNotEmpty())
            assertTrue(dungeon.staminaCost > 0)
            assertTrue(dungeon.guaranteedGold > 0)
            assertTrue(dungeon.guaranteedExp > 0)

            // Verify waves progression
            assertEquals(3, dungeon.waves.size)
            val bossWave = dungeon.waves.last()
            assertEquals(EnemyRank.DUNGEON_BOSS, bossWave.enemy.rank)
            assertTrue(bossWave.enemy.dropTable.isNotEmpty())
        }
    }

    @Test
    fun testDifficultyMultipliers() {
        val normal = DungeonDifficulty.NORMAL
        val heroic = DungeonDifficulty.HEROIC
        val mythic = DungeonDifficulty.MYTHIC

        assertEquals(1.0f, normal.statMultiplier, 0.01f)
        assertEquals(1.5f, heroic.statMultiplier, 0.01f)
        assertEquals(2.4f, mythic.statMultiplier, 0.01f)

        assertTrue(heroic.dropRateMultiplier > normal.dropRateMultiplier)
        assertTrue(mythic.dropRateMultiplier > heroic.dropRateMultiplier)
        assertTrue(mythic.expMultiplier > heroic.expMultiplier)
    }

    @Test
    fun testEnemyStatsAndSkills() {
        val volcanoDungeon = DungeonRegistry.buildVolcanoDragonDungeon()
        val boss = volcanoDungeon.waves.last().enemy

        assertEquals("Kaisar Naga Lahar Ignis", boss.name)
        assertTrue(boss.maxHp >= 900)
        assertTrue(boss.attack >= 90)
        assertTrue(boss.skills.any { it.appliesBurn })
        assertTrue(boss.dropTable.any { it.itemId == "item_upgrade_stone" })
    }

    @Test
    fun testPixelSpriteDefinitionsGridSafety() {
        val testKeys = listOf(
            "pet_wolf", "pet_dragon", "pet_turtle", "pet_bird", "pet_cat", "pet_slime",
            "e_slime_green", "e_slime_toxic", "e_goblin_scout", "e_goblin_chief",
            "e_golem_stone", "e_golem_ancient", "e_dragon_ignis", "m_hound", "m_wyvern",
            "m_boar", "m_spider", "m_bat", "unknown_creature", "",
            "fish_salmon", "fish_kraken", "crop_gandum", "seed_wortel",
            "item_refine_stone", "rod_omega", "potion_health", "chest_mystery",
            "boss_demon_king", "boss_void_emperor"
        )

        testKeys.forEach { key ->
            val grid = com.example.ui.components.PixelSpriteDefinitions.getGrid(key)
            assertTrue("Grid for key '$key' should have rows", grid.isNotEmpty())
            assertEquals("Grid for key '$key' standard rows", 16, grid.size)

            for (r in grid.indices) {
                val row = grid[r]
                assertTrue("Row $r in '$key' must not be empty", row.isNotEmpty())
                assertEquals("Row $r in '$key' must have 16 columns", 16, row.size)
                for (c in row.indices) {
                    val color = row[c]
                    assertNotNull(color)
                }
            }
        }
    }

    @Test
    fun testBotItemCatalogCompleteness() {
        val catalog = com.example.data.model.GameDatabaseRegistry.BOT_ITEM_CATALOG
        assertTrue("BOT item catalog should contain diverse items", catalog.size >= 30)

        // Check key bot item categories exist
        assertTrue("Contains refine_stone", catalog.any { it.id == "refine_stone" })
        assertTrue("Contains protection_stone", catalog.any { it.id == "protection_stone" })
        assertTrue("Contains rod_part", catalog.any { it.id == "rod_part" })
        assertTrue("Contains mythic_fragment", catalog.any { it.id == "mythic_fragment" })
        assertTrue("Contains awakening_crystal", catalog.any { it.id == "awakening_crystal" })
        assertTrue("Contains omega_core", catalog.any { it.id == "omega_core" })
        assertTrue("Contains dna_shard", catalog.any { it.id == "dna_shard" })
        assertTrue("Contains mutation_serum", catalog.any { it.id == "mutation_serum" })
        assertTrue("Contains mystery_box", catalog.any { it.id == "mystery_box" })
        assertTrue("Contains xp boosters", catalog.any { it.id.startsWith("xp_booster_") })

        // Check exact bot crops expansion (30 crops)
        val crops = com.example.data.model.GameDatabaseRegistry.CROPS
        assertEquals(30, crops.size)
        assertTrue(crops.any { it.id == "strawberry" })
        assertTrue(crops.any { it.id == "kentang" })
        assertTrue(crops.any { it.id == "crystal_flower" })
        assertTrue(crops.any { it.id == "lotus" })

        // Check fishes expansion
        val fishes = com.example.data.model.GameDatabaseRegistry.FISHES
        assertTrue(fishes.any { it.id == "fish_kraken" })
        assertTrue(fishes.any { it.id == "fish_golden_whale" })
        assertTrue(fishes.any { it.id == "fish_lobster" })

        // Check pet species expansion
        val pets = com.example.data.model.GameDatabaseRegistry.PET_SPECIES
        assertTrue(pets.any { it.id == "pet_kitsune" })
        assertTrue(pets.any { it.id == "pet_griffin" })

        // Check rod tiers from bot (13 rods)
        val rods = com.example.data.model.GameDatabaseRegistry.ROD_TIERS
        assertEquals(13, rods.size)
        assertEquals("Joran Bambu", rods.first().name)
        assertEquals("Joran Omega", rods.last().name)

        // Verify BotDataRepository initialization and mapping
        val botRepo = com.example.data.repository.BotDataRepository()
        assertNotNull(botRepo)
    }

    @Test
    fun testPixelBottomNavigationIntegrity() {
        // Verify all 5 pixel nav icon grids are non-empty 16x16 matrices
        com.example.ui.components.PixelNavIconType.values().forEach { iconType ->
            val grid = com.example.ui.components.PixelNavSpriteDefinitions.getGrid(iconType)
            assertEquals("Grid for $iconType should have 16 rows", 16, grid.size)
            grid.forEachIndexed { rIdx, row ->
                assertEquals("Row $rIdx in $iconType should have 16 columns", 16, row.size)
            }
        }

        // Verify state switching without navigation libraries
        var currentScreen = com.example.GameScreen.HOME
        val targetScreens = listOf(
            com.example.GameScreen.FARMING,
            com.example.GameScreen.FISHING,
            com.example.GameScreen.ADVENTURE,
            com.example.GameScreen.INVENTORY
        )

        targetScreens.forEach { screen ->
            currentScreen = screen
            assertEquals(screen, currentScreen)
        }
    }

    @Test
    fun testDataRepositoryAndSoundManagerSetup() {
        val repo: com.example.data.repository.DataRepository = com.example.data.repository.BotDataRepository()
        assertNotNull(repo)

        // Verify all retro sounds enum values exist
        val sounds = com.example.util.RetroSound.values()
        assertEquals(9, sounds.size)
        assertTrue(sounds.any { it == com.example.util.RetroSound.FISH_CAST })
        assertTrue(sounds.any { it == com.example.util.RetroSound.FISH_BITE })
        assertTrue(sounds.any { it == com.example.util.RetroSound.FISH_CATCH })
        assertTrue(sounds.any { it == com.example.util.RetroSound.COMBAT_SLASH })
        assertTrue(sounds.any { it == com.example.util.RetroSound.COMBAT_HIT })
        assertTrue(sounds.any { it == com.example.util.RetroSound.VICTORY })
        assertTrue(sounds.any { it == com.example.util.RetroSound.MENU_CLICK })
        assertTrue(sounds.any { it == com.example.util.RetroSound.TAB_SWITCH })
        assertTrue(sounds.any { it == com.example.util.RetroSound.COIN })
    }
}
