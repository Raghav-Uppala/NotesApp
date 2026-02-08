package com.notesapp.notesapp

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import androidx.documentfile.provider.DocumentFile

fun String.toComposeColor(): Color {
    return Color(this.toColorInt())
}

fun Color.toHexCode(): String {
    val argb = this.toArgb()
    return String.format("#%06X", argb)
}

fun pressureToThickness(pressure: Float): Float {
    return ((pressure)) * fontSize
}

fun listNotes(context: Context, rootUri: Uri): List<DocumentFile> {
    val root = DocumentFile.fromTreeUri(context, rootUri)
    return root?.listFiles()?.toList() ?: emptyList()
}