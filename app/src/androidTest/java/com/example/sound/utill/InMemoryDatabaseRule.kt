package com.example.sound.utill

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class InMemoryDatabaseRule<T : RoomDatabase>(
    private val databaseClass: Class<T>
) : TestWatcher() {

    lateinit var database: T
        private set

    override fun starting(description: Description) {
        super.starting(description)
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(context, databaseClass)
            .allowMainThreadQueries()
            .build()
    }

    override fun finished(description: Description) {
        super.finished(description)
        database.close()
    }
}