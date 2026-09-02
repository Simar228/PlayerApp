package com.example.sound.Data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sound.Data.local.genre.GenreDao
import com.example.sound.Data.local.genre.GenreEntity
import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.DefaultQueueItemEntity
import com.example.sound.Data.local.editSong.EditSongDao
import com.example.sound.Data.local.editSong.EditSongItemEntity
import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Data.local.historyQueue.HistoryQueueItemEntity
import com.example.sound.Data.local.imageStorage.ImageStorageDao
import com.example.sound.Data.local.imageStorage.ImageStorageItemEntity
import com.example.sound.Data.local.playerState.PlayerStateDao
import com.example.sound.Data.local.playerState.PlayerStateEntity
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.queue.QueueItemEntity

@Database(
    entities = [
        QueueItemEntity::class,
        PlayerStateEntity::class,
        DefaultQueueItemEntity::class,
        GenreEntity::class,
        EditSongItemEntity::class,
        ImageStorageItemEntity::class,
        HistoryQueueItemEntity::class,
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyQueueDao(): HistoryQueueDao
    abstract fun imageStorageDao(): ImageStorageDao
    abstract fun editSongDao(): EditSongDao

    abstract fun genreDao(): GenreDao

    abstract fun defaultQueueDao(): DefaultQueueDao

    abstract fun queueDao(): QueueDao

    abstract fun playerStateDao(): PlayerStateDao
}
