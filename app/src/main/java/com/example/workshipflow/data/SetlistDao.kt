package com.example.workshipflow.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Query("SELECT * FROM setlists")
    fun getAllSetlists(): Flow<List<Setlist>>

    @Query("SELECT * FROM setlists WHERE date >= :currentTime ORDER BY date ASC LIMIT 1")
    fun getUpcomingSetlist(currentTime: Long): Flow<Setlist?>

    @Query("SELECT * FROM setlists WHERE id = :id")
    fun getSetlistById(id: Long): Flow<Setlist?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlist(setlist: Setlist): Long

    @Update
    suspend fun updateSetlist(setlist: Setlist)

    @Delete
    suspend fun deleteSetlist(setlist: Setlist)
}
