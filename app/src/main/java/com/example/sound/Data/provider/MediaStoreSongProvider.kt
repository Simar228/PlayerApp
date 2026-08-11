package com.example.sound.Data.provider

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.sound.Domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Источник песен из системной медиатеки Android (MediaStore).
 *
 * Класс не хранит песни самостоятельно: при каждом вызове [loadSongs] он читает
 * актуальные данные MediaStore и превращает строки Cursor в доменные объекты [Song].
 * Hilt передаёт application Context, чтобы provider не зависел от Activity и не создавал утечек.
 */
@Singleton
class MediaStoreSongProvider @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {
    fun observeSongs(): Flow<List<Song>> {
        return callbackFlow {
            val observer = object : ContentObserver(
                Handler(Looper.getMainLooper())
            ) {
                override fun onChange(selfChange: Boolean) {
                    trySend(Unit)
                }
            }

            externalAudioCollections().forEach { collection ->
                context.contentResolver.registerContentObserver(
                    collection,
                    true,
                    observer
                )
            }

            trySend(Unit)

            awaitClose {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
            .conflate()
            .map {
                loadSongs()
            }
            .flowOn(Dispatchers.IO)
    }

    /** Загружает актуальный снимок пользовательского аудио из внешних коллекций. */
    fun loadSongs(): List<Song> {
        // LinkedHashMap удаляет дубликаты, сохраняя порядок добавления.
        // Одна песня может одновременно прийти из merged URI и URI конкретного volume.
        val songsByUri = linkedMapOf<String, Song>()
        val genresByVolume = mutableMapOf<String, Map<Long, String>>()

        // Опрашиваем все внешние аудиоколлекции и объединяем их содержимое.
        externalAudioCollections().forEach { collection ->
            queryAudioCollection(collection, genresByVolume).forEach { song ->
                // Канонический content URI учитывает настоящий volume и поэтому
                // подходит как уникальный ключ песни.
                songsByUri.putIfAbsent(song.uri.toString(), song)
            }
        }

        // Возвращаем один общий список, отсортированный по названию.
        val songs = songsByUri.values
            .sortedBy { it.title.orEmpty().lowercase() }

        // Этот лог показывает итог после объединения и удаления дубликатов.
        Log.d(TAG, "loadSongs() returning ${songs.size} songs")

        return songs
    }

    /**
     * Формирует список внешних Audio URI для запроса.
     * INTERNAL_CONTENT_URI намеренно не используется: он содержит системные звуки,
     * тогда как приложению нужны пользовательские файлы.
     */
    private fun externalAudioCollections(): List<Uri> {
        // Legacy URI нужен на Android 9 и ниже и полезен для диагностики новых версий.
        val legacyCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        Log.d(TAG, "MediaStore.Audio.Media.EXTERNAL_CONTENT_URI=$legacyCollection")

        // До Android 10 нескольких именованных external volumes ещё не было.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return listOf(legacyCollection)
        }

        // VOLUME_EXTERNAL — синтетическая коллекция, объединяющая все внешние носители.
        val mergedCollection = MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )

        // Получаем реальные volumes: external_primary и, например, UUID SD-карты.
        // Ошибка конкретного MediaProvider не должна уронить приложение.
        val volumeNames = runCatching {
            MediaStore.getExternalVolumeNames(context)
        }.onFailure { error ->
            Log.e(TAG, "Cannot obtain MediaStore external volumes", error)
        }.getOrDefault(emptySet())

        Log.d(TAG, "MediaStore VOLUME_EXTERNAL URI=$mergedCollection")
        Log.d(TAG, "MediaStore external volumes=$volumeNames")

        // Проверяем merged view и каждый накопитель. Повторы самих URI удаляем здесь,
        // а одинаковые песни из разных запросов — в loadSongs().
        return (listOf(mergedCollection) + volumeNames
            .map(MediaStore.Audio.Media::getContentUri))
            .distinct()
    }

    /**
     * Читает одну Audio-коллекцию и преобразует каждую строку Cursor в [Song].
     * IS_MUSIC намеренно не используется как фильтр: у корректного MP3 значение может быть NULL.
     */
    private fun queryAudioCollection(
        collection: Uri,
        genresByVolume: MutableMap<String, Map<Long, String>>
    ): List<Song> {
        val songs = mutableListOf<Song>()

        // Projection содержит только нужные основному списку поля. GENRE здесь нет,
        // поскольку доступность этой колонки различается между версиями MediaProvider.
        val projection = buildList {
            // Системный ID нужен для создания content URI конкретной песни.
            add(MediaStore.Audio.Media._ID)
            // TITLE берётся из тегов, DISPLAY_NAME служит запасным названием.
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            // Метаданные, которые используются интерфейсом плеера.
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            // Колонка читается только для диагностики, но не ограничивает результат.
            add(MediaStore.Audio.Media.IS_MUSIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Реальное имя volume нужно для канонического URI и дедупликации.
                add(MediaStore.MediaColumns.VOLUME_NAME)
            }
        }.toTypedArray()

        // Счётчики показывают, какие значения IS_MUSIC фактически вернуло устройство.
        var nullIsMusicCount = 0
        var zeroIsMusicCount = 0
        var nonZeroIsMusicCount = 0

        try {
            // selection=null означает, что записи не отбрасываются по IS_MUSIC.
            // Сортировку по TITLE выполняет сам MediaStore.
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                Log.d(TAG, "MediaStore query URI=$collection")
                Log.d(TAG, "MediaStore cursor.count = ${cursor.count}")

                while (cursor.moveToNext()) {
                    // Без _ID нельзя построить URI. Такую повреждённую строку пропускаем,
                    // не прерывая загрузку остальных песен.
                    val id = cursor.longOrNull(MediaStore.Audio.Media._ID)
                    if (id == null) {
                        Log.w(TAG, "Skipping an audio row without _ID from $collection")
                        continue
                    }

                    when (cursor.longOrNull(MediaStore.Audio.Media.IS_MUSIC)) {
                        null -> nullIsMusicCount++
                        0L -> zeroIsMusicCount++
                        else -> nonZeroIsMusicCount++
                    }

                    // Если TITLE отсутствует, ниже используем имя файла без расширения.
                    val displayName = cursor.stringOrNull(
                        MediaStore.Audio.Media.DISPLAY_NAME
                    )

                    // Для строки merged-коллекции восстанавливаем URI настоящего volume.
                    // Иначе merged и per-volume варианты одной песни имели бы разные URI.
                    val itemVolumeName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cursor.stringOrNull(MediaStore.MediaColumns.VOLUME_NAME)
                            ?: collection.volumeNameOrDefault()
                    } else {
                        LEGACY_EXTERNAL_VOLUME_NAME
                    }

                    val itemCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Audio.Media.getContentUri(itemVolumeName)
                    } else {
                        collection
                    }

                    // Добавляем _ID к URI коллекции и получаем URI самого аудиофайла.
                    val contentUri = ContentUris.withAppendedId(itemCollection, id)
                    val genre = genresByVolume
                        .getOrPut(itemVolumeName) { queryGenresByAudioId(itemVolumeName) }[id]

                    // Преобразуем текущую строку системного Cursor в модель приложения.
                    songs += Song(
                        id = contentUri.toString(),
                        title = cursor.stringOrNull(MediaStore.Audio.Media.TITLE)
                            ?: displayName?.substringBeforeLast('.')
                            ?: "Без названия",
                        artist = cursor.stringOrNull(MediaStore.Audio.Media.ARTIST)
                            ?: "Неизвестный исполнитель",
                        duration = cursor.longOrNull(MediaStore.Audio.Media.DURATION) ?: 0L,
                        uri = contentUri.toString(),
                        album = cursor.stringOrNull(MediaStore.Audio.Media.ALBUM),
                        // Жанр пока не загружается ради совместимости основной projection.
                        // При необходимости его лучше получать отдельным optional-запросом.
                        genre = genre,
                        art = cursor
                            .longOrNull(MediaStore.Audio.Media.ALBUM_ID)
                            ?.let(::albumArtUri)?.toString()
                    )
                }
            } ?: Log.w(TAG, "MediaStore returned a null cursor for URI=$collection")
        } catch (error: Exception) {
            // Ошибка одного накопителя не должна мешать запросить другие volumes.
            Log.e(TAG, "MediaStore audio query failed for URI=$collection", error)
        }

        // Лог объясняет ситуации, когда старый фильтр IS_MUSIC != 0 давал пустой список.
        Log.d(
            TAG,
            "IS_MUSIC values for $collection: " +
                "null=$nullIsMusicCount, zero=$zeroIsMusicCount, " +
                "nonZero=$nonZeroIsMusicCount"
        )
        return songs
    }

    private fun albumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(ALBUM_ART_BASE_URI, albumId)
    }

    private fun queryGenresByAudioId(volumeName: String): Map<Long, String> {
        val genresByAudioId = mutableMapOf<Long, String>()

        return try {
            context.contentResolver.query(
                MediaStore.Audio.Genres.getContentUri(volumeName),
                arrayOf(
                    MediaStore.Audio.Genres._ID,
                    MediaStore.Audio.Genres.NAME
                ),
                null,
                null,
                null
            )?.use { genresCursor ->
                while (genresCursor.moveToNext()) {
                    val genreId = genresCursor.longOrNull(MediaStore.Audio.Genres._ID)
                        ?: continue
                    val genreName = genresCursor
                        .stringOrNull(MediaStore.Audio.Genres.NAME)
                        .cleanMetadataValue()
                        ?: continue

                    queryGenreMembers(volumeName, genreId, genreName, genresByAudioId)
                }
            }

            genresByAudioId
        } catch (error: Exception) {
            Log.w(TAG, "Cannot read MediaStore genres for volume=$volumeName", error)
            emptyMap()
        }
    }

    private fun queryGenreMembers(
        volumeName: String,
        genreId: Long,
        genreName: String,
        genresByAudioId: MutableMap<Long, String>
    ) {
        runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Genres.Members.getContentUri(volumeName, genreId),
                arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
                null,
                null,
                null
            )?.use { membersCursor ->
                while (membersCursor.moveToNext()) {
                    val audioId = membersCursor
                        .longOrNull(MediaStore.Audio.Genres.Members.AUDIO_ID)
                        ?: continue
                    genresByAudioId.putIfAbsent(audioId, genreName)
                }
            }
        }.onFailure { error ->
            Log.w(
                TAG,
                "Cannot read MediaStore genre members for volume=$volumeName, genreId=$genreId",
                error
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun Uri.volumeNameOrDefault(): String {
        return pathSegments.firstOrNull() ?: MediaStore.VOLUME_EXTERNAL
    }

    /**
     * Безопасно читает String: возвращает null, если колонки нет или в ней SQL NULL.
     */
    private fun Cursor.stringOrNull(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    /**
     * Безопасно читает Long без getColumnIndexOrThrow, поскольку набор колонок может
     * отличаться между версиями Android и реализациями MediaProvider.
     */
    private fun Cursor.longOrNull(columnName: String): Long? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getLong(index) else null
    }

    /** Нормализует пустые и служебные значения метаданных в null. */
    private fun String?.cleanMetadataValue(): String? {
        val value = this?.trim().orEmpty()
        return value.takeIf {
            it.isNotEmpty() &&
                it != MediaStore.UNKNOWN_STRING &&
                !it.equals("unknown", ignoreCase = true)
        }
    }

    private companion object {
        // По одному tag легко отфильтровать весь путь загрузки в Logcat.
        const val TAG = "SongsDebug"
        const val LEGACY_EXTERNAL_VOLUME_NAME = "external"
        val ALBUM_ART_BASE_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}
