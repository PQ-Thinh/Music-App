package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MusicViewModel(private val repository: MusicRepository) : ViewModel() {

    // --- Media Player Engine State ---
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgressSec = MutableStateFlow(0)
    val playbackProgressSec: StateFlow<Int> = _playbackProgressSec.asStateFlow()

    private var playerJob: Job? = null

    // --- Lists and Collections (Reactive Streams from DB) ---
    val allSongs: StateFlow<List<SongEntity>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val friendActivities: StateFlow<List<FriendActivityEntity>> = repository.friendActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncDevices: StateFlow<List<SyncDeviceEntity>> = repository.syncDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Search & Filters ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _showOfflineOnly = MutableStateFlow(false)
    val showOfflineOnly = _showOfflineOnly.asStateFlow()

    // Combined filtered songs list
    val filteredSongs: StateFlow<List<SongEntity>> = combine(allSongs, searchQuery, showOfflineOnly) { list, query, offlineOnly ->
        list.filter { song ->
            val matchesQuery = query.isBlank() || 
                               song.title.contains(query, ignoreCase = true) || 
                               song.artist.contains(query, ignoreCase = true) ||
                               song.genre.contains(query, ignoreCase = true) ||
                               (query.equals("Favorite", ignoreCase = true) && song.isFavorite)
            val matchesOffline = !offlineOnly || song.isOffline
            matchesQuery && matchesOffline
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Sync & Download States (Simulated UI Async Actions) ---
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabase()
            // Default current song to first item if available
            allSongs.filter { it.isNotEmpty() }.first().let { list ->
                if (_currentSong.value == null && list.isNotEmpty()) {
                    _currentSong.value = list.first()
                }
            }
        }
    }

    // --- Playback Actions ---
    fun selectSong(song: SongEntity, autoPlay: Boolean = true) {
        _currentSong.value = song
        _playbackProgressSec.value = 0
        if (autoPlay) {
            setPlaying(true)
        }
        viewModelScope.launch {
            repository.incrementPlayCount(song.id)
        }
    }

    fun togglePlayPause() {
        if (_currentSong.value != null) {
            setPlaying(!_isPlaying.value)
        }
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
        if (playing) {
            startPlaybackTimer()
        } else {
            stopPlaybackTimer()
        }
    }

    fun skipNext() {
        val songsList = allSongs.value
        val current = _currentSong.value
        if (songsList.isNotEmpty() && current != null) {
            val idx = songsList.indexOfFirst { it.id == current.id }
            val nextIdx = if (idx == -1 || idx == songsList.size -1) 0 else idx + 1
            selectSong(songsList[nextIdx])
        }
    }

    fun skipPrev() {
        val songsList = allSongs.value
        val current = _currentSong.value
        if (songsList.isNotEmpty() && current != null) {
            val idx = songsList.indexOfFirst { it.id == current.id }
            val prevIdx = if (idx <= 0) songsList.size - 1 else idx - 1
            selectSong(songsList[prevIdx])
        }
    }

    fun seekTo(seconds: Int) {
        val maxDuration = _currentSong.value?.durationSecs ?: 100
        _playbackProgressSec.value = seconds.coerceIn(0, maxDuration)
    }

    private fun startPlaybackTimer() {
        playerJob?.cancel()
        playerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _currentSong.value ?: break
                if (_playbackProgressSec.value >= current.durationSecs) {
                    // Loop or move next
                    skipNext()
                } else {
                    _playbackProgressSec.value += 1
                }
            }
        }
    }

    private fun stopPlaybackTimer() {
        playerJob?.cancel()
        playerJob = null
    }

    // --- Search & Filters Actions ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleOfflineFilter() {
        _showOfflineOnly.value = !_showOfflineOnly.value
    }

    // --- User Activity Actions ---
    fun toggleSongFavorite(song: SongEntity) {
        viewModelScope.launch {
            val nextState = !song.isFavorite
            repository.toggleFavorite(song.id, nextState)
            // Update current playing state if it's the same song
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isFavorite = nextState)
            }
        }
    }

    fun startDownload(song: SongEntity) {
        if (song.isOffline) return // already offline
        if (_downloadProgress.value.containsKey(song.id)) return // already downloading

        viewModelScope.launch {
            // Simulate progression
            for (progress in 0..100 step 10) {
                _downloadProgress.update { it + (song.id to progress) }
                delay(200L)
            }
            // persist offline download status
            repository.toggleDownload(song.id, true)
            _downloadProgress.update { it - song.id } // remove from active downloading map

            // If downloading currently playing, update details
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isOffline = true)
            }

            // Add social action
            repository.addFriendActivity(
                username = "You",
                avatarColor = "#FFFFFF",
                action = "downloaded \"${song.title}\" for offline listen",
                songId = song.id
            )
        }
    }

    fun removeDownload(song: SongEntity) {
        viewModelScope.launch {
            repository.toggleDownload(song.id, false)
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isOffline = false)
            }
        }
    }

    fun shareSongWithFriend(song: SongEntity, friendName: String, friendAvatarColor: String, customMessage: String = "") {
        viewModelScope.launch {
            // create dynamic share activity
            val formattedMsg = if (customMessage.isBlank()) {
                "shared \"${song.title}\" with you"
            } else {
                "shared \"${song.title}\": \"$customMessage\""
            }
            repository.addFriendActivity(
                username = friendName,
                avatarColor = friendAvatarColor,
                action = formattedMsg,
                songId = song.id
            )
        }
    }

    fun createCustomPlaylist(name: String, description: String, selectedSongIds: List<String>) {
        viewModelScope.launch {
            repository.createPlaylist(name, description, selectedSongIds)
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            repository.deletePlaylistById(playlistId)
        }
    }

    // --- Cross Platform Sync Simulation ---
    fun synchronizeCrossPlatform() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        viewModelScope.launch {
            // Pulse sync loading
            delay(2500L)
            repository.syncAllDevices("Synced Just now")
            _isSyncing.value = false

            // Append to friend/system activity feed
            repository.addFriendActivity(
                username = "Cloud Sync",
                avatarColor = "#00E5FF",
                action = "completed background synchronization across 3 devices",
                songId = null
            )
        }
    }

    override fun onCleared() {
        stopPlaybackTimer()
        super.onCleared()
    }
}

// Factory provider
class MusicViewModelFactory(private val repository: MusicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
