package com.notesapp.notesapp

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "user_prefs")
val USER_PREF_KEY = stringPreferencesKey("user_pref")

class DataStoreManager(private val context: Context) {
    companion object {
        private val STYLUS_ONLY_KEY = booleanPreferencesKey("stylus")
        private val SHOW_SAVE = booleanPreferencesKey("showSave")
        private val DEFAULT_ZOOM = floatPreferencesKey("defaultZoom")
    }

    val settingsFlow: Flow<Settings> = context.dataStore.data
        .map { prefs ->
            Settings(
                stylusOnly = prefs[STYLUS_ONLY_KEY] ?: true,
                showSave = prefs[SHOW_SAVE] ?: false,
                defaultZoom = prefs[DEFAULT_ZOOM] ?: 1f,
            )
        }

    suspend fun setStylusOnly(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_PREF_KEY)
        }
        context.dataStore.edit { prefs ->
            prefs[STYLUS_ONLY_KEY] = value
        }
    }
    suspend fun setShowSave(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SHOW_SAVE] = value
        }
    }
    suspend fun setDefaultZoom(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_ZOOM] = value
        }
    }
}


class SettingsViewModel(private val repository: DataStoreManager) : ViewModel() {
    val settingsState = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Settings()
    )

    fun updateStylusOnly(value: Boolean) {
        viewModelScope.launch {
            repository.setStylusOnly(value)
        }
    }
    fun updateShowSave(value: Boolean) {
        viewModelScope.launch {
            repository.setShowSave(value)
        }
    }

    fun updateDefaultZoom(value: Float) {
        viewModelScope.launch {
            repository.setDefaultZoom(value)
        }
    }
}

val LocalSettingsViewModel = staticCompositionLocalOf<SettingsViewModel> {
    error("SettingsViewModel not found")
}