package com.example.workshipflow.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Query("SELECT * FROM setlists ORDER BY date DESC")
    fun getAllSetlists(): Flow<List<Setlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlist(setlist: Setlist): Long

    @Update
    suspend fun updateSetlist(setlist: Setlist)

    @Delete
    suspend fun deleteSetlist(setlist: Setlist)
}
