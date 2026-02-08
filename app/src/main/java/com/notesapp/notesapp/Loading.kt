package com.notesapp.notesapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Xml
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

@Composable
fun LoadSVGButton(
    modifier: Modifier = Modifier
) {
    //val height = LocalConfiguration.current.screenHeightDp
    //val width = LocalConfiguration.current.screenHeightDp
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)

            file_uri = it

            strokes.clear()
            strokes.addAll(parseSvgToElementsHelper(context, it))
        }
    }
    Box {
        FilledTonalButton(
            onClick = {
                filePickerLauncher.launch(arrayOf("text/plain", "image/svg+xml"))
            },
            modifier = modifier
        ) {
            Text("Load Drawing")
        }
    }
}

fun parseSvgToElementsHelper(
    context: Context,
    uri: Uri
): List<Element> {
    context.contentResolver.openInputStream(uri)?.use { input ->
        return parseSvgToElements(input)
    }
    return emptyList()
}

fun parseSvgToElements(input: InputStream): List<Element> {
    val elements = mutableListOf<Element>()
    val parser = Xml.newPullParser().apply {
        setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        setInput(input, null)
    }

    var eventType = parser.eventType // Get initial state (usually START_DOCUMENT)
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
            when (parser.name) {
                "path" -> {
                    val d = parser.getAttributeValue(null, "d")
                    if (d != null) {
                        parseStrokePath(d)?.let { elements.add(it) }
                    }
                }
                "circle" -> {
                    val r = parser.getAttributeValue(null, "r")
                    val cx = parser.getAttributeValue(null, "cx")
                    val cy = parser.getAttributeValue(null, "cy")
                    val color = parser.getAttributeValue(null, "fill")

                    if (r.isNullOrEmpty() || cx.isNullOrEmpty() || cy.isNullOrEmpty() || color.isNullOrEmpty()) {
                        continue
                    }

                    val penPoint: MutableList<PenPoint> = mutableListOf()
                    penPoint.add(PenPoint(Offset(cx.toFloat(), cy.toFloat()), (r.toFloat() * 2f)))
                    elements.add(Element.Stroke(penPoint, null, color.toComposeColor()))
                }
            }
        }
        eventType = parser.next()
    }

    return elements
}

private fun parseStrokePath(d: String): Element.Stroke? {
    val points = mutableStateListOf<Offset>()
    var i = 0
    var startIndex = -1
    var endIndex = -1
    while (i < d.length) {
        if (startIndex == -1 && endIndex == -1 && d[i] in "MLZmlQq" ) {
            startIndex = i + 1
        } else if (startIndex != -1 && endIndex == -1 && d[i] in "MLZmlzQq") {
            endIndex = i - 1
        } else if (endIndex != -1 && startIndex != -1) {
            val sub = d.substring(startIndex + 1, endIndex)
            val parts = sub.split(" ")
            points.add(Offset(parts[0].toFloat(), parts[1].toFloat()))
            startIndex = endIndex + 2
            endIndex = -1
        } else if (d[i] in "Zz") {
            break;
        }
        i++
    }
    val half = points.size / 2
    val leftEdges: List<Offset> = points.subList(0, half)
    val rightEdges: List<Offset> = points.subList(half, points.size).asReversed()
    val empty: MutableList<PenPoint> = mutableListOf()

    if (leftEdges.isEmpty() or rightEdges.isEmpty()) {
        return null
    }

    val strokePath = Path()

    strokePath.moveTo(leftEdges[0].x, leftEdges[0].y)

    for (i in 1 until leftEdges.size) {
        val mid = (leftEdges[i-1] + leftEdges[i]) / 2f
        strokePath.quadraticTo(leftEdges[i-1].x, leftEdges[i-1].y, mid.x, mid.y)
    }

    strokePath.lineTo(rightEdges.last().x, rightEdges.last().y)

    for (i in rightEdges.size - 2 downTo 0) {
        val mid = (rightEdges[i+1] + rightEdges[i]) / 2f
        strokePath.quadraticTo(rightEdges[i+1].x, rightEdges[i+1].y, mid.x, mid.y)
    }

    strokePath.close()

    return Element.Stroke(empty, StrokeComp(leftEdges, rightEdges, strokePath))
}