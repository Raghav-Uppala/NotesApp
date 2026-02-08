package com.notesapp.notesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import android.net.Uri
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainCompose()
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

@Composable
fun MainCompose(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
    RouteController(modifier = modifier)
}