package com.example.sound.Data.repository


import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.playerState.PlayerStateDao
import com.example.sound.Data.local.playerState.toPlayerStateEntity
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.Domain.repository.PlayerQueueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaybackTransitionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val queueDao: QueueDao,
    private val playerStateDao: PlayerStateDao,
    private val playerQueueRepository: PlayerQueueRepository,
) : PlaybackTransitionRepository {

    override suspend fun updateCurrentSongIfMatches(songs: List<Song>) {
        database.withTransaction {
            val savedState = playerStateDao.getPlayerState()
                ?: return@withTransaction

            val updatedSong = songs.find { song ->
                song.id == savedState.currentSong.id
            } ?: return@withTransaction

            playerStateDao.savePlayerState(
                updatedSong.toPlayerStateEntity()
            )

            val queue = queueDao.getQueue()
            val currentItem = queue.firstOrNull()
                ?: return@withTransaction

            if (currentItem.song.id != updatedSong.id) {
                return@withTransaction
            }

            val updatedQueue = queue.toMutableList().apply {
                this[0] = currentItem.copy(
                    song = updatedSong
                )
            }

            queueDao.replaceQueue(updatedQueue)
        }
    }

    override suspend fun startPlayback(
        song: Song,
        defaultQueueSongs: List<Song>?,
        queueItemId: Long?
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            defaultQueueSongs?.let { songs ->
                playerQueueRepository.setCurrentSong(
                    currentSong = song,
                    songs = songs
                )
            }

            playerStateDao.savePlayerState(
                song.toPlayerStateEntity()
            )

            if (queueItemId != null) {
                playerQueueRepository.moveQueueItemToCurrent(queueItemId)
            }
        }
    }

    override suspend fun saveTransition(song: Song, queueItemId: Long?) {
        database.withTransaction {
            playerStateDao.savePlayerState(
                song.toPlayerStateEntity()
            )

            if (queueItemId != null) {
                playerQueueRepository.moveQueueItemToCurrent(queueItemId)
            }
        }
    }
}


