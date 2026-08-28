package com.example.sound.Data.local.storage


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import androidx.core.net.toUri

@RunWith(AndroidJUnit4::class)
class ImageStorageTest {

    private lateinit var context: Context
    private lateinit var sut: ImageStorage
    private lateinit var sourceFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        sut = ImageStorage(context)

        sourceFile = File(context.cacheDir, "test_image.jpg").apply {
            writeBytes(TEST_IMAGE_BYTES)
        }
    }

    @After
    fun tearDown() {
        sourceFile.delete()

        File(context.filesDir, "images")
            .deleteRecursively()
    }

    @Test
    fun hashImage_returnsCorrectSha256() {
        val uri = sourceFile.toUri().toString()

        val result = sut.hashImage(uri)

        assertThat(result)
            .isEqualTo(
                "03754271b00a0e1c761384c9dd0e7575ae942ae4c0952cf4b64da85f7c168307"
            )
    }

    @Test
    fun hashImage_returnsSameHashForSameContent() {
        val uri = sourceFile.toUri().toString()

        val firstHash = sut.hashImage(uri)
        val secondHash = sut.hashImage(uri)

        assertThat(firstHash).isEqualTo(secondHash)
    }

    @Test
    fun hashImage_returnsDifferentHashForDifferentContent() {
        val firstUri = sourceFile.toUri().toString()

        val secondFile = File(context.cacheDir, "test_image_2.jpg").apply {
            writeBytes("different content".toByteArray())
        }

        try {
            val firstHash = sut.hashImage(firstUri)
            val secondHash = sut.hashImage(secondFile.toURI().toString())

            assertThat(firstHash).isNotEqualTo(secondHash)
        } finally {
            secondFile.delete()
        }
    }

    @Test
    fun saveImage_savesImageWithCorrectContent() {
        val imageId = "test_image_id"
        val uri = sourceFile.toURI().toString()

        val result = sut.saveImage(
            uri = uri,
            imageId = imageId
        )

        val savedFile = File(
            context.filesDir,
            "images/$imageId"
        )

        assertThat(savedFile.exists()).isTrue()
        assertThat(savedFile.readBytes())
            .isEqualTo(TEST_IMAGE_BYTES)

        assertThat(result)
            .isEqualTo(savedFile.toUri().toString())
    }

    @Test
    fun saveImage_createsImagesDirectory() {
        val directory = File(context.filesDir, "images")
        directory.deleteRecursively()

        assertThat(directory.exists()).isFalse()

        sut.saveImage(
            uri = sourceFile.toUri().toString(),
            imageId = "test_image_id"
        )

        assertThat(directory.exists()).isTrue()
        assertThat(directory.isDirectory).isTrue()
    }

    companion object {
        private val TEST_IMAGE_BYTES =
            "fake image content".toByteArray()
    }
}