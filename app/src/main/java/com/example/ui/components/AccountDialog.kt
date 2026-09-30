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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.UserAccountEntity
import com.example.ui.theme.PressStartFontFamily
import com.example.ui.theme.Vt323FontFamily

@Composable
fun AccountDialog(
    currentAccount: UserAccountEntity?,
    allAccounts: List<UserAccountEntity>,
    onDismiss: () -> Unit,
    onLoginGoogle: (username: String, email: String) -> Unit,
    onLoginFacebook: (username: String, email: String) -> Unit,
    onLoginDiscord: (username: String, email: String) -> Unit,
    onLoginGuest: () -> Unit,
    onLinkProvider: (provider: String) -> Unit,
    onSwitchAccount: (accountId: String) -> Unit,
    onLogout: () -> Unit
) {
    var usernameInput by remember { mutableStateOf(currentAccount?.username ?: "") }
    var emailInput by remember { mutableStateOf(currentAccount?.email ?: "") }

    val activeProvider = currentAccount?.provider ?: "GUEST"
    val linkedList = currentAccount?.linkedProviders?.split(",")?.filter { it.isNotBlank() } ?: listOf(activeProvider)

    Dialog(onDismissRequest = onDismiss) {
        PixelFrame(
            backgroundColor = Color(0xFF131022),
            borderColor = Color(0xFFFFC107),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("account_dialog_frame")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
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
                            text = "AKUN PEMAIN [AUTH]",
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
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Account Summary Card
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
                            Text(getProviderEmoji(activeProvider), fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentAccount?.username ?: "Petualang Guest",
                                    fontFamily = PressStartFontFamily,
                                    fontSize = 10.sp,
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // Linked Accounts Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TERHUBUNG: ",
                                fontFamily = PressStartFontFamily,
                                fontSize = 6.sp,
                                color = Color(0xFFFFD54F)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("GOOGLE", "FACEBOOK", "DISCORD").forEach { provider ->
                                    val isLinked = linkedList.contains(provider)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isLinked) getProviderColor(provider) else Color(0xFF2C2442),
                                                RoundedCornerShape(3.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${getProviderEmoji(provider)} ${if (isLinked) "✓" else "+"}",
                                            fontFamily = PressStartFontFamily,
                                            fontSize = 6.sp,
                                            color = if (isLinked) Color.White else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Input Custom Name & Email
                Text(
                    text = "LOGIN DENGAN AKUN SOSIAL:",
                    fontFamily = PressStartFontFamily,
                    fontSize = 8.sp,
                    color = RetroGold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PixelInputField(
                        label = "NAMA AKUN:",
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        placeholder = "Masukkan username..."
                    )

                    PixelInputField(
                        label = "EMAIL AKUN:",
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = "email@domain.com"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Provider Login Buttons
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SocialLoginButton(
                        providerName = "Google",
                        emoji = "🔴",
                        backgroundColor = Color(0xFFD32F2F),
                        testTag = "login_btn_google",
                        onClick = {
                            val name = usernameInput.ifBlank { "Google Petualang" }
                            val email = emailInput.ifBlank { "petualang.google@gmail.com" }
                            onLoginGoogle(name, email)
                        }
                    )

                    SocialLoginButton(
                        providerName = "Facebook",
                        emoji = "🔵",
                        backgroundColor = Color(0xFF1976D2),
                        testTag = "login_btn_facebook",
                        onClick = {
                            val name = usernameInput.ifBlank { "FB Petualang" }
                            val email = emailInput.ifBlank { "petualang.fb@facebook.com" }
                            onLoginFacebook(name, email)
                        }
                    )

                    SocialLoginButton(
                        providerName = "Discord",
                        emoji = "🟣",
                        backgroundColor = Color(0xFF5865F2),
                        testTag = "login_btn_discord",
                        onClick = {
                            val name = usernameInput.ifBlank { "Discord#1337" }
                            val email = emailInput.ifBlank { "petualang.discord@discord.com" }
                            onLoginDiscord(name, email)
                        }
                    )

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
                            onClick = onLogout,
                            backgroundColor = Color(0xFFC62828),
                            modifier = Modifier.weight(1f),
                            testTag = "account_btn_logout"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("LOGOUT", fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Link Providers
                Text(
                    text = "HUBUNGKAN AKUN SOSIAL:",
                    fontFamily = PressStartFontFamily,
                    fontSize = 8.sp,
                    color = RetroGold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("GOOGLE", "FACEBOOK", "DISCORD").forEach { provider ->
                        val isLinked = linkedList.contains(provider)
                        PixelButton(
                            onClick = { if (!isLinked) onLinkProvider(provider) },
                            backgroundColor = if (isLinked) Color(0xFF2E7D32) else getProviderColor(provider).copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f),
                            testTag = "link_btn_$provider"
                        ) {
                            Text(
                                text = "${getProviderEmoji(provider)} ${if (isLinked) "TERDAPFTAR" else "+ HUBUNG"}",
                                fontFamily = PressStartFontFamily,
                                fontSize = 6.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account Switcher List
                if (allAccounts.size > 1) {
                    Text(
                        text = "DAFTAR AKUN TERSIMPAN:",
                        fontFamily = PressStartFontFamily,
                        fontSize = 8.sp,
                        color = RetroGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.height(100.dp),
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
                                        Text(account.username, fontFamily = PressStartFontFamily, fontSize = 7.sp, color = Color.White)
                                        Text(account.email, fontFamily = Vt323FontFamily, fontSize = 12.sp, color = Color.Gray)
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
    placeholder: String
) {
    Column {
        Text(label, fontFamily = PressStartFontFamily, fontSize = 6.sp, color = Color(0xFFB0BEC5))
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C0A15))
                .border(1.dp, Color(0xFF4A3B69))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
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
                modifier = Modifier.fillMaxWidth()
            )
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
        else -> "👤"
    }
}

fun getProviderColor(provider: String): Color {
    return when (provider.uppercase()) {
        "GOOGLE" -> Color(0xFFD32F2F)
        "FACEBOOK" -> Color(0xFF1976D2)
        "DISCORD" -> Color(0xFF5865F2)
        else -> Color(0xFF607D8B)
    }
}
