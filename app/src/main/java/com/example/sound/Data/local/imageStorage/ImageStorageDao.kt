package com.example.sound.Data.local.imageStorage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface ImageStorageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNewImage(imageStorageItemEntity: ImageStorageItemEntity)

}