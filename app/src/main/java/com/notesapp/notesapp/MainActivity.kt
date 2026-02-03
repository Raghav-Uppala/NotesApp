package com.notesapp.notesapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Surface {
            ZoomScreen()
        } }
    }
}
fun String.toComposeColor(): Color {
    return Color(this.toColorInt())
}

fun pressureToThickness(pressure: Float): Float {
    return ((pressure)) * fontSize
}

@Composable
fun ZoomScreen() {
    var reset by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        DrawingScreen(
            reset,
            Modifier.fillMaxSize()
        )
        Button(
            onClick = {
                reset++
            }
        ) {
            Text("Reset Zoom")
        }
    }
}
