package fumi.day.literallauncher.data

const val PREFS_NAME = "launcher_prefs"

const val SLOT_CLOCK = "slot_clock"
const val SLOT_DATE = "slot_date"
const val SLOT_BATTERY = "slot_battery"
const val SLOT_TOP_LEFT = "slot_tl"
const val SLOT_TOP_RIGHT = "slot_tr"
const val SLOT_MID_LEFT = "slot_ml"
const val SLOT_MID_RIGHT = "slot_mr"
const val SLOT_BOT_LEFT = "slot_bl"
const val SLOT_BOT_RIGHT = "slot_br"
const val SLOT_DOUBLE_TAP = "slot_double"

const val SET_SHOW_CLOCK = "show_clock"
const val SET_SHOW_DATE = "show_date"
const val SET_SHOW_BATTERY = "show_battery"
const val SET_FONT_INDEX = "font_index"
const val SET_GLOBAL_SCALE = "global_scale"
const val SET_DRAWER_RIGHT = "drawer_right"
const val SET_SLOT_LOCK = "slot_lock"

const val PREF_CHEST_APPS = "chest_apps"
const val PREF_RENAME_PREFIX = "rename_"
const val PREF_BG_COLOR = "bg_color"
const val PREF_BG_TRANSPARENT = "bg_transparent"
const val PREF_TEXT_COLOR = "text_color"
const val PREF_ACCENT_COLOR = "accent_color"

val ALL_SLOT_IDS = listOf(
    SLOT_TOP_LEFT, SLOT_MID_LEFT, SLOT_BOT_LEFT,
    SLOT_TOP_RIGHT, SLOT_MID_RIGHT, SLOT_BOT_RIGHT,
    SLOT_DOUBLE_TAP, SLOT_CLOCK, SLOT_DATE, SLOT_BATTERY
)
