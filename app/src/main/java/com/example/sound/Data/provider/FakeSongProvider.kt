package com.example.sound.Data.provider

import android.content.Context
import android.net.Uri
import com.example.sound.Domain.model.Song
import com.example.sound.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FakeSongProvider @Inject constructor(
    @param: ApplicationContext
     private val context: Context
){
    fun loadSongs(): List<Song> {
    return listOf(
        Song(
            id = "1",
            title = "Пока-Пока",
            artist = "CUPSIZE",
            duration = 90000,
            uri = Uri.EMPTY,
            album = "кажется, в аду прикольно, но меня выгнали б утром",
            genre = "Инди-Рок",
            art = Uri.parse("android.resource://${context.packageName}/${R.drawable.art_zmp}")
        ),
        Song(
            id = "2",
            title = "Я ПЫЛЬ",
            artist = "MORGENSHTERN",
            duration = 120000,
            uri = Uri.EMPTY,
            album = "Легендарная пыль",
            genre = "Треп",
            art = null
        ),
        Song(
            id = "3",
            title = "Это было в России",
            artist = "Монеточка",
            duration = 240000,
            uri = Uri.EMPTY,
            album = null,
            genre = null,
            art = null
        )
    )
    }
}
