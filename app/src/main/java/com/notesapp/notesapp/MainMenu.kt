package com.notesapp.notesapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun MenuHost(navController: NavHostController) {
    MainNavigationRail(navController, NotesAppRoutes.Menu) {
        MainMenu(navController)
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenu(navController: NavHostController, ) {
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
                navController.navigate(NotesAppRoutes.Canvas.name)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
            //.padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("h")
                }
                rootFolderUri?.let {
                    Column() {
                        val context = LocalContext.current
                        val docs = listNotes(context = context, rootFolderUri!!)
                        if (docs.isNotEmpty()) {
                            for (doc in docs) {
                                FilledTonalButton(
                                    onClick = {
                                        file_uri = doc.uri
                                        navController.navigate(NotesAppRoutes.Canvas.name)
                                    }
                                ) {
                                    Text(doc.name?.substringBeforeLast(".") ?: "Untitled")
                                }
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = { showDialog = true },
                    contentPadding = PaddingValues(12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
//
//    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val scope = rememberCoroutineScope()
//
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            ModalDrawerSheet {
//                Text("Drawer Title", modifier = Modifier.padding(16.dp))
//                HorizontalDivider()
//                NavigationDrawerItem(
//                    label = { Text("Close Menu") },
//                    selected = false,
//                    onClick = {
//                        scope.launch { drawerState.close() }
//                    }
//                )
//            }
//        }
//    ) {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("") },
//                    navigationIcon = {
//                        IconButton(onClick = {
//                            scope.launch { drawerState.open() }
//                        }) {
//                            Icon(Icons.Default.Menu, contentDescription = "Menu")
//                        }
//                    }
//                )
//            }
//        ) { paddingValues ->
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues),
//                contentAlignment = Alignment.Center
//            ) {
//            }
//        }
//    }
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