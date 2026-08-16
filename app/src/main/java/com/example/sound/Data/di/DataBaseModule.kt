package com.example.sound.Data.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.Genre.GenreDao
import com.example.sound.Data.local.Genre.GenreEntity
import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.editSong.EditSongDao
import com.example.sound.Data.local.imageStorage.ImageStorageDao
import com.example.sound.Data.local.playerState.PlayerStateDao
import com.example.sound.Data.local.queue.QueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sound_database"
        )
            .build()
    }

    class DatabaseInitializer @Inject constructor(
        private val genreDao: GenreDao
    ) {
        suspend fun initialize() {
            val result = genreDao.insertGenres(
                listOf(
                    GenreEntity(name = "Поп", isSystem = true),
                    GenreEntity(name = "Рок", isSystem = true),
                    GenreEntity(name = "Хип-хоп", isSystem = true),
                    GenreEntity(name = "Электронная музыка", isSystem = true),
                    GenreEntity(name = "Джаз", isSystem = true),
                    GenreEntity(name = "Классическая музыка", isSystem = true),
                    GenreEntity(name = "Рэп", isSystem = true),
                    GenreEntity(name = "Инди", isSystem = true),
                    GenreEntity(name = "Метал", isSystem = true),
                    GenreEntity(name = "РНБ", isSystem = true),
                    GenreEntity(name = "Фолк", isSystem = true),
                    GenreEntity(name = "Блюз", isSystem = true),
                    GenreEntity(name = "Соул", isSystem = true),
                    GenreEntity(name = "Регги", isSystem = true),
                    GenreEntity(name = "Кантри", isSystem = true),
                )
            )
            Log.d("DB", result.toString())
        }
    }

    @Provides
    fun provideEditSongDao(database: AppDatabase): EditSongDao{
        return database.editSongDao()
    }
    @Provides
    fun provideImageStorageDao(database: AppDatabase): ImageStorageDao{
        return database.imageStorageDao()
    }
    @Provides
    fun provideGenreDao(database: AppDatabase): GenreDao {
        return database.genreDao()
    }
    @Provides
    fun provideDefaultQueueDao(database: AppDatabase): DefaultQueueDao {
        return database.defaultQueueDao()
    }
    @Provides
    fun provideQueueDao(database: AppDatabase): QueueDao {
        return database.queueDao()
    }

    @Provides
    fun providePlayerStateDao(database: AppDatabase): PlayerStateDao {
        return database.playerStateDao()
    }
}