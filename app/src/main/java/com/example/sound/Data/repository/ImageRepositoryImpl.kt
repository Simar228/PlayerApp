package com.example.sound.Data.repository

import com.example.sound.Data.local.imageStorage.ImageStorageDao
import com.example.sound.Data.local.imageStorage.ImageStorageItemEntity
import com.example.sound.Data.local.storage.ImageStorage
import com.example.sound.Domain.repository.ImageRepository
import jakarta.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageStorageDao: ImageStorageDao,
    private val imageStorage: ImageStorage,
) : ImageRepository {

    override suspend fun saveImage(uri: String): String {
        val imageId = imageStorage.hashImage(uri)

        imageStorageDao.getImageIds(imageId)?.let {
            return it.path
        }

        val path = imageStorage.saveImage(uri, imageId)

        imageStorageDao.addNewImage(
            ImageStorageItemEntity(
                id = imageId,
                path = path,
            )
        )

        return path
    }
}

