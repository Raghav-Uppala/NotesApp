package com.notesapp.notesapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile

@Composable
fun MainMenu(
    onclick: () -> Unit
) {
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // 1. Persist the permission so it works even after app restart
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            sharedPref.edit { putString("root_folder_uri", it.toString()) }

            rootFolderUri = it
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        NameFileDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                showDialog = false
                val rootFolder = rootFolderUri?.let { DocumentFile.fromTreeUri(context, it) }
                val newFile = rootFolder?.createFile("image/svg+xml", name)
                file_uri = newFile?.uri
                onclick()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            FilledTonalButton(onClick = { showDialog = true }) {
                Text("New Note")
            }

        }
        rootFolderUri?.let {
            Column(modifier = Modifier.fillMaxSize()) {
                val context = LocalContext.current
                val docs = listNotes(context = context, rootFolderUri!!)
                if (docs.isNotEmpty()) {
                    for (doc in docs) {
                        FilledTonalButton(
                            onClick = {
                                file_uri = doc.uri
                                onclick()
                            }
                        ) {
                            Text(doc.name?.substringBeforeLast(".") ?: "Untitled")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NameFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "New Note Name") },
        text = {
            Column {
                Text("Enter a name for your note:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    placeholder = { Text("Untitled") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (fileName.isNotBlank()) onConfirm(fileName)
                    else onConfirm("Untitled_${System.currentTimeMillis()}")
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}