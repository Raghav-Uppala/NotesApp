package com.notesapp.notesapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun MainNavigationRail(navController: NavHostController, currentDest: NotesAppRoutes, content: @Composable () -> Unit) {
    val startDestination = NotesAppRoutes.Menu

    Scaffold(modifier = Modifier) { contentPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            NavigationRail(modifier = Modifier.padding(contentPadding)) {
                NavigationRailItem(
                    selected = currentDest.name == NotesAppRoutes.Menu.name,
                    onClick = {
                        navController.navigate(route = NotesAppRoutes.Menu.name)
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "TODO()"
                        )
                    },
                )
                NavigationRailItem(
                    selected = currentDest.name == NotesAppRoutes.Settings.name,
                    onClick = {
                        navController.navigate(route = NotesAppRoutes.Settings.name)
                    },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "TODO()"
                        )
                    },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                content()
            }
        }
    }
}