package com.example.workshipflow.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workshipflow.data.*
import com.example.workshipflow.utils.ChordTransposer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class WorshipViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val songDao = database.songDao()
    private val setlistDao = database.setlistDao()

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val allSetlists: Flow<List<Setlist>> = setlistDao.getAllSetlists().map {
        val currentTime = System.currentTimeMillis()
        val upcoming = it.filter { setlist -> setlist.date >= currentTime }.sortedBy { setlist -> setlist.date }
        val past = it.filter { setlist -> setlist.date < currentTime }.sortedByDescending { setlist -> setlist.date }
        upcoming + past
    }
    val recentSongs: Flow<List<Song>> = songDao.getRecentSongs(3)
    val currentSetlist: Flow<Setlist?> = setlistDao.getUpcomingSetlist(System.currentTimeMillis())

    fun getSetlistById(id: Long): Flow<Setlist?> {
        return setlistDao.getSetlistById(id)
    }

    fun addSong(title: String, content: String, originalKey: String? = null, artist: String = "", sourceUrl: String = "", onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val finalKey = originalKey ?: ChordTransposer.detectKey(content)
            val id = songDao.insertSong(
                Song(
                    title = title.trim(),
                    artist = artist.trim(),
                    originalKey = finalKey,
                    currentKey = finalKey,
                    content = content.trim(),
                    sourceUrl = sourceUrl.trim(),
                    currentKeyShift = 0
                )
            )
            onSaved(id)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            songDao.deleteSong(song)
        }
    }

    fun addSetlist(name: String, date: Long, theme: String = "", leader: String = "", songIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            setlistDao.insertSetlist(
                Setlist(
                    name = name.trim(),
                    date = date,
                    theme = theme.trim(),
                    leader = leader.trim(),
                    songIds = songIds
                )
            )
        }
    }

    fun updateSetlist(setlist: Setlist) {
        viewModelScope.launch {
            setlistDao.updateSetlist(setlist)
        }
    }

    fun deleteSetlist(setlist: Setlist) {
        viewModelScope.launch {
            setlistDao.deleteSetlist(setlist)
        }
    }

    fun addSongToSetlist(setlist: Setlist, songId: Long) {
        viewModelScope.launch {
            val updatedSongs = setlist.songIds.toMutableList().apply {
                add(songId)
            }
            setlistDao.updateSetlist(setlist.copy(songIds = updatedSongs))
        }
    }

    fun removeSongFromSetlist(setlist: Setlist, songId: Long) {
        viewModelScope.launch {
            val updatedSongs = setlist.songIds.toMutableList().apply {
                remove(songId)
            }
            setlistDao.updateSetlist(setlist.copy(songIds = updatedSongs))
        }
    }

    fun reorderSongInSetlist(setlist: Setlist, from: Int, to: Int) {
        viewModelScope.launch {
            val updatedSongs = setlist.songIds.toMutableList().apply {
                val song = removeAt(from)
                add(to, song)
            }
            setlistDao.updateSetlist(setlist.copy(songIds = updatedSongs))
        }
    }

    fun getSongsByIds(songIds: List<Long>): Flow<List<Song>> {
        return songDao.getSongsByIds(songIds).map { songs ->
            val songMap = songs.associateBy { it.id }
            songIds.mapNotNull { id -> songMap[id] }
        }
    }

    suspend fun getSongById(id: Long): Song? {
        return songDao.getSongById(id)
    }

    fun getSongFlow(id: Long): Flow<Song?> {
        return songDao.getSongFlow(id)
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            songDao.updateSong(song.copy(
                title = song.title.trim(),
                artist = song.artist.trim(),
                originalKey = song.originalKey.trim(),
                currentKey = song.currentKey.trim(),
                content = song.content.trim()
            ))
        }
    }

    fun parseAndSave(url: String, html: String, onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val (title, content) = withContext(Dispatchers.IO) {
                    val doc = Jsoup.parse(html, url)

                    var pageTitle = doc.title()
                        .split("|", "-", "–")[0].trim()
                        .replace(" Chords", "", ignoreCase = true)
                        .replace(" Lyrics", "", ignoreCase = true)

                    var extractedText = ""

                    // 1. Special handling for Ultimate Guitar
                    if (url.contains("ultimate-guitar.com")) {
                        val store = doc.select(".js-store").first()
                        val dataContent = store?.attr("data-content")
                        if (dataContent != null) {
                            val contentMatch = Regex("\"content\":\"(.*?)\",\"").find(dataContent)
                            extractedText = contentMatch?.groupValues?.get(1)
                                ?.replace("\\r\\n", "\n")
                                ?.replace("\\n", "\n")
                                ?.replace("\\\"", "\"") ?: ""
                        }
                    }

                    // 2. Standard CSS selectors
                    if (extractedText.isBlank()) {
                        val selectors = arrayOf(
                            "pre", ".js-tab-content", ".chord-pro", ".song-content",
                            "article pre", "div[class*='chord']", "div[class*='tab']"
                        )
                        for (selector in selectors) {
                            val element = doc.select(selector).first()
                            if (element != null && element.wholeText().isNotBlank()) {
                                extractedText = element.wholeText()
                                break
                            }
                        }
                    }

                    Pair(pageTitle, extractedText.trim())
                }

                if (content.isNotBlank()) {
                    val detectedKey = ChordTransposer.detectKey(content)
                    val id = songDao.insertSong(Song(
                        title = title.trim(),
                        content = content.trim(),
                        originalKey = detectedKey,
                        currentKey = detectedKey,
                        sourceUrl = url.trim()
                    ))
                    onSuccess(id)
                } else {
                    onError("Could not find song content on this page.")
                }
            } catch (e: Exception) {
                onError("Extraction failed: ${e.message}")
            }
        }
    }

    fun fetchFromUrl(url: String, onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val doc = withContext(Dispatchers.IO) {
                    Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get()
                }
                parseAndSave(url, doc.outerHtml(), onSuccess, onError)
            } catch (e: Exception) {
                onError("Fetch failed: ${e.message}")
            }
        }
    }
}
