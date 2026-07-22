package com.example.sound.Data.local.defualtQueue

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "defaultQueue_items")
class DefaultQueueDao (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val songId: String,
    val songUri: String,
    val position: Int,
    val title: String?,
    val artist: String?,
    val duration: Long,
    val album: String?,
    val genre: String?,
    val artUri: String?
)