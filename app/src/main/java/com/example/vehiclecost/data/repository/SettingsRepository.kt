package com.example.vehiclecost.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        val PURCHASE_DATE_KEY = longPreferencesKey("purchase_date")
        val CAR_PHOTO_URI_KEY = stringPreferencesKey("car_photo_uri")
        val REMINDER_ENABLED_KEY = booleanPreferencesKey("reminder_enabled")
    }

    val purchaseDateFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[PURCHASE_DATE_KEY]
    }

    val carPhotoUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CAR_PHOTO_URI_KEY]
    }

    val reminderEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_ENABLED_KEY] ?: false
    }

    suspend fun savePurchaseDate(dateMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[PURCHASE_DATE_KEY] = dateMillis
        }
    }

    suspend fun saveCarPhotoUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[CAR_PHOTO_URI_KEY] = uri
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_ENABLED_KEY] = enabled
        }
    }
}
