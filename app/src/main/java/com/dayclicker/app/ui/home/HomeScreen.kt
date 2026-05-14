package com.dayclicker.app.ui.home

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayclicker.app.DayClickerApp
import com.dayclicker.app.data.Counter
import com.dayclicker.app.data.Repository
import com.dayclicker.app.ui.note.NoteInputSheet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: Repository = (app as DayClickerApp).repository
    val counters = repo.allCounters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun increment(id: Long, note: String?) = viewModelScope.launch { repo.increment(id, note) }
    fun decrement(id: Long) = viewModelScope.launch { repo.decrement(id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenCounter: (Long) -> Unit,
    onAddCounter: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val counters by vm.counters.collectAsState()
    var noteForCounter by remember { mutableStateOf<Counter?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("DayClicker") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCounter) {
                Icon(Icons.Default.Add, contentDescription = "Add counter")
            }
        }
    ) { padding ->
        if (counters.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No counters yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to add one",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(counters, key = { it.id }) { counter ->
                    CounterRow(
                        counter = counter,
                        onCardClick = { onOpenCounter(counter.id) },
                        onPlus = {
                            if (counter.askForNoteOnPlus) {
                                noteForCounter = counter
                            } else {
                                vm.increment(counter.id, null)
                            }
                        },
                        onMinus = { vm.decrement(counter.id) }
                    )
                }
            }
        }
    }

    val targetCounter = noteForCounter
    if (targetCounter != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { noteForCounter = null },
            sheetState = sheetState
        ) {
            NoteInputSheet(
                counterName = targetCounter.name,
                projectedCount = targetCounter.currentCount + 1,
                onSubmit = { note ->
                    vm.increment(targetCounter.id, note)
                    noteForCounter = null
                },
                onSkip = {
                    vm.increment(targetCounter.id, null)
                    noteForCounter = null
                }
            )
        }
    }
}

@Composable
private fun CounterRow(
    counter: Counter,
    onCardClick: () -> Unit,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    val accent = parseHex(counter.colorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accent)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        counter.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = counter.currentCount.toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onMinus, enabled = counter.currentCount > 0) {
                    Icon(Icons.Default.Remove, contentDescription = "Subtract 1")
                }
                Spacer(Modifier.width(4.dp))
                FilledIconButton(
                    onClick = onPlus,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = accent)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add 1", tint = Color.White)
                }
            }
        }
    }
}

private fun parseHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    Color(0xFF185FA5)
}
