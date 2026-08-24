package com.example.sound.Presentation.playerUi.viewModel

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerControllerModule {

    @Binds
    abstract fun bindPlayerControllerProvider(
        factory: PlayerControllerFactory
    ): PlayerControllerProvider
}
