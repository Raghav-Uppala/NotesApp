package com.notesapp.notesapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset

var strokes = mutableStateListOf<SnapshotStateList<PenPoint>>()
val primaryColorHex = "#FFFFFF"
val backgroundColorHex = "#000000"
val primaryColor = primaryColorHex.toComposeColor()
val backgroundColor = backgroundColorHex.toComposeColor()
val samplingThreshold = 4f
val eraserThreshold = 10f

val fontSize = 5f
val fontSizeDot = fontSize/2
val fontSizeLine = fontSize