package com.dayclicker.app

import android.app.Application
import androidx.room.Room
import com.dayclicker.app.data.AppDatabase
import com.dayclicker.app.data.Repository

class DayClickerApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "dayclicker.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository: Repository by lazy {
        Repository(database.counterDao(), database.entryDao(), applicationContext)
    }
}
