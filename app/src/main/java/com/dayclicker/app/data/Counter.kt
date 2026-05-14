package com.dayclicker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "counters")
data class Counter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val currentCount: Int = 0,
    val askForNoteOnPlus: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
