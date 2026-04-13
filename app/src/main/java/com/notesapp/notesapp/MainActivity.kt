package com.notesapp.notesapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import android.content.Context
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStoreManager = DataStoreManager(applicationContext)

        val settingsViewModel = SettingsViewModel(dataStoreManager)

        setContent {
            AppTheme {
                CompositionLocalProvider(LocalSettingsViewModel provides settingsViewModel) {
                    Surface(
                        modifier = Modifier
                            .safeDrawingPadding()
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainCompose()
                    }
                }
            }
        }
    }
}
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(), // replace later
        content = content
    )
}

@SuppressLint("ContextCastToActivity")
@Composable
fun MainCompose(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val statusBarLight = MaterialTheme.colorScheme.surfaceDim.toArgb()
    val statusBarDark = MaterialTheme.colorScheme.surfaceDim.toArgb()
    val navigationBarLight = MaterialTheme.colorScheme.surfaceDim.toArgb()
    val navigationBarDark = MaterialTheme.colorScheme.surfaceDim.toArgb()
    val isDarkMode = isSystemInDarkTheme()
    val lcontext = LocalContext.current as ComponentActivity

    DisposableEffect(isDarkMode) {
        lcontext.enableEdgeToEdge(
            statusBarStyle = if (!isDarkMode) {
                SystemBarStyle.light(
                    statusBarLight,
                    statusBarDark
                )
            } else {
                SystemBarStyle.dark(
                    statusBarDark
                )
            },
            navigationBarStyle = if(!isDarkMode){
                SystemBarStyle.light(
                    navigationBarLight,
                    navigationBarDark
                )
            } else {
                SystemBarStyle.dark(navigationBarDark)
            }
        )

        onDispose { }
    }
    LaunchedEffect(Unit) {
        val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val savedUriString = sharedPref.getString("root_folder_uri", null)

        if (savedUriString != null) {
            val savedUri = savedUriString.toUri()

            // Verify we still have permission (user might have deleted the folder)
            val hasPermission = context.contentResolver.persistedUriPermissions.any {
                it.uri == savedUri
            }

            if (hasPermission) {
                rootFolderUri = savedUri
            }
        }
    }
    RouteController(
        modifier = modifier
            .padding()
    )
}