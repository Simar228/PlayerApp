package com.example.sound.Data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.playerstate.PlayerStateEntity
import com.example.sound.Data.local.queue.QueueItemEntity

@Database(
    entities = [
        QueueItemEntity::class,
        PlayerStateEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun queueDao(): QueueDao

    abstract fun playerStateDao(): PlayerStateDao
    companion object {
        val MIGRATION_1_2 = Migration(1, 2) { database ->
            database.execSQL(
                "ALTER TABLE queue_items " +
                        "ADD COLUMN songId TEXT NOT NULL DEFAULT ''"
            )

            database.execSQL("DELETE FROM queue_items")
            database.execSQL("DELETE FROM player_state")
        }
    }
}