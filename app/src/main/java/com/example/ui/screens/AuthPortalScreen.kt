package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.components.PixelBadge
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelFrame
import com.example.ui.components.RetroGold
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily
import com.example.util.LocalSoundManager

enum class PortalTab {
    LOGIN,
    REGISTER,
    SAVED_ACCOUNTS
}

@Composable
fun AuthPortalScreen(
    savedAccounts: List<UserAccountEntity>,
    onLogin: (identifier: String, pass: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onRegister: (username: String, email: String, pass: String, discord: String?, onResult: (Boolean, String) -> Unit) -> Unit,
    onLoginDiscord: (discordTag: String, email: String?, onResult: (Boolean, String) -> Unit) -> Unit,
    onLoginGoogle: (username: String, email: String) -> Unit,
    onLoginFacebook: (username: String, email: String) -> Unit,
    onGuestLogin: () -> Unit,
    onSwitchAccount: (accountId: String) -> Unit
) {
    val soundManager = LocalSoundManager.current
    var selectedTab by remember {
        mutableStateOf(if (savedAccounts.isNotEmpty()) PortalTab.SAVED_ACCOUNTS else PortalTab.LOGIN)
    }

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Register Form State
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regDiscordTag by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }

    // Discord Quick Login State
    var showDiscordInput by remember { mutableStateOf(false) }
    var discordTagInput by remember { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0B18),
                        Color(0xFF1B122C),
                        Color(0xFF0A0711)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val isWide = maxWidth >= 640.dp

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Title Banner
                Text(
                    text = "⚔️ DUNGEON ID 🏰",
                    fontFamily = PressStartFontFamily,
                    fontSize = if (isWide) 18.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetroGold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "PETUALANGAN DUNGEON • KEBUN • MANCING • PET",
                    fontFamily = Vt323FontFamily,
                    fontSize = 18.sp,
                    color = Color(0xFFCE93D8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Tab Switcher
                PixelFrame(
                    backgroundColor = Color(0xFF130E22),
                    borderColor = Color(0xFF3B2A56),
                    contentPadding = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PixelButton(
                            onClick = {
                                soundManager.playTabSwitch()
                                selectedTab = PortalTab.LOGIN
                                statusMessage = null
                            },
                            backgroundColor = if (selectedTab == PortalTab.LOGIN) Color(0xFF6A1B9A) else Color(0xFF241938),
                            modifier = Modifier.weight(1f),
                            testTag = "portal_tab_login"
                        ) {
                            Text(
                                "🔑 MASUK",
                                fontFamily = PressStartFontFamily,
                                fontSize = 8.sp,
                                color = if (selectedTab == PortalTab.LOGIN) Color.White else Color(0xFFB0BEC5)
                            )
                        }

                        PixelButton(
                            onClick = {
                                soundManager.playTabSwitch()
                                selectedTab = PortalTab.REGISTER
                                statusMessage = null
                            },
                            backgroundColor = if (selectedTab == PortalTab.REGISTER) Color(0xFF2E7D32) else Color(0xFF241938),
                            modifier = Modifier.weight(1f),
                            testTag = "portal_tab_register"
                        ) {
                            Text(
                                "📝 DAFTAR",
                                fontFamily = PressStartFontFamily,
                                fontSize = 8.sp,
                                color = if (selectedTab == PortalTab.REGISTER) Color.White else Color(0xFFB0BEC5)
                            )
                        }

                        if (savedAccounts.isNotEmpty()) {
                            PixelButton(
                                onClick = {
                                    soundManager.playTabSwitch()
                                    selectedTab = PortalTab.SAVED_ACCOUNTS
                                    statusMessage = null
                                },
                                backgroundColor = if (selectedTab == PortalTab.SAVED_ACCOUNTS) Color(0xFFE65100) else Color(0xFF241938),
                                modifier = Modifier.weight(1.2f),
                                testTag = "portal_tab_saved"
                            ) {
                                Text(
                                    "👥 AKUN (${savedAccounts.size})",
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 7.sp,
                                    color = if (selectedTab == PortalTab.SAVED_ACCOUNTS) Color.White else Color(0xFFB0BEC5)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Frame Container
                PixelFrame(
                    backgroundColor = Color(0xFF161126),
                    borderColor = Color(0xFF5D4037),
                    contentPadding = 14.dp,
                    modifier = Modifier.fillMaxWidth().testTag("auth_portal_card")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Status message banner
                        if (statusMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isErrorStatus) Color(0xFF4A1010) else Color(0xFF1B4D24),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isErrorStatus) Color(0xFFFF5252) else Color(0xFF66BB6A),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = statusMessage!!,
                                    fontFamily = Vt323FontFamily,
                                    fontSize = 16.sp,
                                    color = if (isErrorStatus) Color(0xFFFFCDD2) else Color(0xFFC8E6C9),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        when (selectedTab) {
                            PortalTab.LOGIN -> {
                                Text(
                                    text = "MASUK KE DUNIA PETUALANGAN",
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 8.sp,
                                    color = RetroGold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("USERNAME ATAU EMAIL:", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                                Spacer(modifier = Modifier.height(4.dp))
                                PortalPixelInput(
                                    value = loginIdentifier,
                                    onValueChange = { loginIdentifier = it },
                                    placeholder = "misal: dragon_slayer / user@email.com",
                                    testTag = "portal_input_identifier"
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("KATA SANDI:", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                                Spacer(modifier = Modifier.height(4.dp))
                                PortalPixelInput(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    placeholder = "minimal 6 karakter",
                                    isPassword = true,
                                    isPasswordVisible = isPasswordVisible,
                                    onTogglePassword = { isPasswordVisible = !isPasswordVisible },
                                    testTag = "portal_input_password"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                PixelButton(
                                    onClick = {
                                        soundManager.playMenuClick()
                                        if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                                            isErrorStatus = true
                                            statusMessage = "Mohon isi username/email dan kata sandi!"
                                            return@PixelButton
                                        }
                                        onLogin(loginIdentifier, loginPassword) { ok, msg ->
                                            if (!ok) {
                                                isErrorStatus = true
                                                statusMessage = msg
                                            }
                                        }
                                    },
                                    backgroundColor = Color(0xFF6A1B9A),
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "portal_submit_login_btn"
                                ) {
                                    Text("⚔️ MASUK KE GAME", fontFamily = PressStartFontFamily, fontSize = 9.sp, color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick OAuth options
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Divider(modifier = Modifier.weight(1f), color = Color(0xFF382B54))
                                    Text(
                                        " ATAU MASUK LEWAT ",
                                        fontFamily = Vt323FontFamily,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Divider(modifier = Modifier.weight(1f), color = Color(0xFF382B54))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PixelButton(
                                        onClick = {
                                            soundManager.playMenuClick()
                                            showDiscordInput = !showDiscordInput
                                        },
                                        backgroundColor = Color(0xFF5865F2),
                                        modifier = Modifier.weight(1f),
                                        testTag = "portal_oauth_discord_btn"
                                    ) {
                                        Text("DISCORD", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                                    }

                                    PixelButton(
                                        onClick = {
                                            soundManager.playMenuClick()
                                            onLoginGoogle("Pemain Google", "google_hero@rpgrealm.com")
                                        },
                                        backgroundColor = Color(0xFFC62828),
                                        modifier = Modifier.weight(1f),
                                        testTag = "portal_oauth_google_btn"
                                    ) {
                                        Text("GOOGLE", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                                    }

                                    PixelButton(
                                        onClick = {
                                            soundManager.playMenuClick()
                                            onLoginFacebook("Pemain Facebook", "fb_hero@rpgrealm.com")
                                        },
                                        backgroundColor = Color(0xFF1565C0),
                                        modifier = Modifier.weight(1f),
                                        testTag = "portal_oauth_facebook_btn"
                                    ) {
                                        Text("FACEBOOK", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                                    }
                                }

                                AnimatedVisibility(visible = showDiscordInput) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .background(Color(0xFF1F1B35), RoundedCornerShape(4.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text("TAG DISCORD:", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFF8C9EFF))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        PortalPixelInput(
                                            value = discordTagInput,
                                            onValueChange = { discordTagInput = it },
                                            placeholder = "contoh: Petualang#1234 atau Petualang",
                                            testTag = "portal_input_discord_tag"
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        PixelButton(
                                            onClick = {
                                                soundManager.playMenuClick()
                                                if (discordTagInput.isBlank()) {
                                                    isErrorStatus = true
                                                    statusMessage = "Tag Discord tidak boleh kosong!"
                                                    return@PixelButton
                                                }
                                                onLoginDiscord(discordTagInput, null) { ok, msg ->
                                                    if (!ok) {
                                                        isErrorStatus = true
                                                        statusMessage = msg
                                                    }
                                                }
                                            },
                                            backgroundColor = Color(0xFF5865F2),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("MASUK DENGAN DISCORD", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            PortalTab.REGISTER -> {
                                Text(
                                    text = "DAFTAR AKUN BARU",
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 8.sp,
                                    color = Color(0xFF81C784)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("USERNAME (3-20 Karakter):", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                                Spacer(modifier = Modifier.height(4.dp))
                                PortalPixelInput(
                                    value = regUsername,
                                    onValueChange = { regUsername = it },
                                    placeholder = "misal: kesatria_naga",
                                    testTag = "portal_reg_username"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("EMAIL:", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                                Spacer(modifier = Modifier.height(4.dp))
                                PortalPixelInput(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    placeholder = "user@domain.com",
                                    keyboardType = KeyboardType.Email,
                                    testTag = "portal_reg_email"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("KATA SANDI (min. 6 karakter):", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                                Spacer(modifier = Modifier.height(4.dp))
                                PortalPixelInput(
                                    value = regPassword,
                                    onValueChange = { regPassword = it },
                                    placeholder = "kata sandi rahasia",
                                    isPassword = true,
                                    isPasswordVisible = isRegPasswordVisible,
                                    onTogglePassword = { isRegPasswordVisible = !isRegPasswordVisible },
                                    testTag = "portal_reg_password"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("KONFIRMASI KATA SANDI:", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                                Spacer(modifier = Modifier.height(4.dp))
                                PortalPixelInput(
                                    value = regConfirmPassword,
                                    onValueChange = { regConfirmPassword = it },
                                    placeholder = "ulangi kata sandi",
                                    isPassword = true,
                                    isPasswordVisible = isRegPasswordVisible,
                                    onTogglePassword = { isRegPasswordVisible = !isRegPasswordVisible },
                                    testTag = "portal_reg_confirm_password"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text("TAG DISCORD (OPSIONAL):", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color(0xFFB0BEC5))
                                Spacer(modifier = Modifier.height(4.dp))
                                PortalPixelInput(
                                    value = regDiscordTag,
                                    onValueChange = { regDiscordTag = it },
                                    placeholder = "User#1234",
                                    testTag = "portal_reg_discord"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                PixelButton(
                                    onClick = {
                                        soundManager.playMenuClick()
                                        if (regPassword != regConfirmPassword) {
                                            isErrorStatus = true
                                            statusMessage = "Kata sandi dan konfirmasi tidak cocok!"
                                            return@PixelButton
                                        }
                                        onRegister(
                                            regUsername,
                                            regEmail,
                                            regPassword,
                                            regDiscordTag.ifBlank { null }
                                        ) { ok, msg ->
                                            if (!ok) {
                                                isErrorStatus = true
                                                statusMessage = msg
                                            }
                                        }
                                    },
                                    backgroundColor = Color(0xFF2E7D32),
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "portal_submit_register_btn"
                                ) {
                                    Text("✨ SELESAIKAN PENDAFTARAN", fontFamily = PressStartFontFamily, fontSize = 8.sp, color = Color.White)
                                }
                            }

                            PortalTab.SAVED_ACCOUNTS -> {
                                Text(
                                    text = "PILIH AKUN TERSIMPAN",
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 8.sp,
                                    color = Color(0xFFFFB74D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 260.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(savedAccounts) { account ->
                                        PixelFrame(
                                            backgroundColor = Color(0xFF1E1730),
                                            borderColor = Color(0xFF513875),
                                            contentPadding = 8.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    soundManager.playMenuClick()
                                                    onSwitchAccount(account.id)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = when (account.provider) {
                                                        "GUEST" -> "👤"
                                                        "DISCORD" -> "🟣"
                                                        "GOOGLE" -> "🔴"
                                                        "FACEBOOK" -> "🔵"
                                                        else -> "⚔️"
                                                    },
                                                    fontSize = 20.sp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = account.username,
                                                        fontFamily = PressStartFontFamily,
                                                        fontSize = 8.sp,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "${account.email} • ${account.provider}",
                                                        fontFamily = Vt323FontFamily,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFFB0BEC5)
                                                    )
                                                }
                                                PixelBadge(
                                                    text = "MASUK",
                                                    backgroundColor = Color(0xFF388E3C)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guest Mode Prominent Action
                PixelFrame(
                    backgroundColor = Color(0xFF110E1C),
                    borderColor = Color(0xFF455A64),
                    contentPadding = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "INGIN MENCOBA TANPA AKUN?",
                            fontFamily = Vt323FontFamily,
                            fontSize = 16.sp,
                            color = Color(0xFF90A4AE)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PixelButton(
                            onClick = {
                                soundManager.playMenuClick()
                                onGuestLogin()
                            },
                            backgroundColor = Color(0xFF37474F),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "portal_guest_mode_btn"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎮 MAIN SEBAGAI TAMU (GUEST MODE)", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PortalPixelInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E0A1A), RoundedCornerShape(2.dp))
            .border(1.dp, Color(0xFF4A3B69), RoundedCornerShape(2.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = Vt323FontFamily,
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = Vt323FontFamily,
                        fontSize = 17.sp,
                        color = Color.White
                    ),
                    visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    cursorBrush = SolidColor(RetroGold),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isPassword && onTogglePassword != null) {
                IconButton(
                    onClick = onTogglePassword,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Sandi",
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
