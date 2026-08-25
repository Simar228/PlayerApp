package com.example.sound.Domain.repository

class FakeImageRepository : ImageRepository {
    val saveImageCalls = mutableListOf<String>()
    var savedImagePath: String? = null

    override suspend fun saveImage(uri: String): String {
        saveImageCalls += uri
        return savedImagePath ?: uri
    }
}
