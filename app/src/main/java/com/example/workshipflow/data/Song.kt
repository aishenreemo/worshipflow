package com.example.workshipflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String = "",
    val originalKey: String = "C",
    val currentKey: String = "C",
    val content: String, // ChordPro format or similar
    val sourceUrl: String = "",
    val currentKeyShift: Int = 0
)
