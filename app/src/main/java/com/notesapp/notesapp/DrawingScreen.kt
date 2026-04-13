package com.notesapp.notesapp

import android.content.Intent
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle

@Composable
fun DrawingScreen(
    onclick: () -> Unit
) {
    val viewModel = LocalSettingsViewModel.current
    val settings by viewModel.settingsState.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val uri = file_uri
        if (uri != null) {
            strokes.clear()
            strokes.addAll(parseSvgToElementsHelper(context, uri))
        }
    }

    val metrics = Resources.getSystem().displayMetrics
    val lWidth  = (minOf(metrics.widthPixels, metrics.heightPixels) / metrics.density)
    val lHeight = (lWidth * 1.414f)
    val borderColor = MaterialTheme.colorScheme.surfaceBright
    var reset by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(MaterialTheme.colorScheme.surface)
                .drawBehind {
                    val strokeWidth = 1f * density // 1dp thick
                    val y = size.height - strokeWidth / 2

                    drawLine(
                        color = borderColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
            horizontalArrangement = Arrangement
                .spacedBy(
                    5.dp,
                    alignment = Alignment.CenterHorizontally
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolBarTool(
                onclick = {lasso = lasso != true},
                icon = { color ->
                    Icon(
                        painter = painterResource(id = R.drawable.lasso),
                        contentDescription = "Localized description",
                        tint = color
                    )
                }
            )
            ToolBarTool(
                toggleable = false,
                onclick = {reset++},
                icon = { color ->
                    Icon(
                        painter = painterResource(id = R.drawable.reset_zoom),
                        contentDescription = "Localized description",
                        tint = color
                    )
                }
            )
            ToolBarTool(
                onclick = {Log.d("d", "d")},
                icon = { color ->
                    Icon(
                        painter = painterResource(id = R.drawable.eraser),
                        contentDescription = "Localized description",
                        tint = color
                    )
                }
            )
            ToolBarTool(
                toggleable = false,
                onclick = onclick,
                icon = { color ->
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "go home",
                        tint = color
                    )
                }
            )
            if (settings.showSave == true) {
                SaveSVGButton()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxSize(),
            Arrangement.Center
        ) {
            Log.d("noteapp", "${lWidth}")
            DrawingCanvas(
                reset,
                lheight = lHeight,
                lwidth = lWidth,
                modifier = Modifier
                    .width(lWidth.dp)
                    .height(1000.dp)
            )
        }
    }
}

@Composable
fun ToolBarTool(
    toggleable: Boolean = true,
    onclick: () -> Unit,
    icon: @Composable (Color) -> Unit
) {
    val interSource = remember { MutableInteractionSource() }

    val isPressed by interSource.collectIsPressedAsState()

    var isClicked by remember { mutableStateOf(false) }

    val backgroundColor = if ((isClicked && toggleable) || isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if ((isClicked && toggleable) || isPressed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    FilledIconButton(
        onClick = {
            isClicked = !isClicked
            onclick()
        },
        interactionSource = interSource,
        modifier = Modifier
            .padding(2.dp)
            .size(25.dp),
        shape = RoundedCornerShape(5.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        icon(contentColor)
    }
}