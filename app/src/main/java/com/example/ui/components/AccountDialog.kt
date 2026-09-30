package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily

enum class AuthTab {
    LOGIN,
    REGISTER
}

enum class LoginMethod {
    USERNAME_OR_EMAIL,
    DISCORD
}

@Composable
fun AccountDialog(
    currentAccount: UserAccountEntity?,
    allAccounts: List<UserAccountEntity>,
    onDismiss: () -> Unit,
    onLoginCredentials: (identifier: String, password: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    onRegister: (username: String, email: String, password: String, discordTag: String?, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _, _, _ -> },
    onLoginDiscord: (discordTag: String, email: String?, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    onLinkDiscord: (discordTag: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    onLoginGoogle: (username: String, email: String) -> Unit = { _, _ -> },
    onLoginFacebook: (username: String, email: String) -> Unit = { _, _ -> },
    onLoginGuest: () -> Unit = {},
    onLinkProvider: (provider: String) -> Unit = {},
    onSwitchAccount: (accountId: String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(AuthTab.LOGIN) }
    var loginMethod by remember { mutableStateOf(LoginMethod.USERNAME_OR_EMAIL) }

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var discordTagInput by remember { mutableStateOf(currentAccount?.discordId ?: "") }
    var discordEmailInput by remember { mutableStateOf(currentAccount?.email ?: "") }

    // Register Form State
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regDiscordTag by remember { mutableStateOf("") }

    // Link Discord to current account
    var linkDiscordInput by remember { mutableStateOf("") }
    var showLinkDiscordField by remember { mutableStateOf(false) }

    // Feedback
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val activeProvider = currentAccount?.provider ?: "GUEST"
    val isGuest = activeProvider == "GUEST"
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(
            backgroundColor = Color(0xFF131022),
            borderColor = Color(0xFFFFC107),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .testTag("account_dialog_frame")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SISTEM AKUN [AUTH]",
                            fontFamily = PressStartFontFamily,
                            fontSize = 10.sp,
                            color = RetroGold
                        )
                    }

                    PixelButton(
                        onClick = onDismiss,
                        backgroundColor = Color(0xFF424242),
                        testTag = "account_dialog_close_button"
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Account Profile Card
                PixelFrame(
                    backgroundColor = Color(0xFF201836),
                    borderColor = getProviderColor(activeProvider),
                    contentPadding = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(getProviderEmoji(activeProvider), fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentAccount?.username ?: "Petualang Tamu",
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = currentAccount?.email ?: "guest@rpgrealm.local",
                                    fontFamily = Vt323FontFamily,
                                    fontSize = 14.sp,
                                    color = Color(0xFFB0BEC5)
                                )
                            }
                            PixelBadge(
                                text = activeProvider,
                                backgroundColor = getProviderColor(activeProvider)
                            )
                        }

                        // Discord Linked Info
                        if (!currentAccount?.discordId.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2C2442), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🟣", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DISCORD: ${currentAccount?.discordId}",
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 7.sp,
                                    color = Color(0xFFB388FF)
                                )
                            }
                        } else if (!isGuest && currentAccount != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            if (!showLinkDiscordField) {
                                PixelButton(
                                    onClick = { showLinkDiscordField = true },
                                    backgroundColor = Color(0xFF5865F2).copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "btn_open_link_discord"
                                ) {
                                    Text(
                                        text = "+ TAUTKAN DISCORD KE AKUN INI",
                                        fontFamily = PressStartFontFamily,
                                        fontSize = 7.sp,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        PixelInputField(
                                            label = "TAG DISCORD:",
                                            value = linkDiscordInput,
                                            onValueChange = { linkDiscordInput = it },
                                            placeholder = "User#1234"
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PixelButton(
                                        onClick = {
                                            if (linkDiscordInput.isNotBlank()) {
                                                onLinkDiscord(linkDiscordInput) { success, msg ->
                                                    if (success) {
                                                        successMessage = msg
                                                        errorMessage = null
                                                        showLinkDiscordField = false
                                                    } else {
                                                        errorMessage = msg
                                                    }
                                                }
                                            }
                                        },
                                        backgroundColor = Color(0xFF2E7D32)
                                    ) {
                                        Text("SIMPAN", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Logout button
                        if (!isGuest) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                PixelButton(
                                    onClick = onLogout,
                                    backgroundColor = Color(0xFFB71C1C),
                                    testTag = "account_btn_logout"
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("LOGOUT", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Error / Success Banner
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4A1015), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ ${errorMessage}",
                            fontFamily = Vt323FontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFFFCDD2)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B4324), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFF43A047), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "✅ ${successMessage}",
                            fontFamily = Vt323FontFamily,
                            fontSize = 15.sp,
                            color = Color(0xFFC8E6C9)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Auth Mode Tabs: [LOGIN] vs [REGISTER]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PixelButton(
                        onClick = {
                            currentTab = AuthTab.LOGIN
                            errorMessage = null
                            successMessage = null
                        },
                        backgroundColor = if (currentTab == AuthTab.LOGIN) Color(0xFF6A1B9A) else Color(0xFF2C2442),
                        modifier = Modifier.weight(1f),
                        testTag = "tab_login"
                    ) {
                        Text(
                            text = "🔑 MASUK (LOGIN)",
                            fontFamily = PressStartFontFamily,
                            fontSize = 8.sp,
                            color = if (currentTab == AuthTab.LOGIN) RetroGold else Color.Gray
                        )
                    }

                    PixelButton(
                        onClick = {
                            currentTab = AuthTab.REGISTER
                            errorMessage = null
                            successMessage = null
                        },
                        backgroundColor = if (currentTab == AuthTab.REGISTER) Color(0xFF2E7D32) else Color(0xFF2C2442),
                        modifier = Modifier.weight(1f),
                        testTag = "tab_register"
                    ) {
                        Text(
                            text = "📝 DAFTAR (REGISTER)",
                            fontFamily = PressStartFontFamily,
                            fontSize = 8.sp,
                            color = if (currentTab == AuthTab.REGISTER) RetroGold else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TAB 1: LOGIN FORM
                if (currentTab == AuthTab.LOGIN) {
                    // Sub-method selector: [Username/Email] vs [Discord]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PixelButton(
                            onClick = {
                                loginMethod = LoginMethod.USERNAME_OR_EMAIL
                                errorMessage = null
                            },
                            backgroundColor = if (loginMethod == LoginMethod.USERNAME_OR_EMAIL) Color(0xFF3949AB) else Color(0xFF1E1A33),
                            modifier = Modifier.weight(1f),
                            testTag = "subtab_username_email"
                        ) {
                            Text(
                                text = "👤/✉️ EMAIL/USER",
                                fontFamily = PressStartFontFamily,
                                fontSize = 7.sp,
                                color = Color.White
                            )
                        }

                        PixelButton(
                            onClick = {
                                loginMethod = LoginMethod.DISCORD
                                errorMessage = null
                            },
                            backgroundColor = if (loginMethod == LoginMethod.DISCORD) Color(0xFF5865F2) else Color(0xFF1E1A33),
                            modifier = Modifier.weight(1f),
                            testTag = "subtab_discord"
                        ) {
                            Text(
                                text = "🟣 DISCORD",
                                fontFamily = PressStartFontFamily,
                                fontSize = 7.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (loginMethod == LoginMethod.USERNAME_OR_EMAIL) {
                        // Username or Email + Password login
                        PixelInputField(
                            label = "USERNAME ATAU EMAIL:",
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            placeholder = "Masukkan username atau email...",
                            testTag = "input_login_identifier"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        PixelInputField(
                            label = "PASSWORD:",
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            placeholder = "Kata sandi...",
                            isPassword = true,
                            testTag = "input_login_password"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        PixelButton(
                            onClick = {
                                if (loginIdentifier.isBlank()) {
                                    errorMessage = "Username atau email tidak boleh kosong!"
                                    return@PixelButton
                                }
                                if (loginPassword.isBlank()) {
                                    errorMessage = "Password tidak boleh kosong!"
                                    return@PixelButton
                                }
                                onLoginCredentials(loginIdentifier, loginPassword) { success, msg ->
                                    if (success) {
                                        successMessage = msg
                                        errorMessage = null
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            backgroundColor = Color(0xFF1E88E5),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_submit_login_credentials"
                        ) {
                            Text(
                                text = "🔑 MASUK KE DUNGEON",
                                fontFamily = PressStartFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Discord Login
                        PixelInputField(
                            label = "DISCORD USERNAME ATAU TAG:",
                            value = discordTagInput,
                            onValueChange = { discordTagInput = it },
                            placeholder = "cth: GamerDungeon#1337",
                            testTag = "input_login_discord_tag"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        PixelInputField(
                            label = "EMAIL DISCORD (OPSIONAL):",
                            value = discordEmailInput,
                            onValueChange = { discordEmailInput = it },
                            placeholder = "email@discord.com (opsional)",
                            testTag = "input_login_discord_email"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        PixelButton(
                            onClick = {
                                if (discordTagInput.isBlank()) {
                                    errorMessage = "Tag atau username Discord wajib diisi!"
                                    return@PixelButton
                                }
                                onLoginDiscord(discordTagInput, discordEmailInput.ifBlank { null }) { success, msg ->
                                    if (success) {
                                        successMessage = msg
                                        errorMessage = null
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            backgroundColor = Color(0xFF5865F2),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_submit_login_discord"
                        ) {
                            Text(
                                text = "🟣 MASUK DENGAN DISCORD",
                                fontFamily = PressStartFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Social & Guest Quick Logins
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PixelButton(
                            onClick = onLoginGuest,
                            backgroundColor = Color(0xFF455A64),
                            modifier = Modifier.weight(1f),
                            testTag = "login_btn_guest"
                        ) {
                            Text("👤 MODE GUEST", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                        }

                        PixelButton(
                            onClick = {
                                val name = loginIdentifier.ifBlank { "Google Petualang" }
                                val email = if (loginIdentifier.contains("@")) loginIdentifier else "petualang.google@gmail.com"
                                onLoginGoogle(name, email)
                            },
                            backgroundColor = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f),
                            testTag = "login_btn_google"
                        ) {
                            Text("🔴 GOOGLE", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                        }
                    }
                }

                // TAB 2: REGISTER FORM
                if (currentTab == AuthTab.REGISTER) {
                    PixelInputField(
                        label = "USERNAME (MIN 3 KARAKTER):",
                        value = regUsername,
                        onValueChange = { regUsername = it },
                        placeholder = "Pilih username unik...",
                        testTag = "input_reg_username"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    PixelInputField(
                        label = "EMAIL RESMI:",
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        placeholder = "kamu@domain.com",
                        keyboardType = KeyboardType.Email,
                        testTag = "input_reg_email"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    PixelInputField(
                        label = "PASSWORD (MIN 6 KARAKTER):",
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        placeholder = "Masukkan kata sandi...",
                        isPassword = true,
                        testTag = "input_reg_password"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    PixelInputField(
                        label = "KONFIRMASI PASSWORD:",
                        value = regConfirmPassword,
                        onValueChange = { regConfirmPassword = it },
                        placeholder = "Ulangi kata sandi...",
                        isPassword = true,
                        testTag = "input_reg_confirm_password"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    PixelInputField(
                        label = "TAG DISCORD (OPSIONAL):",
                        value = regDiscordTag,
                        onValueChange = { regDiscordTag = it },
                        placeholder = "Gamer#1234 (opsional)",
                        testTag = "input_reg_discord_tag"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PixelButton(
                        onClick = {
                            if (regUsername.trim().length < 3) {
                                errorMessage = "Username minimal 3 karakter!"
                                return@PixelButton
                            }
                            if (regEmail.isBlank()) {
                                errorMessage = "Email wajib diisi!"
                                return@PixelButton
                            }
                            if (regPassword.length < 6) {
                                errorMessage = "Password minimal 6 karakter!"
                                return@PixelButton
                            }
                            if (regPassword != regConfirmPassword) {
                                errorMessage = "Konfirmasi password tidak cocok!"
                                return@PixelButton
                            }

                            onRegister(
                                regUsername.trim(),
                                regEmail.trim(),
                                regPassword,
                                regDiscordTag.trim().ifBlank { null }
                            ) { success, msg ->
                                if (success) {
                                    successMessage = msg
                                    errorMessage = null
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        backgroundColor = Color(0xFF2E7D32),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_submit_register"
                    ) {
                        Text(
                            text = "📝 DAFTAR AKUN BARU",
                            fontFamily = PressStartFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Switcher List (Stored in Database)
                if (allAccounts.isNotEmpty()) {
                    Text(
                        text = "DATABASE AKUN TERSIMPAN (${allAccounts.size}):",
                        fontFamily = PressStartFontFamily,
                        fontSize = 8.sp,
                        color = RetroGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allAccounts) { account ->
                            val isActive = account.id == currentAccount?.id
                            PixelFrame(
                                backgroundColor = if (isActive) Color(0xFF2A2342) else Color(0xFF19142A),
                                borderColor = if (isActive) Color(0xFFFFD54F) else Color(0xFF382B54),
                                contentPadding = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSwitchAccount(account.id) }
                                    .testTag("switch_account_${account.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(getProviderEmoji(account.provider), fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = account.username,
                                            fontFamily = PressStartFontFamily,
                                            fontSize = 7.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${account.email}${if (!account.discordId.isNullOrBlank()) " | 🟣 ${account.discordId}" else ""}",
                                            fontFamily = Vt323FontFamily,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    if (isActive) {
                                        PixelBadge(text = "AKTIF", backgroundColor = Color(0xFF388E3C))
                                    } else {
                                        Text("PILIH", fontFamily = PressStartFontFamily, fontSize = 6.sp, color = RetroGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PixelInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        Text(label, fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFB0BEC5))
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C0A15))
                .border(1.dp, Color(0xFF4A3B69))
                .padding(horizontal = 8.dp, vertical = 6.dp)
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
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontFamily = Vt323FontFamily,
                            fontSize = 15.sp,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(RetroGold),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isPassword) KeyboardType.Password else keyboardType
                        ),
                        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isPassword) {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Sembunyikan password" else "Lihat password",
                            tint = Color(0xFFB0BEC5),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SocialLoginButton(
    providerName: String,
    emoji: String,
    backgroundColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    PixelButton(
        onClick = onClick,
        backgroundColor = backgroundColor,
        modifier = Modifier.fillMaxWidth(),
        testTag = testTag
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "LOGIN DENGAN $providerName",
                fontFamily = PressStartFontFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

fun getProviderEmoji(provider: String): String {
    return when (provider.uppercase()) {
        "GOOGLE" -> "🔴"
        "FACEBOOK" -> "🔵"
        "DISCORD" -> "🟣"
        "LOCAL", "EMAIL", "USERNAME" -> "🛡️"
        else -> "👤"
    }
}

fun getProviderColor(provider: String): Color {
    return when (provider.uppercase()) {
        "GOOGLE" -> Color(0xFFD32F2F)
        "FACEBOOK" -> Color(0xFF1976D2)
        "DISCORD" -> Color(0xFF5865F2)
        "LOCAL", "EMAIL", "USERNAME" -> Color(0xFF6A1B9A)
        else -> Color(0xFF607D8B)
    }
}
