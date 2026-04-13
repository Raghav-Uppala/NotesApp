package com.notesapp.notesapp

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


@Composable
fun SettingsHost(navController: NavHostController) {
    MainNavigationRail(navController, NotesAppRoutes.Settings) {
        SettingsMenu(navController)
    }
}

@Composable
fun SettingsMenu(navController: NavHostController) {
    val viewModel = LocalSettingsViewModel.current
    val settings by viewModel.settingsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 66.dp, horizontal = 16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(bottom = 16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween
        ) {
            Text("Stylus Only Mode")
            Switch(
                checked = settings.stylusOnly,
                onCheckedChange = {
                    viewModel.updateStylusOnly(it)
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween
        ) {
            Text("Show Save Button in Canvas")
            Switch(
                checked = settings.showSave,
                onCheckedChange = {
                    viewModel.updateShowSave(it)
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Default Zoom")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${(settings.defaultZoom*100).toInt()}%")
                Spacer(Modifier.width(20.dp))
                Slider(
                    value = settings.defaultZoom/2,
                    onValueChange = {viewModel.updateDefaultZoom(it*2)},
                    steps = 14,
                    valueRange = 0.25f ..1f,
                    modifier = Modifier
                        .width(300.dp)
                )
            }
        }
    }
}
