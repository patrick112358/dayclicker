package com.dayclicker.app.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionRunCallback
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.dayclicker.app.DayClickerApp
import com.dayclicker.app.ui.note.NoteInputActivity

object WidgetKeys {
    val COUNTER_ID = longPreferencesKey("widget_counter_id")
}

val counterIdActionKey = ActionParameters.Key<Long>(NoteInputActivity.EXTRA_COUNTER_ID)

class DecrementWidgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val counterId = parameters[counterIdActionKey] ?: return
        val app = context.applicationContext as DayClickerApp
        app.repository.decrement(counterId)
    }
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
    val accent = try {
        Color(android.graphics.Color.parseColor(counterColorHex))
    } catch (_: Exception) { Color(0xFF185FA5) }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp)
            .background(ColorProvider(Color.White, Color(0xFF1A1A1A)))
            .cornerRadius(16.dp)
            .padding(10.dp)
    ) {
        // Accent bar + label
        Box(
            modifier = GlanceModifier
                .width(28.dp)
                .height(3.dp)
                .background(ColorProvider(accent, accent))
                .cornerRadius(2.dp)
        ) {}
        Box(modifier = GlanceModifier.height(4.dp)) {}
        Text(
            text = counterName,
            style = TextStyle(
                fontSize = 11.sp,
                color = ColorProvider(Color(0xFF5F5E5A), Color(0xFFB4B2A9))
            )
        )
        // Count — fills remaining space
        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = currentCount.toString(),
                style = TextStyle(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color.Black, Color.White)
                )
            )
        }
        // Button row
        Row(modifier = GlanceModifier.fillMaxWidth().wrapContentHeight()) {
            // −1 button
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(36.dp)
                    .cornerRadius(8.dp)
                    .background(ColorProvider(Color(0xFFF0EFEC), Color(0xFF2A2A2A)))
                    .clickable(
                        actionRunCallback<DecrementWidgetAction>(
                            actionParametersOf(counterIdActionKey to counterId)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−1",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(Color(0xFF3C3C3C), Color(0xFFCCCCCC)),
                        textAlign = TextAlign.Center
                    )
                )
            }
            Box(modifier = GlanceModifier.width(8.dp)) {}
            // +1 button
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(36.dp)
                    .cornerRadius(8.dp)
                    .background(ColorProvider(accent, accent))
                    .clickable(
                        actionStartActivity(
                            ComponentName(
                                "com.dayclicker.app",
                                "com.dayclicker.app.ui.note.NoteInputActivity"
                            ),
                            actionParametersOf(counterIdActionKey to counterId)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+1",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(Color.White, Color.White),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
