package com.example.sound.Data.repository


import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.Genre.GenreDao
import com.example.sound.Data.local.Genre.GenreEntity
import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.toDefaultQueueEntity
import com.example.sound.Data.local.editSong.EditSongDao
import com.example.sound.Data.local.editSong.toEditSongItemEntity
import com.example.sound.Data.local.playerState.PlayerStateDao
import com.example.sound.Data.local.playerState.toPlayerStateEntity
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import javax.inject.Inject

class PlaybackTransitionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val playerStateDao: PlayerStateDao,
    private val queueDao: QueueDao,
    private val defaultQueueDao: DefaultQueueDao,
    private val editSongDao: EditSongDao,
    private val genreDao: GenreDao,
) : PlaybackTransitionRepository {

    override suspend fun updateCurrentSongIfMatches(songs: List<Song>) {
        database.withTransaction {

            val currentSongId = playerStateDao.getPlayerState()?.currentSongId
            val currentSong = songs.find { it.id == currentSongId }

            currentSong?.let { currentSong ->
                playerStateDao.savePlayerState(currentSong.toPlayerStateEntity())
            }
        }
    }


    override suspend fun saveInformationEditSong(
        genre: String,
        newSong: Song,
        oldSong: Song,
    ) {
        val correctGenre = genre
            .trim()
            .replace(Regex("\\s+"), " ")
            .replaceFirstChar { it.uppercase() }
        database.withTransaction {
            val editSongEntity = newSong.copy(genre = correctGenre).toEditSongItemEntity(oldSong)
            editSongDao.addEditSong(editSongEntity)
            genreDao.insertGenre(GenreEntity(name = correctGenre))
        }
    }


    override suspend fun startPlayback(
        song: Song,
        defaultQueueSongs: List<Song>?,
        queueItemId: Long?
    ) {
        database.withTransaction {
            defaultQueueSongs?.let { songs ->
                val defaultQueueEntities =
                    songs.mapIndexed { index, queueSong ->
                        queueSong.toDefaultQueueEntity(index)
                    }

                defaultQueueDao.replaceDefaultQueue(defaultQueueEntities)
            }


            playerStateDao.savePlayerState(
                song.toPlayerStateEntity()
            )

            if (queueItemId != null) {
                queueDao.deleteQueueItemAndReindex(queueItemId)
            }
        }
    }

    override suspend fun saveTransition(song: Song, queueItemId: Long?) {
        database.withTransaction {
            playerStateDao.savePlayerState(
                song.toPlayerStateEntity()
            )

            if (queueItemId != null) {
                queueDao.deleteQueueItemAndReindex(queueItemId)
            }
        }
    }
}
