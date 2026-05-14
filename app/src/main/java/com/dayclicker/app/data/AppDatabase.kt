package com.dayclicker.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Counter::class, Entry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun counterDao(): CounterDao
    abstract fun entryDao(): EntryDao
}
