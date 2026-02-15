package com.example.workshipflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setlists")
data class Setlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Timestamp
    val name: String,
    val theme: String = "",
    val leader: String = "",
    val songIds: List<Long> = emptyList()
)
