package com.example.sound.Data.di

import androidx.compose.runtime.internal.DecoyImplementation
import com.example.sound.Data.local.queue.QueueItemEntity
import com.example.sound.Data.repository.PlayerQueueRepositoryImpl
import com.example.sound.Data.repository.SongRepositoryImpl
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(
        implementation: SongRepositoryImpl
    ): SongRepository

    @Binds
    @Singleton
    abstract fun bindPlayerQueueRepository(
        implementation: PlayerQueueRepositoryImpl
    ): PlayerQueueRepository
}