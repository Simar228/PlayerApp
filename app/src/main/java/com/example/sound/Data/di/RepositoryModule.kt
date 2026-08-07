package com.example.sound.Data.di

import com.example.sound.Data.repository.DefaultQueueRepositoryImpl
import com.example.sound.Data.repository.PlaybackTransitionRepositoryImpl
import com.example.sound.Data.repository.PlayerQueueRepositoryImpl
import com.example.sound.Data.repository.PlayerStateRepositoryImpl
import com.example.sound.Data.repository.SongRepositoryImpl
import com.example.sound.Domain.repository.DefaultQueueRepository
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Domain.repository.PlayerStateRepository
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
    abstract fun bingDefaultQueueRepository(
        implementation: DefaultQueueRepositoryImpl
    ): DefaultQueueRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackTransitionRepository(
        implementation: PlaybackTransitionRepositoryImpl
    ): PlaybackTransitionRepository

    @Binds
    @Singleton
    abstract fun bingPlayerStateRepository(
        implementation: PlayerStateRepositoryImpl
    ): PlayerStateRepository
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