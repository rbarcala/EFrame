package com.antigravity.eframe.modules

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClockModule(
    private val getIsDetailed: () -> Boolean = { true },
    private val getUiScale: () -> Float = { 1.6f },
    override var isEnabled: Boolean = true
) : FrameModule {

    override val id: String = "module_clock"
    override val name: String = "Reloj y Fecha"
    override val description: String = "Hora actual y fecha en alta legibilidad"
    override val icon: ImageVector = Icons.Default.Schedule
    override val defaultPosition: ModulePosition = ModulePosition.TOP_START

    @Composable
    override fun Content(modifier: Modifier, onClick: (() -> Unit)?) {
        val scale = getUiScale()
        val isDetailed = getIsDetailed()

        val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")) }
        val dateStr = remember { dateFormat.format(Date()).replaceFirstChar { it.uppercase() } }

        val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
        val greeting = when (hour) {
            in 6..12 -> "Buenos días"
            in 13..19 -> "Buenas tardes"
            else -> "Buenas noches"
        }

        // Native AndroidView rendering: 0 Compose recomposition overhead
        AndroidView(
            modifier = modifier
                .clip(RoundedCornerShape((18 * scale).dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.40f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape((18 * scale).dp)
                )
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = (18 * scale).dp, vertical = (12 * scale).dp),
            factory = { context ->
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.START

                    // 1. Greeting
                    if (isDetailed) {
                        val greetingView = TextView(context).apply {
                            text = greeting
                            setTextColor(android.graphics.Color.parseColor("#FFD54F"))
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 * scale)
                            typeface = Typeface.DEFAULT_BOLD
                            setPadding(0, 0, 0, (2 * scale).toInt())
                        }
                        addView(greetingView)
                    }

                    // 2. Native TextClock (updates in native C++ layer without recomposing Compose)
                    val textClock = TextClock(context).apply {
                        format12Hour = "hh:mm:ss a"
                        format24Hour = "HH:mm:ss"
                        setTextColor(android.graphics.Color.WHITE)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 36 * scale)
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    addView(textClock)

                    // 3. Date
                    val dateView = TextView(context).apply {
                        text = dateStr
                        setTextColor(android.graphics.Color.parseColor("#E0E0E0"))
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13 * scale)
                    }
                    addView(dateView)
                }
            },
            update = { layout ->
                // Fast layout update if scale or detail changes
            }
        )
    }
}
