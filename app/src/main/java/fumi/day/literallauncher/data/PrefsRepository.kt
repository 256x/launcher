package fumi.day.literallauncher.data

import android.content.Context
import androidx.core.content.edit

class PrefsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getGlobalScale(): Float = prefs.getFloat(SET_GLOBAL_SCALE, 1.0f)
    fun setGlobalScale(v: Float) = prefs.edit { putFloat(SET_GLOBAL_SCALE, v) }

    fun getShowClock(): Boolean = prefs.getBoolean(SET_SHOW_CLOCK, true)
    fun setShowClock(v: Boolean) = prefs.edit { putBoolean(SET_SHOW_CLOCK, v) }

    fun getShowDate(): Boolean = prefs.getBoolean(SET_SHOW_DATE, true)
    fun setShowDate(v: Boolean) = prefs.edit { putBoolean(SET_SHOW_DATE, v) }

    fun getShowBattery(): Boolean = prefs.getBoolean(SET_SHOW_BATTERY, true)
    fun setShowBattery(v: Boolean) = prefs.edit { putBoolean(SET_SHOW_BATTERY, v) }

    fun getFontIndex(): Int = prefs.getInt(SET_FONT_INDEX, 0)
    fun setFontIndex(v: Int) = prefs.edit { putInt(SET_FONT_INDEX, v) }

    fun getDrawerRight(): Boolean = prefs.getBoolean(SET_DRAWER_RIGHT, false)
    fun setDrawerRight(v: Boolean) = prefs.edit { putBoolean(SET_DRAWER_RIGHT, v) }

    fun getSlotsLocked(): Boolean = prefs.getBoolean(SET_SLOT_LOCK, false)
    fun setSlotsLocked(v: Boolean) = prefs.edit { putBoolean(SET_SLOT_LOCK, v) }

    fun getBgColor(): String = prefs.getString(PREF_BG_COLOR, "") ?: ""
    fun setBgColor(v: String) = prefs.edit { putString(PREF_BG_COLOR, v) }

    fun getTextColor(): String = prefs.getString(PREF_TEXT_COLOR, "") ?: ""
    fun setTextColor(v: String) = prefs.edit { putString(PREF_TEXT_COLOR, v) }

    fun getAccentColor(): String = prefs.getString(PREF_ACCENT_COLOR, "") ?: ""
    fun setAccentColor(v: String) = prefs.edit { putString(PREF_ACCENT_COLOR, v) }

    fun getSlot(slot: String): String? = prefs.getString(slot, null)
    fun setSlot(slot: String, pkg: String?) = prefs.edit {
        if (pkg == null) remove(slot) else putString(slot, pkg)
    }
    fun loadAllSlots(): Map<String, String?> = ALL_SLOT_IDS.associateWith { getSlot(it) }

    fun getChestApps(): Set<String> = prefs.getStringSet(PREF_CHEST_APPS, emptySet()) ?: emptySet()
    fun setChestApps(apps: Set<String>) = prefs.edit { putStringSet(PREF_CHEST_APPS, apps) }

    fun getRename(pkg: String, fallback: String): String =
        prefs.getString("$PREF_RENAME_PREFIX$pkg", fallback) ?: fallback

    fun setRename(pkg: String, name: String?) = prefs.edit {
        if (name.isNullOrBlank()) remove("$PREF_RENAME_PREFIX$pkg")
        else putString("$PREF_RENAME_PREFIX$pkg", name)
    }
}
