package fumi.day.literallauncher

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- Constants ---
const val SLOT_CLOCK = "slot_clock"; const val SLOT_DATE = "slot_date"; const val SLOT_BATTERY = "slot_battery"
const val SLOT_TOP_LEFT = "slot_tl"; const val SLOT_TOP_RIGHT = "slot_tr"
const val SLOT_MID_LEFT = "slot_ml"; const val SLOT_MID_RIGHT = "slot_mr"
const val SLOT_BOT_LEFT = "slot_bl"; const val SLOT_BOT_RIGHT = "slot_br"
const val SLOT_DOUBLE_TAP = "slot_double"
const val SET_SHOW_CLOCK = "show_clock"; const val SET_SHOW_DATE = "show_date"; const val SET_SHOW_BATTERY = "show_battery"
const val SET_FONT_INDEX = "font_index"; const val SET_GLOBAL_SCALE = "global_scale"
const val PREF_CHEST_APPS = "chest_apps"
const val PREF_RENAME_PREFIX = "rename_"

fun safeLower(text: String): String = text.lowercase(Locale.ENGLISH)
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { HideSystemBars(); LiteralLauncherScreen() }
    }
    override fun onResume() { super.onResume(); hideSystemBarsForcefully() }
    private fun hideSystemBarsForcefully() {
        val window = this.window ?: return
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Composable
fun HideSystemBars() {
    val view = LocalView.current; val context = LocalContext.current
    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiteralLauncherScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val config = LocalConfiguration.current

    // --- Slot Management (Bug Fix) ---
    val slotIds = listOf(SLOT_TOP_LEFT, SLOT_MID_LEFT, SLOT_BOT_LEFT, SLOT_TOP_RIGHT, SLOT_MID_RIGHT, SLOT_BOT_RIGHT, SLOT_DOUBLE_TAP, SLOT_CLOCK, SLOT_DATE, SLOT_BATTERY)
    val slotStates = remember {
        mutableStateMapOf<String, String?>().apply {
            slotIds.forEach { id -> put(id, prefs.getString(id, null)) }
        }
    }

    var globalScale by remember { mutableFloatStateOf(prefs.getFloat(SET_GLOBAL_SCALE, 1.0f)) }
    var showClock by remember { mutableStateOf(prefs.getBoolean(SET_SHOW_CLOCK, true)) }
    var showDate by remember { mutableStateOf(prefs.getBoolean(SET_SHOW_DATE, true)) }
    var showBattery by remember { mutableStateOf(prefs.getBoolean(SET_SHOW_BATTERY, true)) }
    var fontIndex by remember { mutableIntStateOf(prefs.getInt(SET_FONT_INDEX, 0)) }

    val fontFamilies = listOf(FontFamily(Font(R.font.scopeone)), FontFamily.SansSerif, FontFamily.Serif, FontFamily.Monospace)
    val fontNames = listOf("scope one", "sans serif", "serif", "monospace")
    val currentFont = fontFamilies[fontIndex]

    val screenW = config.screenWidthDp; val screenH = config.screenHeightDp
    var isDrawerOpen by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var batteryLevel by remember { mutableIntStateOf(getBatteryLevel(context)) }
    var showPickerSlot by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { while (true) { currentTime = LocalDateTime.now(); batteryLevel = getBatteryLevel(context); delay(10000) } }
    BackHandler(enabled = isDrawerOpen) { isDrawerOpen = false }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)
        .combinedClickable(onDoubleClick = { launchMu(context, prefs, SLOT_DOUBLE_TAP) }, onLongClick = { showPickerSlot = SLOT_DOUBLE_TAP }, onClick = {})
        .draggable(state = rememberDraggableState { if (it < -15) isDrawerOpen = true }, orientation = Orientation.Vertical)
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.3f).height(60.dp).align(Alignment.BottomCenter).clickable { expandNotifications(context) })

        Column(modifier = Modifier.fillMaxWidth().padding(top = (screenH * 0.35f).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy((screenH * 0.012f * globalScale).dp)) {
            if (showClock) Text(currentTime.format(DateTimeFormatter.ofPattern("HH:mm")), color = Color.White, fontSize = (screenW * 0.07f * globalScale).sp, fontFamily = currentFont, modifier = Modifier.combinedClickable(onClick = { launchMu(context, prefs, SLOT_CLOCK) }, onLongClick = { showPickerSlot = SLOT_CLOCK }))
            if (showDate) Text(safeLower(currentTime.format(DateTimeFormatter.ofPattern("EEEE d", Locale.ENGLISH))), color = Color.White, fontSize = (screenW * 0.045f * globalScale).sp, fontFamily = currentFont, modifier = Modifier.combinedClickable(onClick = { launchMu(context, prefs, SLOT_DATE) }, onLongClick = { showPickerSlot = SLOT_DATE }))
            if (showBattery) Text("$batteryLevel%", color = Color.White, fontSize = (screenW * 0.045f * globalScale).sp, fontFamily = currentFont, modifier = Modifier.combinedClickable(onClick = { launchMu(context, prefs, SLOT_BATTERY) }, onLongClick = { showPickerSlot = SLOT_BATTERY }))
        }

        if (!isDrawerOpen) {
            val targets = listOf(SLOT_TOP_LEFT to Alignment.TopStart, SLOT_MID_LEFT to Alignment.CenterStart, SLOT_BOT_LEFT to Alignment.BottomStart, SLOT_TOP_RIGHT to Alignment.TopEnd, SLOT_MID_RIGHT to Alignment.CenterEnd, SLOT_BOT_RIGHT to Alignment.BottomEnd)
            targets.forEach { (slot, align) -> Box(modifier = Modifier.fillMaxWidth(0.33f).fillMaxHeight(0.33f).align(align).combinedClickable(onClick = { launchMu(context, prefs, slot) }, onLongClick = { showPickerSlot = slot })) }
        }

        AnimatedVisibility(visible = isDrawerOpen, enter = fadeIn(animationSpec = tween(150)), exit = fadeOut(animationSpec = tween(150))) {
            AppDrawer(screenW, globalScale, currentFont, fontIndex, fontNames, showClock, showDate, showBattery,
                slotStates = slotStates, // Added
                onSettingsChange = { k, v ->
                    when(k) { SET_GLOBAL_SCALE -> globalScale = v as Float; SET_SHOW_CLOCK -> showClock = v as Boolean; SET_SHOW_DATE -> showDate = v as Boolean; SET_SHOW_BATTERY -> showBattery = v as Boolean; SET_FONT_INDEX -> fontIndex = v as Int }
                }, onAppClick = { isDrawerOpen = false }, onReturnToHome = { isDrawerOpen = false }, onOpenSlotPicker = { showPickerSlot = it })
        }

        if (showPickerSlot != null) {
            AppPicker(showPickerSlot!!, currentFont, globalScale, { showPickerSlot = null }, { pkg ->
                prefs.edit { putString(showPickerSlot, pkg) }
                slotStates[showPickerSlot!!] = pkg // UI state update
                showPickerSlot = null
            })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(screenW: Int, globalScale: Float, currentFont: FontFamily, fontIndex: Int, fontNames: List<String>,
              showClock: Boolean, showDate: Boolean, showBattery: Boolean,
              slotStates: Map<String, String?>, // Added
              onSettingsChange: (String, Any) -> Unit, onAppClick: () -> Unit, onReturnToHome: () -> Unit, onOpenSlotPicker: (String) -> Unit) {
    val context = LocalContext.current; val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val listState = rememberLazyListState(); val accentColor = Color(0xFFBB86FC)

    var chestAppsSet by remember { mutableStateOf(prefs.getStringSet(PREF_CHEST_APPS, emptySet()) ?: emptySet()) }
    var selectedAppForMenu by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showMainChest by remember { mutableStateOf(false) }; var showSlotMenu by remember { mutableStateOf(false) }; var showChestItems by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val allApps = remember {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        context.packageManager.queryIntentActivities(intent, 0).map { it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() }.sortedBy { safeLower(it.second) }
    }
    val visibleApps = allApps.filter { it.first !in chestAppsSet }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onReturnToHome() }) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = (screenW * 0.12f).dp, vertical = 80.dp)) {
            items(visibleApps.size) { i ->
                val (pkg, name) = visibleApps[i]
                val displayName = prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name
                Text(text = safeLower(displayName), color = Color.White, fontSize = (screenW * 0.045f * globalScale).sp, fontFamily = currentFont,
                    modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { launchMuDirect(context, pkg); onAppClick() }, onLongClick = { selectedAppForMenu = pkg to name }).padding(vertical = 12.dp))
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(30.dp).border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp)).clickable { showMainChest = true }.padding(16.dp)) {
            Text(safeLower("chest"), color = Color.Gray, fontFamily = currentFont)
        }

        if (selectedAppForMenu != null) {
            val (pkg, name) = selectedAppForMenu!!
            AlertDialog(onDismissRequest = { selectedAppForMenu = null }, containerColor = Color(0xFF111111),
                title = { Text(safeLower(name), color = Color.White, fontFamily = currentFont) },
                text = {
                    Column {
                        Text(safeLower("rename"), color = Color.White, modifier = Modifier.fillMaxWidth().clickable { showRenameDialog = pkg to name; selectedAppForMenu = null }.padding(16.dp))
                        Text(safeLower("send to chest"), color = Color.White, modifier = Modifier.fillMaxWidth().clickable {
                            val newSet = chestAppsSet + pkg
                            prefs.edit { putStringSet(PREF_CHEST_APPS, newSet) }
                            chestAppsSet = newSet; selectedAppForMenu = null
                        }.padding(16.dp))
                        Text(safeLower("app info"), color = Color.White, modifier = Modifier.fillMaxWidth().clickable {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData("package:$pkg".toUri()))
                            selectedAppForMenu = null
                        }.padding(16.dp))
                    }
                }, confirmButton = {})
        }

        if (showMainChest) {
            AlertDialog(onDismissRequest = { showMainChest = false }, containerColor = Color(0xFF111111),
                title = { Text(safeLower("CHEST"), color = Color.White, fontFamily = currentFont) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Button(onClick = { showChestItems = true }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                            Text(safeLower("open chest apps"), color = Color.Black, fontFamily = currentFont)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.DarkGray)
                        Text(safeLower("UI SCALE"), color = Color.Gray, fontSize = 12.sp)
                        Slider(value = globalScale, onValueChange = { onSettingsChange(SET_GLOBAL_SCALE, it); prefs.edit { putFloat(SET_GLOBAL_SCALE, it) } }, valueRange = 0.5f..2.0f, colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor))
                        Button(onClick = { val next = (fontIndex + 1) % fontNames.size; onSettingsChange(SET_FONT_INDEX, next); prefs.edit { putInt(SET_FONT_INDEX, next) } }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text(safeLower(fontNames[fontIndex]), fontFamily = currentFont) }
                        listOf(SET_SHOW_CLOCK to "show clock" to showClock, SET_SHOW_DATE to "show date" to showDate, SET_SHOW_BATTERY to "show battery" to showBattery).forEach { pair ->
                            val (kl, cur) = pair; val (k, l) = kl
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onSettingsChange(k, !cur); prefs.edit { putBoolean(k, !cur) } }.padding(vertical = 4.dp)) {
                                Checkbox(checked = cur, onCheckedChange = { onSettingsChange(k, it); prefs.edit { putBoolean(k, it) } }, colors = CheckboxDefaults.colors(checkedColor = accentColor))
                                Text(safeLower(l), color = Color.White, fontFamily = currentFont)
                            }
                        }
                        Button(onClick = { showSlotMenu = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text(safeLower("assign slots"), fontFamily = currentFont) }
                    }
                }, confirmButton = { TextButton(onClick = { showMainChest = false }) { Text(safeLower("done"), color = accentColor) } })
        }

        if (showSlotMenu) {
            AlertDialog(onDismissRequest = { showSlotMenu = false }, containerColor = Color(0xFF111111),
                title = { Text(safeLower("ASSIGN SLOTS"), color = Color.White, fontFamily = currentFont) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        val slots = listOf(SLOT_TOP_LEFT to "top left", SLOT_MID_LEFT to "mid left", SLOT_BOT_LEFT to "bot left", SLOT_TOP_RIGHT to "top right", SLOT_MID_RIGHT to "mid right", SLOT_BOT_RIGHT to "bot right", SLOT_DOUBLE_TAP to "double tap", SLOT_CLOCK to "clock", SLOT_DATE to "date", SLOT_BATTERY to "battery")
                        slots.forEach { (id, label) ->
                            val p = slotStates[id] // Using slotStates for reactive UI update
                            val n = allApps.find { it.first == p }?.second ?: "none"
                            Column(modifier = Modifier.fillMaxWidth().clickable { onOpenSlotPicker(id) }.padding(vertical = 8.dp)) {
                                Text(safeLower(label), color = accentColor, fontSize = 14.sp)
                                Text(safeLower(n), color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }, confirmButton = { TextButton(onClick = { showSlotMenu = false }) { Text(safeLower("back"), color = accentColor) } })
        }

        if (showChestItems) {
            AlertDialog(onDismissRequest = { showChestItems = false }, containerColor = Color(0xFF111111),
                title = { Text(safeLower("CHEST APPS"), color = accentColor, fontFamily = currentFont) },
                text = {
                    val appsInChest = allApps.filter { it.first in chestAppsSet }
                    if (appsInChest.isEmpty()) { Text(safeLower("chest is empty"), color = Color.Gray) }
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(appsInChest.size) { i ->
                            val (pkg, name) = appsInChest[i]
                            val displayName = prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                launchMuDirect(context, pkg); onAppClick(); showChestItems = false; showMainChest = false
                            }.padding(vertical = 8.dp)) {
                                Text(safeLower(displayName), color = Color.White, modifier = Modifier.weight(1f).padding(8.dp))
                                IconButton(onClick = {
                                    val newSet = chestAppsSet - pkg
                                    prefs.edit { putStringSet(PREF_CHEST_APPS, newSet) }
                                    chestAppsSet = newSet
                                }) { Icon(Icons.Default.Refresh, contentDescription = "Restore", tint = Color.Gray) }
                            }
                        }
                    }
                }, confirmButton = { TextButton(onClick = { showChestItems = false }) { Text(safeLower("back"), color = accentColor) } })
        }

        if (showRenameDialog != null) {
            val (pkg, name) = showRenameDialog!!; var text by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { showRenameDialog = null }, containerColor = Color(0xFF111111),
                title = { Text(safeLower("rename $name"), color = Color.White) },
                text = { TextField(value = text, onValueChange = { text = it }, placeholder = { Text("new name") }) },
                confirmButton = { TextButton(onClick = { prefs.edit { putString("${PREF_RENAME_PREFIX}$pkg", text) }; showRenameDialog = null }) { Text(safeLower("ok"), color = accentColor) } })
        }
    }
}

// --- Helpers ---
fun launchMu(context: Context, prefs: android.content.SharedPreferences, slot: String) {
    val pkg = prefs.getString(slot, null) ?: return; launchMuDirect(context, pkg)
}

fun launchMuDirect(context: Context, pkg: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    val options = try { ActivityOptions.makeCustomAnimation(context, R.anim.stay_still, R.anim.stay_still) } catch (_: Exception) { ActivityOptions.makeCustomAnimation(context, 0, 0) }
    context.startActivity(intent, options.toBundle())
    if (context is Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try { context.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.stay_still, R.anim.stay_still) } catch (_: Exception) { @Suppress("DEPRECATION") context.overridePendingTransition(0, 0) }
        } else { @Suppress("DEPRECATION") context.overridePendingTransition(0, 0) }
    }
}

@Composable
fun AppPicker(slotName: String, currentFont: FontFamily, globalScale: Float, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val apps = remember {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        context.packageManager.queryIntentActivities(intent, 0).map { it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() }.sortedBy { safeLower(it.second) }
    }
    AlertDialog(onDismissRequest = onDismiss, containerColor = Color(0xFF111111), title = { Text(safeLower("set $slotName"), color = Color.White, fontFamily = currentFont) }, text = { LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) { items(apps.size) { i -> Text(safeLower(apps[i].second), color = Color.LightGray, fontSize = (16 * globalScale).sp, modifier = Modifier.fillMaxWidth().clickable { onSelect(apps[i].first) }.padding(vertical = 12.dp)) } } }, confirmButton = { TextButton(onClick = onDismiss) { Text(safeLower("cancel")) } })
}

fun getBatteryLevel(context: Context): Int {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    return intent?.let { (it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / it.getIntExtra(BatteryManager.EXTRA_SCALE, -1).toFloat()).toInt() } ?: 0
}

fun expandNotifications(context: Context) {
    try {
        val sbs = context.getSystemService("statusbar"); val sbm = Class.forName("android.app.StatusBarManager")
        sbm.getMethod("expandNotificationsPanel").invoke(sbs)
    } catch (_: Exception) {}
}
