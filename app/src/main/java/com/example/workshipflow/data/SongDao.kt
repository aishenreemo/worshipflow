package com.example.workshipflow.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id IN (:songIds)")
    fun getSongsByIds(songIds: List<Long>): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongFlow(id: Long): Flow<Song?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song): Long

    @Update
    suspend fun updateSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)
    
    @Query("SELECT * FROM songs ORDER BY id DESC LIMIT :limit")
    fun getRecentSongs(limit: Int): Flow<List<Song>>
}
