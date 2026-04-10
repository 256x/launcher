package fumi.day.literallauncher.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import fumi.day.literallauncher.ui.LauncherViewModel
import fumi.day.literallauncher.util.launchMuDirect
import fumi.day.literallauncher.util.safeLower

@Composable
fun AppDrawerScreen(
    viewModel: LauncherViewModel,
    screenW: Int,
    globalScale: Float,
    currentFont: FontFamily,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    onAppClick: () -> Unit,
    onReturnToHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSlotPicker: (String) -> Unit
) {
    val context = LocalContext.current
    val allApps by viewModel.allApps.collectAsState()
    val chestApps by viewModel.chestApps.collectAsState()
    val renameVersion by viewModel.renameVersion.collectAsState()
    val drawerRight by viewModel.drawerRight.collectAsState()
    val listState = rememberLazyListState()

    var selectedAppForMenu by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showChestItems by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val visibleApps = remember(chestApps, renameVersion, allApps) {
        allApps
            .filter { it.first !in chestApps }
            .map { (pkg, name) -> pkg to viewModel.getRename(pkg, name) }
            .sortedBy { safeLower(it.second) }
    }
    val secondaryText = textColor.copy(alpha = 0.5f)
    val borderColor = textColor.copy(alpha = 0.2f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onReturnToHome() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = (screenW * 0.12f).dp, vertical = 80.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxHeight()
                    .width((screenW * 0.76f).dp)
                    .align(if (drawerRight) Alignment.TopEnd else Alignment.TopStart)
            ) {
                items(visibleApps, key = { it.first }) { (pkg, displayName) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = if (drawerRight) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Text(
                            text = safeLower(displayName),
                            color = textColor,
                            fontSize = (screenW * 0.045f * globalScale).sp,
                            fontFamily = currentFont,
                            modifier = Modifier.pointerInput(pkg) {
                                detectTapGestures(
                                    onTap = {
                                        launchMuDirect(context, pkg)
                                        onAppClick()
                                    },
                                    onLongPress = { selectedAppForMenu = pkg to displayName }
                                )
                            }
                        )
                    }
                }
            }
        }

        // Chest button
        Box(
            modifier = Modifier
                .align(if (drawerRight) Alignment.BottomStart else Alignment.BottomEnd)
                .padding(30.dp)
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable { showChestItems = true }
                .padding(16.dp)
        ) {
            Text(safeLower("chest"), color = secondaryText, fontFamily = currentFont)
        }

        // Settings button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(30.dp)
                .clickable { onOpenSettings() }
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = textColor,
                modifier = Modifier.size((screenW * 0.06f * globalScale).dp)
            )
        }

        // App context menu
        selectedAppForMenu?.let { (pkg, name) ->
            AlertDialog(
                onDismissRequest = { selectedAppForMenu = null },
                containerColor = bgColor,
                title = { Text(safeLower(name), color = textColor, fontFamily = currentFont) },
                text = {
                    Column {
                        listOf(
                            "rename" to {
                                showRenameDialog = pkg to name
                                selectedAppForMenu = null
                            },
                            "send to chest" to {
                                viewModel.addToChest(pkg)
                                selectedAppForMenu = null
                            },
                            "app info" to {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData("package:$pkg".toUri())
                                )
                                selectedAppForMenu = null
                            }
                        ).forEach { (label, action) ->
                            Text(
                                text = safeLower(label),
                                color = textColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { action() }
                                    .padding(16.dp)
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Chest dialog
        if (showChestItems) {
            val appsInChest = remember(chestApps, renameVersion, allApps) {
                allApps
                    .filter { it.first in chestApps }
                    .map { (pkg, name) -> pkg to viewModel.getRename(pkg, name) }
                    .sortedBy { safeLower(it.second) }
            }
            AlertDialog(
                onDismissRequest = { showChestItems = false },
                containerColor = bgColor,
                title = { Text(safeLower("chest"), color = accentColor, fontFamily = currentFont) },
                text = {
                    if (appsInChest.isEmpty()) {
                        Text(safeLower("chest is empty"), color = secondaryText)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(appsInChest, key = { it.first }) { (pkg, displayName) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = safeLower(displayName),
                                        color = textColor,
                                        fontFamily = currentFont,
                                        fontSize = (screenW * 0.045f * globalScale).sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                launchMuDirect(context, pkg)
                                                onAppClick()
                                                showChestItems = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                    )
                                    Text(
                                        text = safeLower("restore"),
                                        color = secondaryText,
                                        fontFamily = currentFont,
                                        fontSize = (screenW * 0.035f * globalScale).sp,
                                        modifier = Modifier
                                            .clickable { viewModel.removeFromChest(pkg) }
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChestItems = false }) {
                        Text(safeLower("done"), color = accentColor)
                    }
                }
            )
        }

        // Rename dialog
        showRenameDialog?.let { (pkg, name) ->
            var text by remember(pkg) { mutableStateOf(viewModel.getRename(pkg, name)) }
            AlertDialog(
                onDismissRequest = { showRenameDialog = null },
                containerColor = bgColor,
                title = { Text(safeLower("rename $name"), color = textColor) },
                text = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("new name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setRename(pkg, text.ifBlank { null })
                        showRenameDialog = null
                    }) {
                        Text(safeLower("ok"), color = accentColor)
                    }
                }
            )
        }
    }
}
