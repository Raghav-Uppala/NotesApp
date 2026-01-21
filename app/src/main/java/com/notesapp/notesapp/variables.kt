package com.notesapp.notesapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

val strokes = mutableStateListOf<SnapshotStateList<PenPoint>>()
val primaryColorHex = "#FFFFFF"
val primaryColor = primaryColorHex.toComposeColor()

val samplingThreshold = 4f
val eraserThreshold = 10f

val fontSize = 5f
val fontSizeDot = fontSize/2
val fontSizeLine = fontSize