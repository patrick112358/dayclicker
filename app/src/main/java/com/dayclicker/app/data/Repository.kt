package com.dayclicker.app.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.dayclicker.app.widget.CounterWidget
import com.dayclicker.app.widget.CounterWidgetReceiver
import kotlinx.coroutines.flow.Flow

class Repository(
    private val counterDao: CounterDao,
    private val entryDao: EntryDao,
    private val appContext: Context
) {
    val allCounters: Flow<List<Counter>> = counterDao.observeAll()

    fun counterFlow(id: Long): Flow<Counter?> = counterDao.observeById(id)
    fun entriesFlow(counterId: Long): Flow<List<Entry>> = entryDao.observeForCounter(counterId)

    suspend fun getCounter(id: Long): Counter? = counterDao.getById(id)
    suspend fun getAllCountersOnce(): List<Counter> = counterDao.getAllOnce()

    suspend fun createCounter(name: String, colorHex: String, startCount: Int, askForNote: Boolean): Long {
        val id = counterDao.insert(
            Counter(name = name, colorHex = colorHex, currentCount = startCount, askForNoteOnPlus = askForNote)
        )
        refreshWidgets()
        return id
    }

    suspend fun updateCounter(counter: Counter) {
        counterDao.update(counter)
        refreshWidgets()
    }

    suspend fun deleteCounter(counter: Counter) {
        counterDao.delete(counter)
        refreshWidgets()
    }

    suspend fun increment(counterId: Long, note: String?) {
        val c = counterDao.getById(counterId) ?: return
        val newCount = c.currentCount + 1
        counterDao.setCount(counterId, newCount)
        entryDao.insert(
            Entry(
                counterId = counterId,
                timestamp = System.currentTimeMillis(),
                delta = 1,
                note = note?.takeIf { it.isNotBlank() },
                kind = Entry.KIND_INCREMENT
            )
        )
        refreshWidgets()
    }

    suspend fun decrement(counterId: Long) {
        val c = counterDao.getById(counterId) ?: return
        if (c.currentCount <= 0) return
        val newCount = c.currentCount - 1
        counterDao.setCount(counterId, newCount)
        entryDao.insert(
            Entry(
                counterId = counterId,
                timestamp = System.currentTimeMillis(),
                delta = -1,
                note = null,
                kind = Entry.KIND_DECREMENT
            )
        )
        refreshWidgets()
    }

    suspend fun reset(counterId: Long) {
        val c = counterDao.getById(counterId) ?: return
        val previous = c.currentCount
        counterDao.setCount(counterId, 0)
        entryDao.insert(
            Entry(
                counterId = counterId,
                timestamp = System.currentTimeMillis(),
                delta = -previous,
                note = "Monthly reset · was $previous",
                kind = Entry.KIND_RESET
            )
        )
        refreshWidgets()
    }

    suspend fun updateEntryNote(entry: Entry, note: String?) {
        entryDao.update(entry.copy(note = note?.takeIf { it.isNotBlank() }))
    }

    private suspend fun refreshWidgets() {
        try {
            CounterWidget().updateAll(appContext)
        } catch (_: Exception) {}
        // Belt-and-suspenders: standard broadcast pipeline guarantees update
        // even if Glance's internal scheduler is delayed
        try {
            val manager = AppWidgetManager.getInstance(appContext)
            val ids = manager.getAppWidgetIds(
                ComponentName(appContext, CounterWidgetReceiver::class.java)
            )
            if (ids.isNotEmpty()) {
                appContext.sendBroadcast(
                    Intent(appContext, CounterWidgetReceiver::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                )
            }
        } catch (_: Exception) {}
    }
}
