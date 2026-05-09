package fumi.day.literallauncher.ui

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import fumi.day.literallauncher.R
import fumi.day.literallauncher.data.SLOT_DOUBLE_TAP
import fumi.day.literallauncher.ui.component.AppPickerDialog
import fumi.day.literallauncher.ui.screen.AppDrawerScreen
import fumi.day.literallauncher.ui.screen.HomeScreen
import fumi.day.literallauncher.ui.screen.SettingsScreen
import fumi.day.literallauncher.util.expandNotifications
import fumi.day.literallauncher.util.findActivity
import fumi.day.literallauncher.util.launchMuDirect
import fumi.day.literallauncher.util.parseColor
import fumi.day.literallauncher.util.safeLower

@Composable
fun LiteralLauncherApp(vm: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val windowInfo = LocalWindowInfo.current
    val containerSize = windowInfo.containerSize
    val density = LocalDensity.current
    val screenW = with(density) { containerSize.width.toDp().value.toInt() }
    val screenH = with(density) { containerSize.height.toDp().value.toInt() }

    // Collect state from ViewModel
    val currentTime by vm.currentTime.collectAsState()
    val batteryLevel by vm.batteryLevel.collectAsState()
    val allApps by vm.allApps.collectAsState()
    val slotStates by vm.slotStates.collectAsState()
    val renameVersion by vm.renameVersion.collectAsState()
    val globalScale by vm.globalScale.collectAsState()
    val showClock by vm.showClock.collectAsState()
    val showDate by vm.showDate.collectAsState()
    val showBattery by vm.showBattery.collectAsState()
    val slotsLocked by vm.slotsLocked.collectAsState()
    val fontIndex by vm.fontIndex.collectAsState()
    val bgColorHex by vm.bgColorHex.collectAsState()
    val bgTransparent by vm.bgTransparent.collectAsState()
    val textColorHex by vm.textColorHex.collectAsState()
    val accentColorHex by vm.accentColorHex.collectAsState()
    val widgetColorHex by vm.widgetColorHex.collectAsState()

    // Derived (cached)
    val fontFamilies = remember { listOf(FontFamily.SansSerif, FontFamily.Serif, FontFamily.Monospace, FontFamily(Font(R.font.scopeone))) }
    val currentFont = fontFamilies[fontIndex]
    val bgColor = remember(bgColorHex) { parseColor(bgColorHex) ?: Color.Black }
    val textColor = remember(textColorHex) { parseColor(textColorHex) ?: Color.White }
    val accentColor = remember(accentColorHex) { parseColor(accentColorHex) ?: Color(0xFFBB86FC) }
    val widgetColor = remember(widgetColorHex, textColor) { parseColor(widgetColorHex) ?: textColor }
    val effectiveBgColor = if (bgTransparent) Color.Transparent else bgColor

    SideEffect {
        val activity = context.findActivity()
        if (activity != null) {
            if (bgTransparent) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            }
        }
    }

    // Apps with renames applied, sorted — recomputed only when app list or renames change
    val appsWithRenames = remember(allApps, renameVersion) {
        allApps
            .map { (pkg, name) -> pkg to vm.getRename(pkg, name) }
            .sortedBy { safeLower(it.second) }
    }

    val updatedSlotStates = rememberUpdatedState(slotStates)

    // UI state
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showPickerSlot by remember { mutableStateOf<String?>(null) }

    // Collect one-shot events from ViewModel (e.g. close drawer on app install/resume)
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                LauncherEvent.CloseDrawer -> isDrawerOpen = false
            }
        }
    }

    BackHandler(enabled = isDrawerOpen || showSettings) {
        if (showSettings) showSettings = false else isDrawerOpen = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(effectiveBgColor)
            // Swipe-up opens drawer
            .pointerInput(Unit) {
                var swipeStartY = 0f
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        if (changes.isEmpty()) continue
                        val change = changes.first()
                        when {
                            change.pressed && !change.previousPressed ->
                                swipeStartY = change.position.y
                            !change.pressed && change.previousPressed ->
                                if (change.position.y - swipeStartY < -200f) isDrawerOpen = true
                        }
                    }
                }
            }
            // Double-tap / long-press on background
            .pointerInput(slotsLocked) {
                detectTapGestures(
                    onDoubleTap = { updatedSlotStates.value[SLOT_DOUBLE_TAP]?.let { launchMuDirect(context, it) } },
                    onLongPress = { if (!slotsLocked) showPickerSlot = SLOT_DOUBLE_TAP }
                )
            }
    ) {
        HomeScreen(
            screenW = screenW,
            screenH = screenH,
            globalScale = globalScale,
            currentFont = currentFont,
            textColor = textColor,
            widgetColor = widgetColor,
            currentTime = currentTime,
            batteryLevel = batteryLevel,
            showClock = showClock,
            showDate = showDate,
            showBattery = showBattery,
            isDrawerOpen = isDrawerOpen,
            slotStates = slotStates,
            slotsLocked = slotsLocked,
            onSlotLongPress = { showPickerSlot = it },
            onExpandNotifications = { expandNotifications(context) }
        )

        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            AppDrawerScreen(
                viewModel = vm,
                screenW = screenW,
                globalScale = globalScale,
                currentFont = currentFont,
                bgColor = bgColor,
                textColor = textColor,
                accentColor = accentColor,
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
            SettingsScreen(
                viewModel = vm,
                currentFont = currentFont,
                bgColor = bgColor,
                textColor = textColor,
                accentColor = accentColor,
                widgetColor = widgetColor,
                onOpenSlotPicker = { showPickerSlot = it },
                onDismiss = { showSettings = false }
            )
        }

        showPickerSlot?.let { slotKey ->
            AppPickerDialog(
                slotName = slotKey,
                currentFont = currentFont,
                globalScale = globalScale,
                bgColor = bgColor,
                textColor = textColor,
                accentColor = accentColor,
                apps = appsWithRenames,
                onDismiss = { showPickerSlot = null },
                onSelect = { pkg ->
                    vm.setSlot(slotKey, pkg)
                    showPickerSlot = null
                }
            )
        }
    }
}
