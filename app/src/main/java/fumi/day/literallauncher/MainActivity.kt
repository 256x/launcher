package fumi.day.literallauncher
import android.app.Activity
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
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
import kotlin.math.roundToInt

// --- Constants ---
const val SLOT_CLOCK = "slot_clock"; const val SLOT_DATE = "slot_date"; const val SLOT_BATTERY = "slot_battery"
const val SLOT_TOP_LEFT = "slot_tl"; const val SLOT_TOP_RIGHT = "slot_tr"
const val SLOT_MID_LEFT = "slot_ml"; const val SLOT_MID_RIGHT = "slot_mr"
const val SLOT_BOT_LEFT = "slot_bl"; const val SLOT_BOT_RIGHT = "slot_br"
const val SLOT_DOUBLE_TAP = "slot_double"
const val SET_SHOW_CLOCK = "show_clock"; const val SET_SHOW_DATE = "show_date"; const val SET_SHOW_BATTERY = "show_battery"
const val SET_FONT_INDEX = "font_index"; const val SET_GLOBAL_SCALE = "global_scale"
const val SET_DRAWER_RIGHT = "drawer_right"
const val SET_SLOT_LOCK = "slot_lock"
const val PREF_CHEST_APPS = "chest_apps"
const val PREF_RENAME_PREFIX = "rename_"
const val PREF_BG_COLOR = "bg_color"
const val PREF_TEXT_COLOR = "text_color"
const val PREF_ACCENT_COLOR = "accent_color"

fun safeLower(text: String): String = text.lowercase(Locale.ENGLISH)

fun parseColor(hex: String): Color? = try {
    if (hex.isBlank()) null
    else Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) { null }

fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

val appListVersion = mutableIntStateOf(0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            HideSystemBars()
            LiteralLauncherScreen()
        }
    }
    override fun onResume() {
        super.onResume()
        hideSystemBarsForcefully()
        appListVersion.intValue++
    }
    private fun hideSystemBarsForcefully() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Composable
fun HideSystemBars() {
    val view = LocalView.current
    val context = LocalContext.current
    SideEffect {
        val activity = context.findActivity() ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Composable
fun LiteralLauncherScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val windowInfo = LocalWindowInfo.current
    val containerSize = windowInfo.containerSize
    val slotIds = listOf(
        SLOT_TOP_LEFT, SLOT_MID_LEFT, SLOT_BOT_LEFT,
        SLOT_TOP_RIGHT, SLOT_MID_RIGHT, SLOT_BOT_RIGHT,
        SLOT_DOUBLE_TAP, SLOT_CLOCK, SLOT_DATE, SLOT_BATTERY
    )
    val slotStates = remember {
        mutableStateMapOf<String, String?>().apply {
            slotIds.forEach { id -> put(id, prefs.getString(id, null)) }
        }
    }
    var globalScale by remember { mutableFloatStateOf(prefs.getFloat(SET_GLOBAL_SCALE, 1.0f)) }
    var showClock by remember { mutableStateOf(prefs.getBoolean(SET_SHOW_CLOCK, true)) }
    var showDate by remember { mutableStateOf(prefs.getBoolean(SET_SHOW_DATE, true)) }
    var showBattery by remember { mutableStateOf(prefs.getBoolean(SET_SHOW_BATTERY, true)) }
    var drawerRight by remember { mutableStateOf(prefs.getBoolean(SET_DRAWER_RIGHT, false)) }
    var slotsLocked by remember { mutableStateOf(prefs.getBoolean(SET_SLOT_LOCK, false)) }
    var fontIndex by remember { mutableIntStateOf(prefs.getInt(SET_FONT_INDEX, 0)) }
    var bgColorHex by remember { mutableStateOf(prefs.getString(PREF_BG_COLOR, "") ?: "") }
    var textColorHex by remember { mutableStateOf(prefs.getString(PREF_TEXT_COLOR, "") ?: "") }
    var accentColorHex by remember { mutableStateOf(prefs.getString(PREF_ACCENT_COLOR, "") ?: "") }

    val fontFamilies = listOf(
        FontFamily.SansSerif,
        FontFamily.Serif,
        FontFamily.Monospace,
        FontFamily(Font(R.font.scopeone))
    )
    val fontNames = listOf("default", "serif", "mono", "scope")
    val currentFont = fontFamilies[fontIndex]
    val density = LocalDensity.current
    val screenW = with(density) { containerSize.width.toDp().value.toInt() }
    val screenH = with(density) { containerSize.height.toDp().value.toInt() }

    val bgColor = parseColor(bgColorHex) ?: Color.Black
    val textColor = parseColor(textColorHex) ?: Color.White
    val accentColor = parseColor(accentColorHex) ?: Color(0xFFBB86FC)

    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var batteryLevel by remember { mutableIntStateOf(getBatteryLevel(context)) }
    var showPickerSlot by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(appListVersion.intValue) { isDrawerOpen = false }

    fun queryAllApps(): List<Pair<String, String>> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return context.packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() }
    }
    val allApps = remember(appListVersion.intValue) { queryAllApps() }
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                appListVersion.intValue++
                if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
                    isDrawerOpen = false
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            batteryLevel = getBatteryLevel(context)
            delay(10_000)
        }
    }
    BackHandler(enabled = isDrawerOpen || showSettings) {
        if (showSettings) showSettings = false
        else isDrawerOpen = false
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .pointerInput(Unit) {
                var swipeStartY = 0f
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        if (changes.isEmpty()) continue
                        val change = changes.first()
                        when {
                            change.pressed && !change.previousPressed -> {
                                swipeStartY = change.position.y
                            }
                            !change.pressed && change.previousPressed -> {
                                val deltaY = change.position.y - swipeStartY
                                if (deltaY < -200f) {
                                    isDrawerOpen = true
                                }
                            }
                        }
                    }
                }
            }
            .pointerInput(slotsLocked) {
                detectTapGestures(
                    onDoubleTap = { launchMu(context, prefs, SLOT_DOUBLE_TAP) },
                    onLongPress = { if (!slotsLocked) showPickerSlot = SLOT_DOUBLE_TAP }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .clickable { expandNotifications(context) }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = (screenH * 0.35f).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((screenH * 0.012f * globalScale).dp)
        ) {
            if (showClock) {
                Text(
                    text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    color = textColor,
                    fontSize = (screenW * 0.07f * globalScale).sp,
                    fontFamily = currentFont,
                    modifier = Modifier.pointerInput(slotsLocked) {
                        detectTapGestures(
                            onTap = { launchMu(context, prefs, SLOT_CLOCK) },
                            onLongPress = { if (!slotsLocked) showPickerSlot = SLOT_CLOCK }
                        )
                    }
                )
            }
            if (showDate) {
                Text(
                    text = safeLower(
                        currentTime.format(DateTimeFormatter.ofPattern("EEEE d", Locale.ENGLISH))
                    ),
                    color = textColor,
                    fontSize = (screenW * 0.045f * globalScale).sp,
                    fontFamily = currentFont,
                    modifier = Modifier.pointerInput(slotsLocked) {
                        detectTapGestures(
                            onTap = { launchMu(context, prefs, SLOT_DATE) },
                            onLongPress = { if (!slotsLocked) showPickerSlot = SLOT_DATE }
                        )
                    }
                )
            }
            if (showBattery) {
                Text(
                    text = "$batteryLevel%",
                    color = textColor,
                    fontSize = (screenW * 0.045f * globalScale).sp,
                    fontFamily = currentFont,
                    modifier = Modifier.pointerInput(slotsLocked) {
                        detectTapGestures(
                            onTap = { launchMu(context, prefs, SLOT_BATTERY) },
                            onLongPress = { if (!slotsLocked) showPickerSlot = SLOT_BATTERY }
                        )
                    }
                )
            }
        }
        if (!isDrawerOpen) {
            val targets = listOf(
                SLOT_TOP_LEFT to Alignment.TopStart,
                SLOT_MID_LEFT to Alignment.CenterStart,
                SLOT_BOT_LEFT to Alignment.BottomStart,
                SLOT_TOP_RIGHT to Alignment.TopEnd,
                SLOT_MID_RIGHT to Alignment.CenterEnd,
                SLOT_BOT_RIGHT to Alignment.BottomEnd
            )
            targets.forEach { (slot, align) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.33f)
                        .fillMaxHeight(0.33f)
                        .align(align)
                        .pointerInput(slot, slotsLocked) {
                            detectTapGestures(
                                onTap = { launchMu(context, prefs, slot) },
                                onLongPress = { if (!slotsLocked) showPickerSlot = slot }
                            )
                        }
                )
            }
        }
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            AppDrawer(
                screenW = screenW,
                globalScale = globalScale,
                currentFont = currentFont,
                drawerRight = drawerRight,
                bgColor = bgColor,
                textColor = textColor,
                accentColor = accentColor,
                allApps = allApps,
                onAppClick = { isDrawerOpen = false },
                onReturnToHome = { isDrawerOpen = false },
                onOpenSettings = { showSettings = true },
                onOpenSlotPicker = { showPickerSlot = it }
            )
        }
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            LauncherSettingsScreen(
                globalScale = globalScale,
                fontIndex = fontIndex,
                fontNames = fontNames,
                currentFont = currentFont,
                showClock = showClock,
                showDate = showDate,
                showBattery = showBattery,
                drawerRight = drawerRight,
                slotsLocked = slotsLocked,
                bgColor = bgColor,
                textColor = textColor,
                accentColor = accentColor,
                bgColorHex = bgColorHex,
                textColorHex = textColorHex,
                accentColorHex = accentColorHex,
                allApps = allApps,
                slotStates = slotStates,
                onSettingsChange = { key, value ->
                    prefs.edit {
                        when (value) {
                            is Float -> putFloat(key, value)
                            is Boolean -> putBoolean(key, value)
                            is Int -> putInt(key, value)
                            is String -> putString(key, value)
                        }
                    }
                    when (key) {
                        SET_GLOBAL_SCALE -> globalScale = value as Float
                        SET_FONT_INDEX -> fontIndex = value as Int
                        SET_SHOW_CLOCK -> showClock = value as Boolean
                        SET_SHOW_DATE -> showDate = value as Boolean
                        SET_SHOW_BATTERY -> showBattery = value as Boolean
                        SET_DRAWER_RIGHT -> drawerRight = value as Boolean
                        SET_SLOT_LOCK -> slotsLocked = value as Boolean
                        PREF_BG_COLOR -> bgColorHex = value as String
                        PREF_TEXT_COLOR -> textColorHex = value as String
                        PREF_ACCENT_COLOR -> accentColorHex = value as String
                    }
                },
                onOpenSlotPicker = { showPickerSlot = it },
                onDismiss = { showSettings = false }
            )
        }
        showPickerSlot?.let { slotKey ->
            AppPicker(
                slotName = slotKey,
                currentFont = currentFont,
                globalScale = globalScale,
                bgColor = bgColor,
                textColor = textColor,
                accentColor = accentColor,
                allApps = allApps,
                onDismiss = { showPickerSlot = null },
                onSelect = { pkg ->
                    prefs.edit {
                        if (pkg == null) remove(slotKey)
                        else putString(slotKey, pkg)
                    }
                    slotStates[slotKey] = pkg
                    showPickerSlot = null
                }
            )
        }
    }
}

@Composable
fun AppDrawer(
    screenW: Int,
    globalScale: Float,
    currentFont: FontFamily,
    drawerRight: Boolean,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    allApps: List<Pair<String, String>>,
    onAppClick: () -> Unit,
    onReturnToHome: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSlotPicker: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val listState = rememberLazyListState()
    var chestAppsSet by remember {
        mutableStateOf(prefs.getStringSet(PREF_CHEST_APPS, emptySet()) ?: emptySet())
    }
    var selectedAppForMenu by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showChestItems by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var renameVersion by remember { mutableIntStateOf(0) }
    val visibleApps = remember(chestAppsSet, renameVersion, allApps) {
        allApps
            .filter { it.first !in chestAppsSet }
            .map { (pkg, name) ->
                val displayName = prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name
                pkg to displayName
            }
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
                items(visibleApps.size) { i ->
                    val (pkg, displayName) = visibleApps[i]
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
                                    onLongPress = {
                                        selectedAppForMenu = pkg to displayName
                                    }
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
        if (selectedAppForMenu != null) {
            val (pkg, name) = selectedAppForMenu!!
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
                                val newSet = chestAppsSet + pkg
                                prefs.edit { putStringSet(PREF_CHEST_APPS, newSet) }
                                chestAppsSet = newSet
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

        // Chest items
        if (showChestItems) {
            AlertDialog(
                onDismissRequest = { showChestItems = false },
                containerColor = bgColor,
                title = { Text(safeLower("chest"), color = accentColor, fontFamily = currentFont) },
                text = {
                    val appsInChest = allApps
                        .filter { it.first in chestAppsSet }
                        .map { (pkg, name) ->
                            pkg to (prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name)
                        }
                        .sortedBy { safeLower(it.second) }
                    if (appsInChest.isEmpty()) {
                        Text(safeLower("chest is empty"), color = secondaryText)
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(appsInChest.size) { i ->
                            val (pkg, displayName) = appsInChest[i]
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
                                        .clickable {
                                            val newSet = chestAppsSet - pkg
                                            prefs.edit { putStringSet(PREF_CHEST_APPS, newSet) }
                                            chestAppsSet = newSet
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                )
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
        if (showRenameDialog != null) {
            val (pkg, name) = showRenameDialog!!
            var text by remember(pkg) {
                mutableStateOf(prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name)
            }
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
                        prefs.edit {
                            if (text.isBlank()) remove("${PREF_RENAME_PREFIX}$pkg")
                            else putString("${PREF_RENAME_PREFIX}$pkg", text)
                        }
                        renameVersion++
                        showRenameDialog = null
                    }) {
                        Text(safeLower("ok"), color = accentColor)
                    }
                }
            )
        }
    }
}

@Composable
fun LauncherSettingsScreen(
    globalScale: Float,
    fontIndex: Int,
    fontNames: List<String>,
    currentFont: FontFamily,
    showClock: Boolean,
    showDate: Boolean,
    showBattery: Boolean,
    drawerRight: Boolean,
    slotsLocked: Boolean,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    bgColorHex: String,
    textColorHex: String,
    accentColorHex: String,
    allApps: List<Pair<String, String>>,
    slotStates: Map<String, String?>,
    onSettingsChange: (String, Any) -> Unit,
    onOpenSlotPicker: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "" }
        catch (_: Exception) { "" }
    }
    var showColorPicker by remember { mutableStateOf<String?>(null) } // "bg", "text", "accent"
    var showSlotMenu by remember { mutableStateOf(false) }

    val cardBg = textColor.copy(alpha = 0.05f)
    val dividerColor = textColor.copy(alpha = 0.15f)
    val secondaryText = textColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back button row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "←",
                    color = textColor,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(8.dp)
                )
                Text(
                    text = safeLower("Settings"),
                    color = textColor,
                    fontSize = 18.sp,
                    fontFamily = currentFont,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Appearance card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(safeLower("Appearance"), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = secondaryText)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Font chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fontNames.forEachIndexed { i, name ->
                            FilterChip(
                                selected = fontIndex == i,
                                onClick = { onSettingsChange(SET_FONT_INDEX, i) },
                                label = { Text(safeLower(name), fontFamily = currentFont) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = accentColor
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Scale slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(safeLower("Size"), color = textColor, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                        Slider(
                            value = globalScale,
                            onValueChange = { onSettingsChange(SET_GLOBAL_SCALE, it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                        Text(
                            text = "${(globalScale * 100).roundToInt()}%",
                            color = textColor,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = dividerColor)

                    // Color dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LauncherColorDot(
                            label = "BG",
                            color = bgColor,
                            textColor = textColor,
                            onClick = { showColorPicker = "bg" }
                        )
                        LauncherColorDot(
                            label = "Text",
                            color = textColor,
                            textColor = textColor,
                            onClick = { showColorPicker = "text" }
                        )
                        LauncherColorDot(
                            label = "Accent",
                            color = accentColor,
                            textColor = textColor,
                            onClick = { showColorPicker = "accent" }
                        )
                    }
                }
            }

            // Display card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(safeLower("Display"), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = secondaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        Triple(SET_SHOW_CLOCK, "show clock", showClock),
                        Triple(SET_SHOW_DATE, "show date", showDate),
                        Triple(SET_SHOW_BATTERY, "show battery", showBattery)
                    ).forEach { (key, label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(safeLower(label), color = textColor, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = value,
                                onCheckedChange = { onSettingsChange(key, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // Layout card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(safeLower("Layout"), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = secondaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        Triple(SET_DRAWER_RIGHT, "controls on left", drawerRight),
                        Triple(SET_SLOT_LOCK, "lock slots", slotsLocked)
                    ).forEach { (key, label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(safeLower(label), color = textColor, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = value,
                                onCheckedChange = { onSettingsChange(key, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // Slots card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(safeLower("Slots"), style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = secondaryText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showSlotMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.2f))
                    ) {
                        Text(safeLower("assign slots"), color = accentColor, fontFamily = currentFont)
                    }
                }
            }

            // Version
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Literal Launcher v$versionName",
                color = secondaryText,
                fontSize = 12.sp,
                fontFamily = currentFont,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Slot assignment dialog
    if (showSlotMenu) {
        AlertDialog(
            onDismissRequest = { showSlotMenu = false },
            containerColor = bgColor,
            title = { Text(safeLower("assign slots"), color = textColor, fontFamily = currentFont) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val slots = listOf(
                        SLOT_TOP_LEFT to "top left", SLOT_MID_LEFT to "mid left", SLOT_BOT_LEFT to "bot left",
                        SLOT_TOP_RIGHT to "top right", SLOT_MID_RIGHT to "mid right", SLOT_BOT_RIGHT to "bot right",
                        SLOT_DOUBLE_TAP to "double tap",
                        SLOT_CLOCK to "clock", SLOT_DATE to "date", SLOT_BATTERY to "battery"
                    )
                    slots.forEach { (id, label) ->
                        val selectedPkg = slotStates[id]
                        val selectedName = allApps.find { it.first == selectedPkg }?.let { (pkg, originalName) ->
                            prefs.getString("${PREF_RENAME_PREFIX}$pkg", originalName) ?: originalName
                        } ?: "none"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenSlotPicker(id) }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = safeLower(label), color = accentColor, fontSize = 14.sp)
                            Text(text = safeLower(selectedName), color = secondaryText, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSlotMenu = false }) {
                    Text(safeLower("done"), color = accentColor)
                }
            }
        )
    }

    // Color picker
    showColorPicker?.let { target ->
        val initialColor = when (target) {
            "bg" -> bgColor
            "text" -> textColor
            else -> accentColor
        }
        LauncherColorPickerDialog(
            initialColor = initialColor,
            textColor = textColor,
            accentColor = accentColor,
            onColorSelected = { color ->
                val hex = colorToHex(color)
                val key = when (target) {
                    "bg" -> PREF_BG_COLOR
                    "text" -> PREF_TEXT_COLOR
                    else -> PREF_ACCENT_COLOR
                }
                onSettingsChange(key, hex)
                showColorPicker = null
            },
            onDismiss = { showColorPicker = null }
        )
    }
}

@Composable
private fun LauncherColorDot(label: String, color: Color, textColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, textColor.copy(alpha = 0.3f), CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = textColor)
    }
}

@Composable
private fun LauncherColorPickerDialog(
    initialColor: Color,
    textColor: Color,
    accentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(initialColor) {
        if (initialColor != Color.Unspecified) {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
            hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2]
        }
    }

    val currentColor = Color.hsv(hue, saturation, brightness)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(safeLower("select color"), color = textColor) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(hue) {
                            detectTapGestures { offset ->
                                saturation = (offset.x / size.width).coerceIn(0f, 1f)
                                brightness = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                            }
                        }
                        .pointerInput(hue) {
                            detectDragGestures { change, _ ->
                                saturation = (change.position.x / size.width).coerceIn(0f, 1f)
                                brightness = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.horizontalGradient(colors = listOf(Color.White, Color.hsv(hue, 1f, 1f))))
                        drawRect(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black)))
                        val cx = saturation * size.width
                        val cy = (1f - brightness) * size.height
                        drawCircle(color = Color.White, radius = 12f, center = Offset(cx, cy))
                        drawCircle(color = Color.Black, radius = 10f, center = Offset(cx, cy),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                hue = (offset.x / size.width * 360f).coerceIn(0f, 360f)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                hue = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val colors = (0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }
                        drawRect(brush = Brush.horizontalGradient(colors))
                        val cx = hue / 360f * size.width
                        drawCircle(color = Color.White, radius = 14f, center = Offset(cx, size.height / 2))
                        drawCircle(color = Color.Black, radius = 12f, center = Offset(cx, size.height / 2),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(1.dp, textColor.copy(alpha = 0.3f), CircleShape)
                    )
                    Text(text = colorToHex(currentColor), color = textColor, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(currentColor) }) { Text(safeLower("select"), color = accentColor) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(safeLower("cancel"), color = accentColor) }
        }
    )
}

// --- Helpers ---
fun launchMu(context: Context, prefs: SharedPreferences, slot: String) {
    val pkg = prefs.getString(slot, null) ?: return
    launchMuDirect(context, pkg)
}

fun launchMuDirect(context: Context, pkg: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return
    intent.addFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_ANIMATION or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    )
    val options = try {
        ActivityOptions.makeCustomAnimation(context, R.anim.stay_still, R.anim.stay_still)
    } catch (_: Exception) {
        ActivityOptions.makeCustomAnimation(context, 0, 0)
    }
    context.startActivity(intent, options.toBundle())
    if (context is Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                context.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.stay_still,
                    R.anim.stay_still
                )
            } catch (_: Exception) {
                @Suppress("DEPRECATION")
                context.overridePendingTransition(0, 0)
            }
        } else {
            @Suppress("DEPRECATION")
            context.overridePendingTransition(0, 0)
        }
    }
}

@Composable
fun AppPicker(
    slotName: String,
    currentFont: FontFamily,
    globalScale: Float,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    allApps: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val apps = allApps
        .map { (pkg, name) ->
            pkg to (prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name)
        }
        .sortedBy { safeLower(it.second) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        title = { Text(safeLower("set $slotName"), color = textColor, fontFamily = currentFont) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                item {
                    Text(
                        text = safeLower("none"),
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = (16 * globalScale).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(null) }
                            .padding(vertical = 12.dp)
                    )
                }
                items(apps.size) { i ->
                    Text(
                        text = safeLower(apps[i].second),
                        color = textColor,
                        fontSize = (16 * globalScale).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(apps[i].first) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(safeLower("cancel"), color = accentColor)
            }
        }
    )
}

fun getBatteryLevel(context: Context): Int {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    return intent?.let {
        val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            0
        }
    } ?: 0
}

fun expandNotifications(context: Context) {
    try {
        val sbs = context.getSystemService("statusbar")
        val sbm = Class.forName("android.app.StatusBarManager")
        sbm.getMethod("expandNotificationsPanel").invoke(sbs)
    } catch (_: Exception) {
    }
}
