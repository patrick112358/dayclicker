package com.dayclicker.app.ui.detail

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayclicker.app.DayClickerApp
import com.dayclicker.app.data.Counter
import com.dayclicker.app.data.Entry
import com.dayclicker.app.data.Repository
import com.dayclicker.app.ui.note.NoteInputSheet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CounterDetailViewModel(app: Application, val counterId: Long) : AndroidViewModel(app) {
    private val repo: Repository = (app as DayClickerApp).repository
    val counter = repo.counterFlow(counterId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val entries = repo.entriesFlow(counterId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun increment(note: String?) = viewModelScope.launch { repo.increment(counterId, note) }
    fun decrement() = viewModelScope.launch { repo.decrement(counterId) }
    fun reset() = viewModelScope.launch { repo.reset(counterId) }
    fun updateEntryNote(e: Entry, note: String?) = viewModelScope.launch { repo.updateEntryNote(e, note) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterDetailScreen(
    counterId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val vm: CounterDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                @Suppress("UNCHECKED_CAST")
                return CounterDetailViewModel(app, counterId) as T
            }
        }
    )
    val counter by vm.counter.collectAsState()
    val entries by vm.entries.collectAsState()

    var showReset by remember { mutableStateOf(false) }
    var showNoteSheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<Entry?>(null) }
    var editedNote by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(counter?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit counter")
                    }
                }
            )
        }
    ) { padding ->
        val c = counter
        if (c == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading…")
            }
        } else {
            val accent = parseHex(c.colorHex)
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding(), bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                        Text(
                            c.currentCount.toString(),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${countThisMonth(entries)} days this month",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vm.decrement() },
                            enabled = c.currentCount > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("−1")
                        }
                        Button(
                            onClick = {
                                if (c.askForNoteOnPlus) showNoteSheet = true
                                else vm.increment(null)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("+1")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showReset = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Reset to 0") }
                    Spacer(Modifier.height(24.dp))
                    Text("History", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                }
                if (entries.isEmpty()) {
                    item {
                        Text(
                            "No entries yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(entries, key = { it.id }) { entry ->
                        EntryRow(entry, onLongPress = {
                            editingEntry = entry
                            editedNote = entry.note ?: ""
                        })
                    }
                }
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset to 0?") },
            text = { Text("Your history is preserved. The current count will be set to 0.") },
            confirmButton = {
                TextButton(onClick = {
                    showReset = false
                    vm.reset()
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel") }
            }
        )
    }

    val cur = counter
    if (showNoteSheet && cur != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showNoteSheet = false },
            sheetState = sheetState
        ) {
            NoteInputSheet(
                counterName = cur.name,
                projectedCount = cur.currentCount + 1,
                onSubmit = { vm.increment(it); showNoteSheet = false },
                onSkip = { vm.increment(null); showNoteSheet = false }
            )
        }
    }

    val editing = editingEntry
    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Edit note") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = editedNote,
                    onValueChange = { editedNote = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateEntryNote(editing, editedNote)
                    editingEntry = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryRow(entry: Entry, onLongPress: () -> Unit) {
    val df = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val date = df.format(Date(entry.timestamp))
    val badgeText = when (entry.kind) {
        Entry.KIND_INCREMENT -> "+1"
        Entry.KIND_DECREMENT -> "−1"
        Entry.KIND_RESET -> "reset"
        else -> ""
    }
    val badgeColor = when (entry.kind) {
        Entry.KIND_RESET -> Color(0xFF993C1D)
        Entry.KIND_DECREMENT -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            date,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.note ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            badgeText,
            style = MaterialTheme.typography.bodySmall,
            color = badgeColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun countThisMonth(entries: List<Entry>): Int {
    val cal = Calendar.getInstance()
    val nowMonth = cal.get(Calendar.MONTH)
    val nowYear = cal.get(Calendar.YEAR)
    return entries.count { e ->
        if (e.kind != Entry.KIND_INCREMENT) return@count false
        cal.timeInMillis = e.timestamp
        cal.get(Calendar.MONTH) == nowMonth && cal.get(Calendar.YEAR) == nowYear
    }
}

private fun parseHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    Color(0xFF185FA5)
}
