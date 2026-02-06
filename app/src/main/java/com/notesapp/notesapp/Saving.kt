package com.notesapp.notesapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedOutputStream
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SaveSVGButton(
    modifier: Modifier = Modifier
) {
    val height = LocalConfiguration.current.screenHeightDp
    val width = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/svg+xml")
    ) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)

            file_uri = it

            scope.launch {
                saveSVGDataToUri(context, it, strokes.toList(), height, width)
            }
        }
    }

// To trigger it (e.g., from a button):
    Button(
        onClick = {
            val uri = file_uri
            if (uri != null) {
                scope.launch {
                    saveSVGDataToUri(context, uri, strokes.toList(), height, width)
                }            } else {
                filePickerLauncher.launch("my_drawing.svg")
            }
        },
        modifier = modifier
    ) {
        Text("Save Drawing")
    }
}

suspend fun saveSVGDataToUri(context: Context, uri: Uri, paths: List<Element>, height: Int, width: Int) {
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { raw ->
                raw.bufferedWriter(Charsets.UTF_8).use { outputStream ->
                    outputStream.write("<svg height=\"$height\" width=\"$width\" xmlns=\"http://www.w3.org/2000/svg\"> ")
                    for (i in 0 until paths.size) {
                        when (val elem = paths[i]) {
                            is Element.Stroke -> {
                                when {
                                    elem.isDot -> {
                                        if (elem.rawPoints.isEmpty()) continue

                                        outputStream.write("<circle r=\"${elem.rawPoints[0].thickness / 2f}\" cx=\"${elem.rawPoints[0].position.x}\" cy=\"${elem.rawPoints[0].position.y}\" fill=\"${elem.color.toHexCode()}\" />")
                                    }

                                    elem.isPath -> {
                                        val computed = elem.computed ?: continue
                                        val left = computed.leftEdges
                                        val right = computed.rightEdges
                                        if (left.isEmpty()) continue
                                        if (right.isEmpty()) continue

                                        val pathData = StringBuilder()

                                        val first = left[0]
                                        pathData.append("M ${first.x} ${first.y} ")

                                        for (p in left) {
                                            pathData.append("L ${p.x} ${p.y} ")
                                        }

                                        for (i in right.indices.reversed()) {
                                            val p = right[i]
                                            pathData.append("L ${p.x} ${p.y} ")
                                        }

                                        pathData.append("Z")

                                        outputStream.write("<path fill=\"${elem.color.toHexCode()}\" stroke=\"none\" d=\"$pathData\"/>")
                                    }
                                }
                            }
                        }
                    }
                    outputStream.write("</svg>")
                    outputStream.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}