package com.example.sound

import android.app.Application
import com.example.sound.Data.di.DatabaseModule
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SoundApplication : Application() {
    @Inject
    lateinit var databaseInitializer: DatabaseModule.DatabaseInitializer

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            databaseInitializer.initialize()
        }
    }
}