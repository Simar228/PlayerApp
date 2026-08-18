package com.example.sound.Domain.repository

interface ImageRepository {
    suspend fun saveImage(uri: String): String
}