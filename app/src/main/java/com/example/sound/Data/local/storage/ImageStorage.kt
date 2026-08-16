package com.example.sound.Data.local.storage

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.sound.Data.local.imageStorage.ImageStorageDao
import com.example.sound.Data.local.imageStorage.ImageStorageItemEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.File
import java.security.MessageDigest

@Singleton
class ImageStorage @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val imageStorageDao: ImageStorageDao,
) {

    suspend fun saveImage(
        uri: Uri,
    ): String {
        val directory = File(context.filesDir, "images").apply {
            mkdirs()
        }
        val imageId = hashImage(uri)
        val file = File(directory, imageId)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input)
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val fileUri = file.toUri().toString()
        imageStorageDao.addNewImage(
            ImageStorageItemEntity(
                id = imageId,
                path = fileUri
            )
        )

        return fileUri
    }


    private fun hashImage(uri: Uri): String {
        val digest = MessageDigest.getInstance("SHA-256")

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input)

            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        return digest.digest().joinToString("") { byte ->
            "%02x".format(byte)
        }
    }
}