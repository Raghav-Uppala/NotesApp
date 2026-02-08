package com.notesapp.notesapp

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DrawingScreen(
    onclick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val uri = file_uri
        if (uri != null) {
            strokes.clear()
            strokes.addAll(parseSvgToElementsHelper(context, uri))
        }
    }
    var reset by remember { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            FilledTonalButton (
                onClick = {
                    reset++
                }
            ) {
                Text("Reset Zoom")
            }
            SaveSVGButton()
            //LoadSVGButton()
            FilledTonalButton(onClick = { lasso = lasso != true }) {
                Text("Lasso")
            }
            FilledTonalButton (onClick = onclick) {
                Text("Menu")
            }
        }
        DrawingCanvas(
            reset,
            Modifier.fillMaxSize()
        )
    }
}