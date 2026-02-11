package com.example.workshipflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workshipflow.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WorshipViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val songDao = database.songDao()
    private val setlistDao = database.setlistDao()

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val allSetlists: Flow<List<Setlist>> = setlistDao.getAllSetlists()

    fun addSong(title: String, originalKey: String, content: String, sourceUrl: String = "") {
        viewModelScope.launch {
            songDao.insertSong(
                Song(
                    title = title,
                    originalKey = originalKey,
                    currentKey = originalKey,
                    content = content,
                    sourceUrl = sourceUrl
                )
            )
        }
    }

    fun addSetlist(name: String, date: Long) {
        viewModelScope.launch {
            setlistDao.insertSetlist(Setlist(name = name, date = date))
        }
    }
    
    fun getSongsByIds(songIds: List<Long>): Flow<List<Song>> {
        return songDao.getSongsByIds(songIds)
    }

    suspend fun getSongById(id: Long): Song? {
        return songDao.getSongById(id)
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            songDao.updateSong(song)
        }
    }

    // Simple mock for link extraction
    fun extractFromUrl(url: String) {
        // In a real app, this would fetch and parse the URL
        // For MVP, we'll just add a dummy song
        addSong(
            title = "Mock Song from Link",
            originalKey = "G",
            content = "[G]Amazing [C]Grace how [G]sweet the sound\nThat [G]saved a [D]wretch like me",
            sourceUrl = url
        )
    }
}
