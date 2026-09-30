package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.GameDao
import com.example.data.repository.GameRepository
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AccountSystemTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: GameDao
    private lateinit var repository: GameRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.gameDao()
        repository = GameRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSecurityUtilsValidationAndHashing() {
        // Username validation
        assertNotNull(SecurityUtils.validateUsername("ab")) // too short
        assertNotNull(SecurityUtils.validateUsername("user name with space")) // invalid chars
        assertNull(SecurityUtils.validateUsername("adit4rt")) // valid
        assertNull(SecurityUtils.validateUsername("hero_123")) // valid

        // Email validation
        assertNotNull(SecurityUtils.validateEmail("invalid-email"))
        assertNotNull(SecurityUtils.validateEmail("@domain.com"))
        assertNull(SecurityUtils.validateEmail("hero@dungeon.com"))
        assertNull(SecurityUtils.validateEmail("adit.rpg@realm.id"))

        // Password validation
        assertNotNull(SecurityUtils.validatePassword("123"))
        assertNull(SecurityUtils.validatePassword("secret123"))

        // Hashing & Verification
        val hash = SecurityUtils.hashPassword("mySecretPassword")
        assertTrue(hash.length == 64) // SHA-256 hex length
        assertTrue(SecurityUtils.verifyPassword("mySecretPassword", hash))
        assertFalse(SecurityUtils.verifyPassword("wrongPassword", hash))
        assertFalse(SecurityUtils.verifyPassword("mySecretPassword", null))
    }

    @Test
    fun testRegisterAndLoginWithUsernameOrEmail() = runBlocking {
        repository.initializeGameIfNeeded()

        // 1. Register new account
        val regResult = repository.registerAccount(
            username = "PetualangAdit",
            email = "adit@dungeon.id",
            password = "passwordSuperSecret123",
            discordId = "Adit#1337"
        )
        assertTrue(regResult.isSuccess)
        val registeredAccount = regResult.getOrThrow()
        assertEquals("PetualangAdit", registeredAccount.username)
        assertEquals("adit@dungeon.id", registeredAccount.email)
        assertEquals("Adit#1337", registeredAccount.discordId)
        assertTrue(registeredAccount.isCurrentActive)

        // 2. Prevent duplicate username
        val duplicateUsername = repository.registerAccount(
            username = "petualangadit", // case insensitive
            email = "other@dungeon.id",
            password = "password123"
        )
        assertTrue(duplicateUsername.isFailure)

        // 3. Prevent duplicate email
        val duplicateEmail = repository.registerAccount(
            username = "AnotherHero",
            email = "ADIT@dungeon.id",
            password = "password123"
        )
        assertTrue(duplicateEmail.isFailure)

        // 4. Login with Username & correct password
        val loginUsername = repository.loginWithUsernameOrEmail("PetualangAdit", "passwordSuperSecret123")
        assertTrue(loginUsername.isSuccess)
        assertEquals("PetualangAdit", loginUsername.getOrThrow().username)

        // 5. Login with Email & correct password
        val loginEmail = repository.loginWithUsernameOrEmail("adit@dungeon.id", "passwordSuperSecret123")
        assertTrue(loginEmail.isSuccess)

        // 6. Login with wrong password
        val loginFail = repository.loginWithUsernameOrEmail("PetualangAdit", "wrongPass")
        assertTrue(loginFail.isFailure)

        // 7. Login with non-existent user
        val loginUnknown = repository.loginWithUsernameOrEmail("NonExistentUser", "anyPassword")
        assertTrue(loginUnknown.isFailure)
    }

    @Test
    fun testDiscordLoginAndLinking() = runBlocking {
        repository.initializeGameIfNeeded()

        // 1. Login with Discord creates new Discord-based account if not exists
        val discordResult = repository.loginWithDiscordAccount("GamerDungeon#9999", "gamer@discord.local")
        assertTrue(discordResult.isSuccess)
        val discordAccount = discordResult.getOrThrow()
        assertEquals("GamerDungeon#9999", discordAccount.username)
        assertEquals("GamerDungeon#9999", discordAccount.discordId)
        assertEquals("DISCORD", discordAccount.provider)
        assertTrue(discordAccount.isCurrentActive)

        // 2. Link Discord to existing registered account
        val regAccount = repository.registerAccount(
            username = "SolitaryKnight",
            email = "knight@dungeon.id",
            password = "passKnight123"
        ).getOrThrow()
        assertNull(regAccount.discordId)

        val linkResult = repository.linkDiscordToCurrentAccount("Knight#4321")
        assertTrue(linkResult.isSuccess)

        val active = dao.getActiveUserAccountSync()
        assertNotNull(active)
        assertEquals("Knight#4321", active?.discordId)
        assertTrue(active?.linkedProviders?.contains("DISCORD") == true)
    }

    @Test
    fun testAccountSwitchingAndGuestMode() = runBlocking {
        repository.initializeGameIfNeeded()

        val acc1 = repository.registerAccount("HeroOne", "hero1@game.com", "pass1234").getOrThrow()
        val acc2 = repository.registerAccount("HeroTwo", "hero2@game.com", "pass1234").getOrThrow()

        // Currently HeroTwo is active
        var active = dao.getActiveUserAccountSync()
        assertEquals(acc2.id, active?.id)

        // Switch back to HeroOne
        val switched = repository.switchUserAccount(acc1.id)
        assertTrue(switched)
        active = dao.getActiveUserAccountSync()
        assertEquals(acc1.id, active?.id)

        // Logout returns to Guest
        repository.logoutCurrentAccount()
        active = dao.getActiveUserAccountSync()
        assertEquals("GUEST", active?.provider)
    }
}
