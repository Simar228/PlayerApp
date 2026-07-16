package com.example.sound.Data.provider

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.sound.Domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
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
    /**
     * Загружает пользовательское аудио из всех доступных внешних коллекций.
     * Это единственный публичный метод; остальные функции ниже обслуживают его работу.
     */
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

        // Подробные запросы Files/Downloads нужны только при нулевом результате.
        // При нормальной загрузке они не выполняются и не создают лишнюю нагрузку.
        if (songs.isEmpty()) {
            logMp3Diagnostics()
        }

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
                        uri = contentUri,
                        album = cursor.stringOrNull(MediaStore.Audio.Media.ALBUM),
                        // Жанр пока не загружается ради совместимости основной projection.
                        // При необходимости его лучше получать отдельным optional-запросом.
                        genre = genre,
                        art = cursor
                            .longOrNull(MediaStore.Audio.Media.ALBUM_ID)
                            ?.let(::albumArtUri)
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

    private fun Uri.volumeNameOrDefault(): String {
        return pathSegments.firstOrNull() ?: MediaStore.VOLUME_EXTERNAL
    }

    /**
     * Запускает диагностику, если Audio не вернул ни одной песни.
     * Files и Downloads используются только для поиска причины, а не как источник списка.
     */
    private fun logMp3Diagnostics() {
        // На Android 10+ проверяем каждый подключённый внешний volume.
        // На старых версиях используется историческое имя external.
        val volumes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching { MediaStore.getExternalVolumeNames(context) }
                .getOrDefault(emptySet())
                .ifEmpty { setOf(MediaStore.VOLUME_EXTERNAL) }
        } else {
            setOf("external")
        }

        volumes.forEach { volume ->
            // Files даёт наиболее полное представление индекса MediaStore.
            val fileDetails = logFilesDiagnostics(volume)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Отдельная коллекция Downloads доступна только начиная с Android 10.
                logDownloadsDiagnostics(volume, fileDetails)
            } else {
                Log.d(TAG, "MediaStore.Downloads is unavailable before Android 10")
            }
        }
    }

    /**
     * Ищет MP3/audio в MediaStore.Files и выводит служебные поля каждой записи.
     * Результат по _ID нужен для дополнения диагностики Downloads.
     */
    private fun logFilesDiagnostics(volume: String): Map<Long, FileDetails> {
        // Получаем Files URI именно выбранного накопителя.
        val baseCollection = MediaStore.Files.getContentUri(volume)

        // Pending-записи обычно скрыты. В диагностическом URI включаем их, чтобы увидеть
        // незавершённый импорт, IS_PENDING и приложение-владельца записи.
        @Suppress("DEPRECATION")
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.setIncludePending(baseCollection)
        } else {
            baseCollection
        }

        // Эти колонки показывают имя, MIME-классификацию, тип медиа, путь и состояние
        // публикации файла. Поля Android 10+ добавляются только на совместимых версиях.
        val projection = buildList {
            add(MediaStore.MediaColumns._ID)
            add(MediaStore.MediaColumns.DISPLAY_NAME)
            add(MediaStore.MediaColumns.MIME_TYPE)
            add(MediaStore.Files.FileColumns.MEDIA_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.MediaColumns.RELATIVE_PATH)
                add(MediaStore.MediaColumns.IS_PENDING)
                add(MediaStore.MediaColumns.OWNER_PACKAGE_NAME)
            }
        }.toTypedArray()

        // Map позволяет связать строки Files и Downloads по общему MediaStore _ID.
        val detailsById = mutableMapOf<Long, FileDetails>()

        try {
            // Ищем и расширение .mp3, и любой MIME_TYPE audio/*: один из признаков
            // может быть заполнен неправильно, а второй всё равно обнаружит файл.
            context.contentResolver.query(
                collection,
                projection,
                "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? OR " +
                    "${MediaStore.MediaColumns.MIME_TYPE} LIKE ?",
                arrayOf("%.mp3", "audio/%"),
                "${MediaStore.MediaColumns.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                Log.d(TAG, "Diagnostic Files URI=$collection, cursor.count=${cursor.count}")
                while (cursor.moveToNext()) {
                    val id = cursor.longOrNull(MediaStore.MediaColumns._ID) ?: continue

                    // Все поля читаются безопасно: отсутствующая/NULL колонка даст null,
                    // но не остановит диагностический проход.
                    val details = FileDetails(
                        displayName = cursor.stringOrNull(MediaStore.MediaColumns.DISPLAY_NAME),
                        mimeType = cursor.stringOrNull(MediaStore.MediaColumns.MIME_TYPE),
                        mediaType = cursor.longOrNull(MediaStore.Files.FileColumns.MEDIA_TYPE),
                        relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            cursor.stringOrNull(MediaStore.MediaColumns.RELATIVE_PATH)
                        } else {
                            null
                        },
                        isMusic = queryIsMusic(volume, id),
                        isPending = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            cursor.longOrNull(MediaStore.MediaColumns.IS_PENDING)
                        } else {
                            null
                        },
                        ownerPackageName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            cursor.stringOrNull(MediaStore.MediaColumns.OWNER_PACKAGE_NAME)
                        } else {
                            null
                        }
                    )

                    // Сохраняем данные для Downloads и сразу печатаем понятную строку.
                    detailsById[id] = details
                    logDiagnosticRow("Files", details)
                }
            } ?: Log.w(TAG, "Diagnostic Files returned a null cursor for $collection")
        } catch (error: Exception) {
            // Диагностический сбой не должен влиять на работу приложения.
            Log.e(TAG, "Diagnostic Files query failed for URI=$collection", error)
        }

        return detailsById
    }

    /**
     * Проверяет, представлены ли найденные файлы в MediaStore.Downloads.
     * Ноль здесь возможен даже для пути Download/, если запись классифицирована только
     * как Audio или MediaProvider ограничивает доступ к чужим загрузкам.
     */
    private fun logDownloadsDiagnostics(
        volume: String,
        filesDetails: Map<Long, FileDetails>
    ) {
        // Коллекции Downloads нет на Android 9 и ниже.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        // Получаем Downloads URI текущего внешнего накопителя.
        val baseCollection = MediaStore.Downloads.getContentUri(volume)

        // Pending-элементы включаются только для выяснения причины пустого Audio-запроса.
        @Suppress("DEPRECATION")
        val collection = MediaStore.setIncludePending(baseCollection)

        // MEDIA_TYPE и IS_MUSIC не входят в обязательную projection Downloads,
        // поэтому ниже они дополняются данными из Files.
        val projection = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.MIME_TYPE,
            MediaStore.Downloads.RELATIVE_PATH,
            MediaStore.Downloads.IS_PENDING,
            MediaStore.Downloads.OWNER_PACKAGE_NAME
        )

        try {
            // Диагностируем только MP3 и другие записи с MIME_TYPE audio/*.
            context.contentResolver.query(
                collection,
                projection,
                "${MediaStore.Downloads.DISPLAY_NAME} LIKE ? OR " +
                    "${MediaStore.Downloads.MIME_TYPE} LIKE ?",
                arrayOf("%.mp3", "audio/%"),
                "${MediaStore.Downloads.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                Log.d(TAG, "Diagnostic Downloads URI=$collection, cursor.count=${cursor.count}")
                while (cursor.moveToNext()) {
                    val id = cursor.longOrNull(MediaStore.Downloads._ID) ?: continue

                    // По одинаковому _ID забираем поля, которых нет в Downloads Cursor.
                    val fromFiles = filesDetails[id]
                    logDiagnosticRow(
                        source = "Downloads",
                        details = FileDetails(
                            displayName = cursor.stringOrNull(MediaStore.Downloads.DISPLAY_NAME),
                            mimeType = cursor.stringOrNull(MediaStore.Downloads.MIME_TYPE),
                            mediaType = fromFiles?.mediaType,
                            relativePath = cursor.stringOrNull(MediaStore.Downloads.RELATIVE_PATH),
                            isMusic = fromFiles?.isMusic ?: queryIsMusic(volume, id),
                            isPending = cursor.longOrNull(MediaStore.Downloads.IS_PENDING),
                            ownerPackageName = cursor.stringOrNull(
                                MediaStore.Downloads.OWNER_PACKAGE_NAME
                            )
                        )
                    )
                }
            } ?: Log.w(TAG, "Diagnostic Downloads returned a null cursor for $collection")
        } catch (error: Exception) {
            // Это вспомогательная диагностика, поэтому исключение только логируется.
            Log.e(TAG, "Diagnostic Downloads query failed for URI=$collection", error)
        }
    }

    /**
     * Читает IS_MUSIC для одного файла напрямую из Audio.
     * Отдельный запрос нужен, потому что Files/Downloads не обязаны принимать эту
     * колонку в своей projection на всех версиях MediaProvider.
     */
    private fun queryIsMusic(volume: String, id: Long): Long? {
        // На Android 10+ обращаемся к конкретному volume; ниже Android 10 — к legacy URI.
        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(volume)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        // URI одной строки экономит работу по сравнению с повторным обходом таблицы.
        val baseItemUri = ContentUris.withAppendedId(audioCollection, id)

        // Для диагностики разрешаем запросу увидеть незавершённую pending-запись.
        @Suppress("DEPRECATION")
        val itemUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.setIncludePending(baseItemUri)
        } else {
            baseItemUri
        }

        // Ошибка или отсутствие строки превращаются в null: диагностическое поле
        // не должно ломать основную загрузку песен.
        return runCatching {
            context.contentResolver.query(
                itemUri,
                arrayOf(MediaStore.Audio.Media.IS_MUSIC),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.longOrNull(MediaStore.Audio.Media.IS_MUSIC)
                } else {
                    null
                }
            }
        }.onFailure { error ->
            Log.w(TAG, "Cannot read IS_MUSIC for URI=$itemUri", error)
        }.getOrNull()
    }

    /** Печатает одну унифицированную строку диагностики Files или Downloads. */
    private fun logDiagnosticRow(source: String, details: FileDetails) {
        Log.d(
            TAG,
            "Diagnostic $source: DISPLAY_NAME=${details.displayName}, " +
                "MIME_TYPE=${details.mimeType}, MEDIA_TYPE=${details.mediaType}, " +
                "RELATIVE_PATH=${details.relativePath}, IS_MUSIC=${details.isMusic}, " +
                "IS_PENDING=${details.isPending}, " +
                "OWNER_PACKAGE_NAME=${details.ownerPackageName}"
        )
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

    /** Поля одной записи, используемые исключительно для подробного Logcat. */
    private fun String?.cleanMetadataValue(): String? {
        val value = this?.trim().orEmpty()
        return value.takeIf {
            it.isNotEmpty() &&
                it != MediaStore.UNKNOWN_STRING &&
                !it.equals("unknown", ignoreCase = true)
        }
    }

    private data class FileDetails(
        val displayName: String?,
        val mimeType: String?,
        val mediaType: Long?,
        val relativePath: String?,
        val isMusic: Long?,
        val isPending: Long?,
        val ownerPackageName: String?
    )

    private companion object {
        // По одному tag легко отфильтровать весь путь загрузки в Logcat.
        const val TAG = "SongsDebug"
        const val LEGACY_EXTERNAL_VOLUME_NAME = "external"
        val ALBUM_ART_BASE_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}
