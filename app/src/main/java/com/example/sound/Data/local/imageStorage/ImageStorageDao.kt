package com.example.sound.Data.local.imageStorage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sound.Data.local.DatabaseTableNames

@Dao
interface ImageStorageDao {

    @Query("SELECT * FROM ${DatabaseTableNames.IMAGE_STORAGE} WHERE id = :id LIMIT 1")
    suspend fun getImageIds(id: String): ImageStorageItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNewImage(imageStorageItemEntity: ImageStorageItemEntity)

}