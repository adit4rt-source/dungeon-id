package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AccountDialog
import com.example.ui.components.PixelBottomNavigationBar
import com.example.ui.components.PlayerTopAppBar
import com.example.ui.components.RetroGold
import com.example.util.LocalSoundManager
import com.example.util.ProvideSoundManager
import com.example.ui.screens.AdventureScreen
import com.example.ui.screens.FarmingScreen
import com.example.ui.screens.FishingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.PetScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

enum class GameScreen(val label: String, val icon: String, val testTag: String) {
    HOME("DESA", "🏰", "nav_tab_home"),
    INVENTORY("TAS", "🎒", "nav_tab_inventory"),
    FISHING("MANCING", "🎣", "nav_tab_fishing"),
    FARMING("KEBUN", "🌾", "nav_tab_farming"),
    PETS("PET", "🐾", "nav_tab_pets"),
    ADVENTURE("DUNGEON", "⚔️", "nav_tab_adventure"),
    MARKET("PASAR", "🏪", "nav_tab_market")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ProvideSoundManager {
                    MainGameApp()
                }
            }
        }
    }
}

@Composable
fun MainGameApp(
    viewModel: GameViewModel = viewModel()
) {
    val soundManager = LocalSoundManager.current
    var currentScreen by remember { mutableStateOf(GameScreen.HOME) }
    var showAccountDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val player by viewModel.playerProfile.collectAsStateWithLifecycle()
    val userAccount by viewModel.activeUserAccount.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()

    BackHandler(enabled = currentScreen != GameScreen.HOME) {
        soundManager.playMenuClick()
        currentScreen = GameScreen.HOME
    }

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Dynamic retro 8-bit backsound music for Desa, Kebun, Mancing, and Berburu
    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            GameScreen.HOME, GameScreen.MARKET, GameScreen.PETS -> soundManager.playDesaBgm()
            GameScreen.FARMING -> soundManager.playKebunBgm()
            GameScreen.FISHING -> soundManager.playMancingBgm()
            GameScreen.ADVENTURE -> soundManager.playBerburuBgm()
            GameScreen.INVENTORY -> {
                if (soundManager.getCurrentBgm() == null) {
                    soundManager.playDesaBgm()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.stopBgm()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PlayerTopAppBar(
                profile = player,
                userAccount = userAccount,
                onOpenAccountDialog = { showAccountDialog = true }
            )
        },
        bottomBar = {
            PixelBottomNavigationBar(
                currentScreen = currentScreen,
                onSelectScreen = { selectedScreen ->
                    if (currentScreen != selectedScreen) {
                        soundManager.playTabSwitch()
                        currentScreen = selectedScreen
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    GameScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToFishing = { currentScreen = GameScreen.FISHING },
                        onNavigateToFarming = { currentScreen = GameScreen.FARMING },
                        onNavigateToPets = { currentScreen = GameScreen.PETS },
                        onNavigateToAdventure = { currentScreen = GameScreen.ADVENTURE },
                        onNavigateToMarket = { currentScreen = GameScreen.MARKET },
                        onNavigateToInventory = { currentScreen = GameScreen.INVENTORY }
                    )
                    GameScreen.INVENTORY -> InventoryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = GameScreen.HOME }
                    )
                    GameScreen.FISHING -> FishingScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = GameScreen.HOME }
                    )
                    GameScreen.FARMING -> FarmingScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = GameScreen.HOME },
                        onNavigateToMarket = { currentScreen = GameScreen.MARKET }
                    )
                    GameScreen.PETS -> PetScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = GameScreen.HOME }
                    )
                    GameScreen.ADVENTURE -> AdventureScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = GameScreen.HOME }
                    )
                    GameScreen.MARKET -> MarketScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = GameScreen.HOME }
                    )
                }
            }

            if (showAccountDialog) {
                AccountDialog(
                    currentAccount = userAccount,
                    allAccounts = allAccounts,
                    onDismiss = { showAccountDialog = false },
                    onLoginCredentials = { identifier, password, callback ->
                        viewModel.loginWithCredentials(identifier, password) { success, msg ->
                            callback(success, msg)
                            if (success) showAccountDialog = false
                        }
                    },
                    onRegister = { username, email, password, discordTag, callback ->
                        viewModel.register(username, email, password, discordTag) { success, msg ->
                            callback(success, msg)
                            if (success) showAccountDialog = false
                        }
                    },
                    onLoginDiscord = { discordTag, email, callback ->
                        viewModel.loginWithDiscord(discordTag, email) { success, msg ->
                            callback(success, msg)
                            if (success) showAccountDialog = false
                        }
                    },
                    onLinkDiscord = { discordTag, callback ->
                        viewModel.linkDiscord(discordTag) { success, msg ->
                            callback(success, msg)
                        }
                    },
                    onLoginGoogle = { username, email ->
                        viewModel.loginWithGoogle(username, email)
                        showAccountDialog = false
                    },
                    onLoginFacebook = { username, email ->
                        viewModel.loginWithFacebook(username, email)
                        showAccountDialog = false
                    },
                    onLoginGuest = {
                        viewModel.loginAsGuest()
                        showAccountDialog = false
                    },
                    onLinkProvider = { provider ->
                        viewModel.linkProvider(provider)
                    },
                    onSwitchAccount = { accountId ->
                        viewModel.switchAccount(accountId)
                    },
                    onLogout = {
                        viewModel.logout()
                        showAccountDialog = false
                    }
                )
            }
        }
    }
}
