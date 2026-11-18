package com.example.data

import java.security.MessageDigest
import kotlin.random.Random

object SecurityUtil {
    private const val SALT = "SOM_TREVEL_LUXURY_SALT_V1"

    fun hashPassword(rawPassword: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$SALT:$rawPassword".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generatePnr(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return "ST" + (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    fun generateReference(prefix: String): String {
        val digits = (10000..99999).random()
        return "$prefix-$digits"
    }
}
