package com.notesapp.notesapp

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun SettingsHost(navController: NavHostController) {
    MainNavigationRail(navController, NotesAppRoutes.Settings) {
        SettingsMenu(navController)
    }
}

@Composable
fun SettingsMenu(navController: NavHostController) {
    Text("hello")
}