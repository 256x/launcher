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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
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

fun safeLower(text: String): String = text.lowercase(Locale.ENGLISH)

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
    }    private fun hideSystemBarsForcefully() {
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
    val fontFamilies = listOf(
        FontFamily(Font(R.font.scopeone)),
        FontFamily.SansSerif,
        FontFamily.Serif,
        FontFamily.Monospace
    )
    val fontNames = listOf("scope one", "sans serif", "serif", "monospace")
    val currentFont = fontFamilies[fontIndex]
    val density = LocalDensity.current
    val screenW = with(density) { containerSize.width.toDp().value.toInt() }
    val screenH = with(density) { containerSize.height.toDp().value.toInt() }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var batteryLevel by remember { mutableIntStateOf(getBatteryLevel(context)) }
    var showPickerSlot by remember { mutableStateOf<String?>(null) }

    // allApps をonResumeのタイミングで再取得
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
    BackHandler(enabled = isDrawerOpen) { isDrawerOpen = false }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
                                if (deltaY < -30f) {
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
                    color = Color.White,
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
                    color = Color.White,
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
                    color = Color.White,
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
                fontIndex = fontIndex,
                fontNames = fontNames,
                showClock = showClock,
                showDate = showDate,
                showBattery = showBattery,
                drawerRight = drawerRight,
                slotsLocked = slotsLocked,
                slotStates = slotStates,
                allApps = allApps,
                onSettingsChange = { key, value ->
                    when (key) {
                        SET_GLOBAL_SCALE -> globalScale = value as Float
                        SET_SHOW_CLOCK -> showClock = value as Boolean
                        SET_SHOW_DATE -> showDate = value as Boolean
                        SET_SHOW_BATTERY -> showBattery = value as Boolean
                        SET_DRAWER_RIGHT -> drawerRight = value as Boolean
                        SET_SLOT_LOCK -> slotsLocked = value as Boolean
                        SET_FONT_INDEX -> fontIndex = value as Int
                    }
                },
                onAppClick = { isDrawerOpen = false },
                onReturnToHome = { isDrawerOpen = false },
                onOpenSlotPicker = { showPickerSlot = it }
            )
        }
        showPickerSlot?.let { slotKey ->
            AppPicker(
                slotName = slotKey,
                currentFont = currentFont,
                globalScale = globalScale,
                onDismiss = { showPickerSlot = null },
                onSelect = { pkg ->
                    prefs.edit {
                        if (pkg == null) {
                            remove(slotKey)
                        } else {
                            putString(slotKey, pkg)
                        }
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
    fontIndex: Int,
    fontNames: List<String>,
    showClock: Boolean,
    showDate: Boolean,
    showBattery: Boolean,
    drawerRight: Boolean,
    slotsLocked: Boolean,
    slotStates: Map<String, String?>,
    allApps: List<Pair<String, String>>,
    onSettingsChange: (String, Any) -> Unit,
    onAppClick: () -> Unit,
    onReturnToHome: () -> Unit,
    onOpenSlotPicker: (String) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val listState = rememberLazyListState()
    val accentColor = Color(0xFFBB86FC)
    var chestAppsSet by remember {
        mutableStateOf(prefs.getStringSet(PREF_CHEST_APPS, emptySet()) ?: emptySet())
    }
    var selectedAppForMenu by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showMainChest by remember { mutableStateOf(false) }
    var showSlotMenu by remember { mutableStateOf(false) }
    var showChestItems by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var renameVersion by remember { mutableIntStateOf(0) }
    val visibleApps = remember(chestAppsSet, renameVersion) {
        allApps
            .filter { it.first !in chestAppsSet }
            .map { (pkg, name) ->
                val displayName = prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name
                pkg to displayName
            }
            .sortedBy { safeLower(it.second) }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
                            color = Color.White,
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
        Box(
            modifier = Modifier
                .align(if (drawerRight) Alignment.BottomStart else Alignment.BottomEnd)
                .padding(30.dp)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                .clickable { showMainChest = true }
                .padding(16.dp)
        ) {
            Text(safeLower("chest"), color = Color.Gray, fontFamily = currentFont)
        }
        if (selectedAppForMenu != null) {
            val (pkg, name) = selectedAppForMenu!!
            AlertDialog(
                onDismissRequest = { selectedAppForMenu = null },
                containerColor = Color(0xFF111111),
                title = { Text(safeLower(name), color = Color.White, fontFamily = currentFont) },
                text = {
                    Column {
                        Text(
                            text = safeLower("rename"),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showRenameDialog = pkg to name
                                    selectedAppForMenu = null
                                }
                                .padding(16.dp)
                        )
                        Text(
                            text = safeLower("send to chest"),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSet = chestAppsSet + pkg
                                    prefs.edit { putStringSet(PREF_CHEST_APPS, newSet) }
                                    chestAppsSet = newSet
                                    selectedAppForMenu = null
                                }
                                .padding(16.dp)
                        )
                        Text(
                            text = safeLower("app info"),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData("package:$pkg".toUri())
                                    )
                                    selectedAppForMenu = null
                                }
                                .padding(16.dp)
                        )
                    }
                },
                confirmButton = {}
            )
        }
        if (showMainChest) {
            AlertDialog(
                onDismissRequest = { showMainChest = false },
                containerColor = Color(0xFF111111),
                title = { Text(safeLower("CHEST"), color = Color.White, fontFamily = currentFont) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Button(
                            onClick = { showChestItems = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text(
                                text = safeLower("open chest apps"),
                                color = Color.Black,
                                fontFamily = currentFont
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = Color.DarkGray
                        )
                        Text(safeLower("UI SCALE"), color = Color.Gray, fontSize = 12.sp)
                        Slider(
                            value = globalScale,
                            onValueChange = {
                                onSettingsChange(SET_GLOBAL_SCALE, it)
                                prefs.edit { putFloat(SET_GLOBAL_SCALE, it) }
                            },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor,
                                activeTrackColor = accentColor
                            )
                        )
                        Button(
                            onClick = {
                                val next = (fontIndex + 1) % fontNames.size
                                onSettingsChange(SET_FONT_INDEX, next)
                                prefs.edit { putInt(SET_FONT_INDEX, next) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text(safeLower(fontNames[fontIndex]), fontFamily = currentFont)
                        }
                        listOf(
                            (SET_SHOW_CLOCK to "show clock") to showClock,
                            (SET_SHOW_DATE to "show date") to showDate,
                            (SET_SHOW_BATTERY to "show battery") to showBattery,
                            (SET_DRAWER_RIGHT to "drawer on right") to drawerRight,
                            (SET_SLOT_LOCK to "lock slots") to slotsLocked
                        ).forEach { pair ->
                            val (labelPair, currentValue) = pair
                            val (key, label) = labelPair
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSettingsChange(key, !currentValue)
                                        when (key) {
                                            SET_SHOW_CLOCK, SET_SHOW_DATE, SET_SHOW_BATTERY, SET_DRAWER_RIGHT, SET_SLOT_LOCK -> {
                                                prefs.edit { putBoolean(key, !currentValue) }
                                            }
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = currentValue,
                                    onCheckedChange = {
                                        onSettingsChange(key, it)
                                        when (key) {
                                            SET_SHOW_CLOCK, SET_SHOW_DATE, SET_SHOW_BATTERY, SET_DRAWER_RIGHT, SET_SLOT_LOCK -> {
                                                prefs.edit { putBoolean(key, it) }
                                            }
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = accentColor)
                                )
                                Text(
                                    text = safeLower(label),
                                    color = Color.White,
                                    fontFamily = currentFont
                                )
                            }
                        }
                        Button(
                            onClick = { showSlotMenu = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text(safeLower("assign slots"), fontFamily = currentFont)
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val versionName = remember {
                            context.packageManager
                                .getPackageInfo(context.packageName, 0).versionName
                        }
                        Text(
                            text = "v$versionName",
                            color = Color.DarkGray,
                            fontSize = 11.sp,
                            fontFamily = currentFont,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                        TextButton(onClick = { showMainChest = false }) {
                            Text(safeLower("done"), color = accentColor)
                        }
                    }
                }
            )
        }
        if (showSlotMenu) {
            AlertDialog(
                onDismissRequest = { showSlotMenu = false },
                containerColor = Color(0xFF111111),
                title = {
                    Text(safeLower("ASSIGN SLOTS"), color = Color.White, fontFamily = currentFont)
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        val slots = listOf(
                            SLOT_TOP_LEFT to "top left",
                            SLOT_MID_LEFT to "mid left",
                            SLOT_BOT_LEFT to "bot left",
                            SLOT_TOP_RIGHT to "top right",
                            SLOT_MID_RIGHT to "mid right",
                            SLOT_BOT_RIGHT to "bot right",
                            SLOT_DOUBLE_TAP to "double tap",
                            SLOT_CLOCK to "clock",
                            SLOT_DATE to "date",
                            SLOT_BATTERY to "battery"
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
                                Text(
                                    text = safeLower(label),
                                    color = accentColor,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = safeLower(selectedName),
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSlotMenu = false }) {
                        Text(safeLower("back"), color = accentColor)
                    }
                }
            )
        }
        if (showChestItems) {
            AlertDialog(
                onDismissRequest = { showChestItems = false },
                containerColor = Color(0xFF111111),
                title = {
                    Text(safeLower("CHEST APPS"), color = accentColor, fontFamily = currentFont)
                },
                text = {
                    val appsInChest = allApps
                        .filter { it.first in chestAppsSet }
                        .map { (pkg, name) ->
                            pkg to (prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name)
                        }
                        .sortedBy { safeLower(it.second) }
                    if (appsInChest.isEmpty()) {
                        Text(safeLower("chest is empty"), color = Color.Gray)
                    }
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(appsInChest.size) { i ->
                            val (pkg, displayName) = appsInChest[i]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = safeLower(displayName),
                                    color = Color.White,
                                    fontFamily = currentFont,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            launchMuDirect(context, pkg)
                                            onAppClick()
                                            showChestItems = false
                                            showMainChest = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                Text(
                                    text = safeLower("restore"),
                                    color = Color.Gray,
                                    fontFamily = currentFont,
                                    modifier = Modifier
                                        .clickable {
                                            val newSet = chestAppsSet - pkg
                                            prefs.edit { putStringSet(PREF_CHEST_APPS, newSet) }
                                            chestAppsSet = newSet
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showChestItems = false }) {
                        Text(safeLower("back"), color = accentColor)
                    }
                }
            )
        }
        if (showRenameDialog != null) {
            val (pkg, name) = showRenameDialog!!
            var text by remember(pkg) {
                mutableStateOf(
                    prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name
                )
            }
            AlertDialog(
                onDismissRequest = { showRenameDialog = null },
                containerColor = Color(0xFF111111),
                title = { Text(safeLower("rename $name"), color = Color.White) },
                text = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("new name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            prefs.edit {
                                if (text.isBlank()) {
                                    remove("${PREF_RENAME_PREFIX}$pkg")
                                } else {
                                    putString("${PREF_RENAME_PREFIX}$pkg", text)
                                }
                            }
                            renameVersion++
                            showRenameDialog = null
                        }
                    ) {
                        Text(safeLower("ok"), color = accentColor)
                    }
                }
            )
        }
    }
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
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE) }
    val intent = remember {
        Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
    }
    val apps = context.packageManager.queryIntentActivities(intent, 0)
        .map { it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() }
        .map { (pkg, name) ->
            pkg to (prefs.getString("${PREF_RENAME_PREFIX}$pkg", name) ?: name)
        }
        .sortedBy { safeLower(it.second) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111111),
        title = { Text(safeLower("set $slotName"), color = Color.White, fontFamily = currentFont) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                item {
                    Text(
                        text = safeLower("none"),
                        color = Color.Gray,
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
                        color = Color.LightGray,
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
                Text(safeLower("cancel"))
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
