package com.notesapp.notesapp

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class NotesAppRoutes() {
    Menu,
    Canvas
}

@Composable
fun RouteController(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NotesAppRoutes.Menu.name,
        modifier = modifier
    ) {
        composable(route = NotesAppRoutes.Menu.name) {
            MainMenu({
                navController.navigate(NotesAppRoutes.Canvas.name)
            })
        }
        composable(route = NotesAppRoutes.Canvas.name) {
            DrawingScreen({
                navController.navigate(NotesAppRoutes.Menu.name)
            })
        }
    }
}