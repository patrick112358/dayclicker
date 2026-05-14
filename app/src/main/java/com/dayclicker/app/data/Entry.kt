package com.dayclicker.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries",
    foreignKeys = [ForeignKey(
        entity = Counter::class,
        parentColumns = ["id"],
        childColumns = ["counterId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("counterId"), Index("timestamp")]
)
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val counterId: Long,
    val timestamp: Long,
    val delta: Int,
    val note: String? = null,
    val kind: String
) {
    companion object {
        const val KIND_INCREMENT = "increment"
        const val KIND_DECREMENT = "decrement"
        const val KIND_RESET = "reset"
    }
}
