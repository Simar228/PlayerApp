package com.example.sound.Data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.playerstate.PlayerStateEntity
import com.example.sound.Data.local.queue.QueueItemEntity

@Database(
    entities = [
        QueueItemEntity::class,
        PlayerStateEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun queueDao(): QueueDao

    abstract fun playerStateDao(): PlayerStateDao
}