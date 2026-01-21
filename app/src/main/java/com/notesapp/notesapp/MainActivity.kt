package com.notesapp.notesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Surface {
            DrawingScreen()
            SaveSVGButton()
            LoadSVGButton()
        } }
    }
}
fun String.toComposeColor(): Color {
    return Color(this.toColorInt())
}

fun pressureToThickness(pressure: Float): Float {
    return (pressure + 0.5f) * fontSize
}