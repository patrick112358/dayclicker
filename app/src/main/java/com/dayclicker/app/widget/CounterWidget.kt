package com.dayclicker.app.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.dayclicker.app.DayClickerApp
import com.dayclicker.app.ui.note.NoteInputActivity

object WidgetKeys {
    val COUNTER_ID = longPreferencesKey("widget_counter_id")
}

class CounterWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as DayClickerApp
        val prefs: Preferences = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val counterId = prefs[WidgetKeys.COUNTER_ID] ?: -1L
        val counter = if (counterId >= 0L) app.repository.getCounter(counterId) else null

        provideContent {
            GlanceTheme {
                WidgetUi(
                    counterId = counterId,
                    counterName = counter?.name ?: "Tap to configure",
                    currentCount = counter?.currentCount ?: 0,
                    counterColorHex = counter?.colorHex ?: "#185FA5"
                )
            }
        }
    }
}

@Composable
private fun WidgetUi(
    counterId: Long,
    counterName: String,
    currentCount: Int,
    counterColorHex: String
) {
    val intent = Intent().apply {
        component = ComponentName(
            "com.dayclicker.app",
            "com.dayclicker.app.ui.note.NoteInputActivity"
        )
        putExtra(NoteInputActivity.EXTRA_COUNTER_ID, counterId)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    val accent = try {
        Color(android.graphics.Color.parseColor(counterColorHex))
    } catch (_: Exception) { Color(0xFF185FA5) }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
            .background(ColorProvider(Color.White, Color(0xFF1A1A1A)))
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(intent))
    ) {
        Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
            Box(
                modifier = GlanceModifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(ColorProvider(accent, accent))
                    .cornerRadius(2.dp)
            ) {}
            Box(modifier = GlanceModifier.height(6.dp)) {}
            Text(
                text = counterName,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color(0xFF5F5E5A), Color(0xFFB4B2A9))
                )
            )
            Box(modifier = GlanceModifier.height(4.dp)) {}
            Text(
                text = currentCount.toString(),
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color.Black, Color.White)
                )
            )
        }
    }
}
