package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "som_trevel_prefs")

data class AppPreferences(
    val darkMode: Boolean = false,
    val languageCode: String = "en",
    val currencyCode: String = "USD",
    val currentUserId: Long? = null,
    val newsletterSubscribedEmail: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language_code")
        val CURRENCY = stringPreferencesKey("currency_code")
        val CURRENT_USER_ID = longPreferencesKey("current_user_id")
        val NEWSLETTER_EMAIL = stringPreferencesKey("newsletter_email")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        val rawUid = prefs[Keys.CURRENT_USER_ID] ?: -1L
        AppPreferences(
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            languageCode = prefs[Keys.LANGUAGE] ?: "en",
            currencyCode = prefs[Keys.CURRENCY] ?: "USD",
            currentUserId = if (rawUid > 0L) rawUid else null,
            newsletterSubscribedEmail = prefs[Keys.NEWSLETTER_EMAIL] ?: ""
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = code }
    }

    suspend fun setCurrency(code: String) {
        context.dataStore.edit { it[Keys.CURRENCY] = code }
    }

    suspend fun setCurrentUserId(userId: Long?) {
        context.dataStore.edit {
            if (userId == null) it.remove(Keys.CURRENT_USER_ID)
            else it[Keys.CURRENT_USER_ID] = userId
        }
    }

    suspend fun setNewsletterEmail(email: String) {
        context.dataStore.edit { it[Keys.NEWSLETTER_EMAIL] = email }
    }
}
