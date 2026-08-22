package com.example.sound.Data.local.imageStorage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames


@Entity(
    tableName = DatabaseTableNames.IMAGE_STORAGE,
)
data class ImageStorageItemEntity(

    @PrimaryKey
    val id: String,

    val path: String,
)