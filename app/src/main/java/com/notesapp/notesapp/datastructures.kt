package com.notesapp.notesapp

import androidx.compose.ui.geometry.Offset

data class PenPoint(
    val position: Offset,
    val thickness: Float,
    val pressure: Float,
)