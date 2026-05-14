package com.dayclicker.app.ui.edit

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayclicker.app.DayClickerApp
import com.dayclicker.app.data.Counter
import com.dayclicker.app.data.Repository
import com.dayclicker.app.ui.theme.CounterColorPalette
import kotlinx.coroutines.launch

class EditCounterViewModel(app: Application, val counterId: Long?) : AndroidViewModel(app) {
    private val repo: Repository = (app as DayClickerApp).repository
    var name by mutableStateOf("")
    var colorHex by mutableStateOf(CounterColorPalette[3])
    var startingCount by mutableStateOf("0")
    var askForNote by mutableStateOf(true)
    var loaded by mutableStateOf(counterId == null)
    private var loadedCounter: Counter? = null

    init {
        if (counterId != null) {
            viewModelScope.launch {
                val c = repo.getCounter(counterId)
                if (c != null) {
                    loadedCounter = c
                    name = c.name
                    colorHex = c.colorHex
                    startingCount = c.currentCount.toString()
                    askForNote = c.askForNoteOnPlus
                }
                loaded = true
            }
        }
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return@launch
        val startInt = startingCount.toIntOrNull()?.coerceAtLeast(0) ?: 0
        if (counterId == null) {
            repo.createCounter(trimmed, colorHex, startInt, askForNote)
        } else {
            val existing = loadedCounter ?: return@launch
            repo.updateCounter(
                existing.copy(
                    name = trimmed,
                    colorHex = colorHex,
                    askForNoteOnPlus = askForNote
                )
            )
        }
        onDone()
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        val existing = loadedCounter ?: return@launch
        repo.deleteCounter(existing)
        onDone()
    }

    val isEdit get() = counterId != null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCounterScreen(
    counterId: Long?,
    onDone: () -> Unit
) {
    val vm: EditCounterViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                @Suppress("UNCHECKED_CAST")
                return EditCounterViewModel(app, counterId) as T
            }
        }
    )

    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vm.isEdit) "Edit counter" else "New counter") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = { vm.save(onDone) }, enabled = vm.name.isNotBlank()) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        if (!vm.loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading…")
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Name", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = vm.name,
                onValueChange = { vm.name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. Construction") }
            )

            Spacer(Modifier.height(20.dp))
            Text("Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CounterColorPalette.forEach { hex ->
                    ColorSwatch(
                        hex = hex,
                        selected = hex.equals(vm.colorHex, ignoreCase = true),
                        onClick = { vm.colorHex = hex }
                    )
                }
            }

            if (!vm.isEdit) {
                Spacer(Modifier.height(20.dp))
                Text("Starting count", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = vm.startingCount,
                    onValueChange = { v -> vm.startingCount = v.filter { it.isDigit() }.take(5) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ask for note on +1", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "When off, +1 commits silently",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = vm.askForNote, onCheckedChange = { vm.askForNote = it })
            }

            if (vm.isEdit) {
                Spacer(Modifier.height(40.dp))
                OutlinedButton(
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete counter") }
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete counter?") },
            text = { Text("This will permanently delete the counter and all its history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    vm.delete(onDone)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ColorSwatch(hex: String, selected: Boolean, onClick: () -> Unit) {
    val color = try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) { Color(0xFF185FA5) }
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}
