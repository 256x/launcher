package fumi.day.literallauncher.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.Locale

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun parseColor(hex: String): Color? = try {
    if (hex.isBlank()) null
    else Color(android.graphics.Color.parseColor(hex))
} catch (_: IllegalArgumentException) { null }

fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}

fun safeLower(text: String): String = text.lowercase(Locale.ENGLISH)

enum class TextCase(val storageValue: Int) {
    LOWERCASE(0), UPPERCASE(1), TITLECASE(2);
    companion object {
        fun fromStorageValue(v: Int): TextCase = entries.find { it.storageValue == v } ?: LOWERCASE
    }
}

fun applyTextCase(text: String, case: TextCase): String = when (case) {
    TextCase.LOWERCASE -> text.lowercase(Locale.ENGLISH)
    TextCase.UPPERCASE -> text.uppercase(Locale.ENGLISH)
    TextCase.TITLECASE -> text.lowercase(Locale.ENGLISH)
        .split(" ")
        .joinToString(" ") { w -> w.replaceFirstChar { it.titlecase(Locale.ENGLISH) } }
}
