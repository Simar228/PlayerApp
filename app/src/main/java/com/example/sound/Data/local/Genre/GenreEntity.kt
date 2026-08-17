package com.example.sound.Data.local.Genre

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.Genre


@Entity(
    tableName = DatabaseTableNames.GENRE,
    indices = [
        Index(
            value = ["name"],
            unique = true
        )
    ]
)
data class GenreEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val isSystem: Boolean = false,
)

fun Genre.toEntity(): GenreEntity {
    return GenreEntity(
        id = id,
        name = name,
        isSystem = false,
    )
}