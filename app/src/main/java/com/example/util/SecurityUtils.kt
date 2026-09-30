package com.example.util

import java.security.MessageDigest

object SecurityUtils {
    private const val SALT = "DungeonId_RetroSalt_2026"

    fun hashPassword(password: String): String {
        val input = "$SALT:$password"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return false
        return hashPassword(password) == storedHash
    }

    fun validateUsername(username: String): String? {
        val trimmed = username.trim()
        if (trimmed.length < 3) return "Username minimal 3 karakter!"
        if (trimmed.length > 20) return "Username maksimal 20 karakter!"
        if (!trimmed.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return "Username hanya boleh huruf, angka, dan garis bawah (_)"
        }
        return null
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isBlank()) return "Email tidak boleh kosong!"
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!trimmed.matches(emailRegex)) return "Format email tidak valid!"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.length < 6) return "Password minimal 6 karakter!"
        return null
    }
}
