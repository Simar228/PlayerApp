package com.example.sound.Data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_clearsIncompatibleQueueAndPlayerState() {
        migrationHelper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                """
                INSERT INTO queue_items (
                    id, songUri, position, title, artist,
                    duration, album, genre, artUri
                ) VALUES (
                    1, 'content://songs/1', 0, 'Song', 'Artist',
                    1000, NULL, NULL, NULL
                )
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO player_state (
                    id, currentQueueItemId, positionMs
                ) VALUES (
                    1, 1, 500
                )
                """.trimIndent()
            )
            close()
        }

        val migratedDatabase = migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE,
            2,
            true,
            AppDatabase.MIGRATION_1_2
        )

        migratedDatabase.query("SELECT COUNT(*) FROM queue_items").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }

        migratedDatabase.query("SELECT COUNT(*) FROM player_state").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }

        migratedDatabase.close()
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
