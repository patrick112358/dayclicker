package com.dayclicker.app.ui.note

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.dayclicker.app.DayClickerApp
import com.dayclicker.app.data.Counter
import com.dayclicker.app.ui.theme.DayClickerTheme
import kotlinx.coroutines.launch

class NoteInputActivity : ComponentActivity() {

    companion object {
        const val EXTRA_COUNTER_ID = "counterId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(incomingIntent: Intent) {
        val counterId = incomingIntent.getLongExtra(EXTRA_COUNTER_ID, -1L)
        if (counterId < 0L) {
            finish()
            return
        }

        val app = applicationContext as DayClickerApp

        lifecycleScope.launch {
            val counter = app.repository.getCounter(counterId)
            if (counter == null) {
                finish()
                return@launch
            }
            if (!counter.askForNoteOnPlus) {
                app.repository.increment(counterId, null)
                setResult(Activity.RESULT_OK)
                finish()
                return@launch
            }
            renderNoteUi(counter)
        }
    }

    private fun renderNoteUi(counter: Counter) {
        setContent {
            DayClickerTheme {
                NoteInputContent(
                    counter = counter,
                    onCommit = { note ->
                        val app = applicationContext as DayClickerApp
                        lifecycleScope.launch {
                            app.repository.increment(counter.id, note)
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@Composable
private fun NoteInputContent(
    counter: Counter,
    onCommit: (String?) -> Unit,
    onCancel: () -> Unit
) {
    val scrimInteraction = remember { MutableInteractionSource() }
    val sheetInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(
                interactionSource = scrimInteraction,
                indication = null,
                onClick = onCancel
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = sheetInteraction,
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            NoteInputSheet(
                counterName = counter.name,
                projectedCount = counter.currentCount + 1,
                onSubmit = { onCommit(it) },
                onSkip = { onCommit(null) }
            )
        }
    }
}
