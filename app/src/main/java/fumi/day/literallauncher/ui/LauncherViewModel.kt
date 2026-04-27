package fumi.day.literallauncher.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fumi.day.literallauncher.data.ALL_SLOT_IDS
import fumi.day.literallauncher.data.PrefsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed class LauncherEvent {
    data object CloseDrawer : LauncherEvent()
}

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PrefsRepository(app)

    // --- Time ---
    private val _currentTime = MutableStateFlow(LocalDateTime.now())
    val currentTime: StateFlow<LocalDateTime> = _currentTime.asStateFlow()

    // --- Battery ---
    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    // --- App list ---
    private val _allApps = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val allApps: StateFlow<List<Pair<String, String>>> = _allApps.asStateFlow()

    // --- One-shot UI events ---
    private val _events = MutableSharedFlow<LauncherEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<LauncherEvent> = _events.asSharedFlow()

    // --- Settings ---
    val globalScale = MutableStateFlow(repo.getGlobalScale())
    val showClock = MutableStateFlow(repo.getShowClock())
    val showDate = MutableStateFlow(repo.getShowDate())
    val showBattery = MutableStateFlow(repo.getShowBattery())
    val drawerRight = MutableStateFlow(repo.getDrawerRight())
    val slotsLocked = MutableStateFlow(repo.getSlotsLocked())
    val fontIndex = MutableStateFlow(repo.getFontIndex())
    val bgColorHex = MutableStateFlow(repo.getBgColor())
    val bgTransparent = MutableStateFlow(repo.getBgTransparent())
    val textColorHex = MutableStateFlow(repo.getTextColor())
    val accentColorHex = MutableStateFlow(repo.getAccentColor())

    // --- Slot assignments ---
    private val _slotStates = MutableStateFlow(repo.loadAllSlots())
    val slotStates: StateFlow<Map<String, String?>> = _slotStates.asStateFlow()

    // --- Chest ---
    private val _chestApps = MutableStateFlow(repo.getChestApps())
    val chestApps: StateFlow<Set<String>> = _chestApps.asStateFlow()

    // --- Rename version: incrementing triggers recomposition in consumers ---
    private val _renameVersion = MutableStateFlow(0)
    val renameVersion: StateFlow<Int> = _renameVersion.asStateFlow()

    // Persistent battery receiver (registered once, not polled)
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
            }
        }
    }

    // Persistent package-change receiver
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            refreshApps()
            if (intent.action == Intent.ACTION_PACKAGE_REMOVED) {
                _events.tryEmit(LauncherEvent.CloseDrawer)
            }
        }
    }

    init {
        getApplication<Application>().registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        getApplication<Application>().registerReceiver(packageReceiver, pkgFilter)

        refreshApps()

        viewModelScope.launch {
            while (true) {
                _currentTime.value = LocalDateTime.now()
                delay(10_000)
            }
        }
    }

    fun refreshApps() {
        val pm = getApplication<Application>().packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        _allApps.value = pm.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
        _events.tryEmit(LauncherEvent.CloseDrawer)
    }

    // Settings update functions
    fun setGlobalScale(v: Float) { globalScale.value = v; repo.setGlobalScale(v) }
    fun setShowClock(v: Boolean) { showClock.value = v; repo.setShowClock(v) }
    fun setShowDate(v: Boolean) { showDate.value = v; repo.setShowDate(v) }
    fun setShowBattery(v: Boolean) { showBattery.value = v; repo.setShowBattery(v) }
    fun setDrawerRight(v: Boolean) { drawerRight.value = v; repo.setDrawerRight(v) }
    fun setSlotsLocked(v: Boolean) { slotsLocked.value = v; repo.setSlotsLocked(v) }
    fun setFontIndex(v: Int) { fontIndex.value = v; repo.setFontIndex(v) }
    fun setBgColor(v: String) { bgColorHex.value = v; repo.setBgColor(v) }
    fun setBgTransparent(v: Boolean) { bgTransparent.value = v; repo.setBgTransparent(v) }
    fun setTextColor(v: String) { textColorHex.value = v; repo.setTextColor(v) }
    fun setAccentColor(v: String) { accentColorHex.value = v; repo.setAccentColor(v) }

    fun setSlot(slot: String, pkg: String?) {
        repo.setSlot(slot, pkg)
        _slotStates.value = _slotStates.value + (slot to pkg)
    }

    fun addToChest(pkg: String) {
        val new = _chestApps.value + pkg
        repo.setChestApps(new)
        _chestApps.value = new
    }

    fun removeFromChest(pkg: String) {
        val new = _chestApps.value - pkg
        repo.setChestApps(new)
        _chestApps.value = new
    }

    fun setRename(pkg: String, name: String?) {
        repo.setRename(pkg, name)
        _renameVersion.value++
    }

    fun getRename(pkg: String, fallback: String): String = repo.getRename(pkg, fallback)

    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(batteryReceiver)
        getApplication<Application>().unregisterReceiver(packageReceiver)
    }
}
