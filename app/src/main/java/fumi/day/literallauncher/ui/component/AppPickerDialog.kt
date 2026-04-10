package fumi.day.literallauncher.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fumi.day.literallauncher.util.safeLower

@Composable
fun AppPickerDialog(
    slotName: String,
    currentFont: FontFamily,
    globalScale: Float,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    apps: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        title = { Text(safeLower("set $slotName"), color = textColor, fontFamily = currentFont) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                item(key = "__none__") {
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
                items(apps, key = { it.first }) { (pkg, displayName) ->
                    Text(
                        text = safeLower(displayName),
                        color = textColor,
                        fontSize = (16 * globalScale).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(pkg) }
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
