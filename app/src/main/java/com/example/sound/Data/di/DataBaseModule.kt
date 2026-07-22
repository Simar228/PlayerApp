package com.example.sound.Data.di

import android.content.Context
import androidx.room.Room
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.queue.QueueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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