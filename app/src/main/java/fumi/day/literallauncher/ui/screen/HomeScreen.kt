package fumi.day.literallauncher.ui.screen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fumi.day.literallauncher.data.SLOT_BATTERY
import fumi.day.literallauncher.data.SLOT_BOT_LEFT
import fumi.day.literallauncher.data.SLOT_BOT_RIGHT
import fumi.day.literallauncher.data.SLOT_CLOCK
import fumi.day.literallauncher.data.SLOT_DATE
import fumi.day.literallauncher.data.SLOT_MID_LEFT
import fumi.day.literallauncher.data.SLOT_MID_RIGHT
import fumi.day.literallauncher.data.SLOT_TOP_LEFT
import fumi.day.literallauncher.data.SLOT_TOP_RIGHT
import fumi.day.literallauncher.util.launchMuDirect
import fumi.day.literallauncher.util.safeLower
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    screenW: Int,
    screenH: Int,
    globalScale: Float,
    currentFont: FontFamily,
    textColor: Color,
    widgetColor: Color,
    currentTime: LocalDateTime,
    batteryLevel: Int,
    showClock: Boolean,
    showDate: Boolean,
    showBattery: Boolean,
    isDrawerOpen: Boolean,
    slotStates: Map<String, String?>,
    slotsLocked: Boolean,
    onSlotLongPress: (String) -> Unit,
    onExpandNotifications: () -> Unit,
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Notification expand zone (bottom center)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectTapGestures { onExpandNotifications() }
                }
        )

        // Clock / date / battery column
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
                    color = widgetColor,
                    fontSize = (screenW * 0.07f * globalScale).sp,
                    fontFamily = currentFont,
                    modifier = Modifier.pointerInput(slotsLocked) {
                        detectTapGestures(
                            onTap = { slotStates[SLOT_CLOCK]?.let { launchMuDirect(context, it) } },
                            onLongPress = { if (!slotsLocked) onSlotLongPress(SLOT_CLOCK) }
                        )
                    }
                )
            }
            if (showDate) {
                Text(
                    text = safeLower(
                        currentTime.format(DateTimeFormatter.ofPattern("EEEE d", Locale.ENGLISH))
                    ),
                    color = widgetColor,
                    fontSize = (screenW * 0.045f * globalScale).sp,
                    fontFamily = currentFont,
                    modifier = Modifier.pointerInput(slotsLocked) {
                        detectTapGestures(
                            onTap = { slotStates[SLOT_DATE]?.let { launchMuDirect(context, it) } },
                            onLongPress = { if (!slotsLocked) onSlotLongPress(SLOT_DATE) }
                        )
                    }
                )
            }
            if (showBattery) {
                Text(
                    text = "$batteryLevel%",
                    color = widgetColor,
                    fontSize = (screenW * 0.045f * globalScale).sp,
                    fontFamily = currentFont,
                    modifier = Modifier.pointerInput(slotsLocked) {
                        detectTapGestures(
                            onTap = { slotStates[SLOT_BATTERY]?.let { launchMuDirect(context, it) } },
                            onLongPress = { if (!slotsLocked) onSlotLongPress(SLOT_BATTERY) }
                        )
                    }
                )
            }
        }

        // Corner slot zones (hidden when drawer is open)
        if (!isDrawerOpen) {
            val corners = listOf(
                SLOT_TOP_LEFT to Alignment.TopStart,
                SLOT_MID_LEFT to Alignment.CenterStart,
                SLOT_BOT_LEFT to Alignment.BottomStart,
                SLOT_TOP_RIGHT to Alignment.TopEnd,
                SLOT_MID_RIGHT to Alignment.CenterEnd,
                SLOT_BOT_RIGHT to Alignment.BottomEnd
            )
            corners.forEach { (slot, align) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.33f)
                        .fillMaxHeight(0.33f)
                        .align(align)
                        .pointerInput(slot, slotsLocked) {
                            detectTapGestures(
                                onTap = { slotStates[slot]?.let { launchMuDirect(context, it) } },
                                onLongPress = { if (!slotsLocked) onSlotLongPress(slot) }
                            )
                        }
                )
            }
        }
    }
}
