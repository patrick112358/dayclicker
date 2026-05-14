package com.dayclicker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {

    @Query("SELECT * FROM counters ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<Counter>>

    @Query("SELECT * FROM counters WHERE id = :id")
    fun observeById(id: Long): Flow<Counter?>

    @Query("SELECT * FROM counters WHERE id = :id")
    suspend fun getById(id: Long): Counter?

    @Query("SELECT * FROM counters ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAllOnce(): List<Counter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(counter: Counter): Long

    @Update
    suspend fun update(counter: Counter)

    @Delete
    suspend fun delete(counter: Counter)

    @Query("UPDATE counters SET currentCount = :newCount WHERE id = :id")
    suspend fun setCount(id: Long, newCount: Int)
}

@Dao
interface EntryDao {

    @Query("SELECT * FROM entries WHERE counterId = :counterId ORDER BY timestamp DESC LIMIT 500")
    fun observeForCounter(counterId: Long): Flow<List<Entry>>

    @Insert
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Query("DELETE FROM entries WHERE counterId = :counterId")
    suspend fun deleteForCounter(counterId: Long)
}
