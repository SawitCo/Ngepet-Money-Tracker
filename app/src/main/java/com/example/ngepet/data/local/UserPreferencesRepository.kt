package com.example.ngepet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    val userNameFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME]
    }

    val hasCompletedOnboardingFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }
}
