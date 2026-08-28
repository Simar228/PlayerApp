package com.example.sound.Data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.imageStorage.ImageStorageDao
import com.example.sound.Data.local.imageStorage.ImageStorageItemEntity
import com.example.sound.Data.local.storage.ImageStorage
import com.example.sound.utill.InMemoryDatabaseRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ImageRepositoryImplIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var dao: ImageStorageDao
    private lateinit var imageStorage: ImageStorage

    private lateinit var sut: ImageRepositoryImpl

    @get:Rule
    val inMemoryDatabaseRule = InMemoryDatabaseRule(AppDatabase::class.java)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = inMemoryDatabaseRule.database
        dao = database.imageStorageDao()
        imageStorage = ImageStorage(context)

        sut = ImageRepositoryImpl(
            imageStorageDao = dao,
            imageStorage = imageStorage,
        )
    }

    @After
    fun tearDown() {
        File(context.filesDir, "images")
            .deleteRecursively()
    }

    @Test
    fun saveImage_createsNewImageWhenImageDoesNotExist() = runTest {
        val sourceFile = File(context.cacheDir, "test_image").apply {
            writeText("test image content")
        }

        val expectedId = imageStorage.hashImage(
            sourceFile.toUri().toString()
        )


        val result = sut.saveImage(
            sourceFile.toUri().toString()
        )


        val savedImage = dao.getImageIds(expectedId)

        assertThat(savedImage).isNotNull()
        assertThat(savedImage!!.id).isEqualTo(expectedId)
        assertThat(savedImage.path).isEqualTo(result)

        val savedFile = File(
            Uri.parse(result).path!!
        )

        assertThat(savedFile.exists()).isTrue()
        assertThat(savedFile.readText())
            .isEqualTo("test image content")
    }

    @Test
    fun saveImage_returnsExistingImagePathWhenImageAlreadyExists() = runTest {
        val sourceFile = File(context.cacheDir, "test_image").apply {
            writeText("test image content")
        }

        val imageId = imageStorage.hashImage(
            sourceFile.toUri().toString()
        )

        val existingPath = "file:///existing/image.jpg"

        dao.addNewImage(
            ImageStorageItemEntity(
                id = imageId,
                path = existingPath,
            )
        )

        val result = sut.saveImage(
            sourceFile.toUri().toString()
        )

        assertThat(result).isEqualTo(existingPath)

        val imagesDirectory = File(
            context.filesDir,
            "images"
        )

        val newFile = File(
            imagesDirectory,
            imageId
        )

        assertThat(newFile.exists()).isFalse()
    }
}