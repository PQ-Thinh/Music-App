package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<SongEntity>> = musicDao.getAllSongs()
    val allPlaylists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
    val friendActivities: Flow<List<FriendActivityEntity>> = musicDao.getFriendActivities()
    val syncDevices: Flow<List<SyncDeviceEntity>> = musicDao.getSyncDevices()

    suspend fun getSongById(id: String): SongEntity? {
        return withContext(Dispatchers.IO) {
            musicDao.getSongById(id)
        }
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            musicDao.updateFavoriteState(id, isFavorite)
        }
    }

    suspend fun toggleDownload(id: String, isOffline: Boolean) {
        withContext(Dispatchers.IO) {
            musicDao.updateDownloadState(id, isOffline)
        }
    }

    suspend fun incrementPlayCount(id: String) {
        withContext(Dispatchers.IO) {
            musicDao.incrementPlayCount(id)
        }
    }

    suspend fun createPlaylist(name: String, description: String, songIds: List<String>) {
        withContext(Dispatchers.IO) {
            val sCsv = songIds.joinToString(",")
            val colors = listOf(
                Pair("#8E2DE2", "#4A00E0"), // Purple
                Pair("#2193b0", "#6dd5ed"), // Blue ocean
                Pair("#ee9ca7", "#ffdde1"), // Rose
                Pair("#11998e", "#38ef7d"), // Turf
                Pair("#ff9966", "#ff5e62")  // Tangerine
            )
            val randomColor = colors.random()
            val pl = PlaylistEntity(
                name = name,
                description = description,
                songIdsCsv = sCsv,
                coverGradientStart = randomColor.first,
                coverGradientEnd = randomColor.second
            )
            musicDao.insertPlaylist(pl)
        }
    }

    suspend fun deletePlaylistById(id: Int) {
        withContext(Dispatchers.IO) {
            musicDao.deletePlaylistById(id)
        }
    }

    suspend fun addFriendActivity(username: String, avatarColor: String, action: String, songId: String?) {
        withContext(Dispatchers.IO) {
            musicDao.insertFriendActivity(
                FriendActivityEntity(
                    username = username,
                    avatarColor = avatarColor,
                    action = action,
                    songId = songId
                )
            )
        }
    }

    suspend fun updateDeviceSyncState(deviceId: String, isSynced: Boolean, lastSyncTime: String) {
        withContext(Dispatchers.IO) {
            val existing = musicDao.getSyncDevices().first().find { it.id == deviceId }
            if (existing != null) {
                musicDao.updateSyncDevice(existing.copy(isSynced = isSynced, lastSyncTime = lastSyncTime))
            }
        }
    }

    suspend fun syncAllDevices(nowTime: String) {
        withContext(Dispatchers.IO) {
            val devices = musicDao.getSyncDevices().first()
            for (device in devices) {
                musicDao.updateSyncDevice(device.copy(isSynced = true, lastSyncTime = nowTime))
            }
        }
    }

    suspend fun seedDatabase() {
        withContext(Dispatchers.IO) {
            val existingSongs = musicDao.getAllSongs().first()
            if (existingSongs.isEmpty()) {
                val seedSongs = listOf(
                    SongEntity(
                        id = "1",
                        title = "Prism Drift",
                        artist = "Neon Ranger",
                        album = "Overdrive",
                        durationSecs = 184,
                        coverColor = "#FF007F", // Neon Pink
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                        isOffline = false,
                        isFavorite = true,
                        tempoBpm = 120,
                        genre = "Synthwave"
                    ),
                    SongEntity(
                        id = "2",
                        title = "Static Echoes",
                        artist = "Calyx",
                        album = "Solitude",
                        durationSecs = 245,
                        coverColor = "#BF5AF2", // Cosmic Violet
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                        isOffline = true,
                        isFavorite = false,
                        tempoBpm = 95,
                        genre = "Ambient"
                    ),
                    SongEntity(
                        id = "3",
                        title = "Deep Focus",
                        artist = "Digital Monk",
                        album = "Neural Net",
                        durationSecs = 310,
                        coverColor = "#0A84FF", // Blue Space
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                        isOffline = false,
                        isFavorite = false,
                        tempoBpm = 128,
                        genre = "Minimalist Techno"
                    ),
                    SongEntity(
                        id = "4",
                        title = "Morning Fog",
                        artist = "Sprout",
                        album = "Greenhouse",
                        durationSecs = 162,
                        coverColor = "#30D158", // Mint Green
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                        isOffline = false,
                        isFavorite = true,
                        tempoBpm = 80,
                        genre = "Lofi Chill"
                    ),
                    SongEntity(
                        id = "5",
                        title = "Midnight Drive",
                        artist = "Highway 84",
                        album = "Interstellar",
                        durationSecs = 215,
                        coverColor = "#FF9F0A", // Amber Gold
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                        isOffline = true,
                        isFavorite = true,
                        tempoBpm = 115,
                        genre = "Outrun Synth"
                    ),
                    SongEntity(
                        id = "6",
                        title = "Quiet Resonance",
                        artist = "Vapor Mist",
                        album = "Atmosphere",
                        durationSecs = 280,
                        coverColor = "#64D2FF", // Teal Sky
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                        isOffline = false,
                        isFavorite = false,
                        tempoBpm = 72,
                        genre = "Cinematic Ambient"
                    ),
                    SongEntity(
                        id = "7",
                        title = "Lunar Cycle",
                        artist = "Selene",
                        album = "Phases",
                        durationSecs = 198,
                        coverColor = "#FF453A", // Hot Coral
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                        isOffline = false,
                        isFavorite = false,
                        tempoBpm = 110,
                        genre = "Downbeat Electro"
                    ),
                    SongEntity(
                        id = "8",
                        title = "Submerged",
                        artist = "Sonar Room",
                        album = "Deep Sea",
                        durationSecs = 224,
                        coverColor = "#32D74B", // Lime Acid
                        streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                        isOffline = false,
                        isFavorite = false,
                        tempoBpm = 124,
                        genre = "Minimal Dub"
                    )
                )
                musicDao.insertSongs(seedSongs)
            }

            val existingPlaylists = musicDao.getAllPlaylists().first()
            if (existingPlaylists.isEmpty()) {
                val seedPlaylists = listOf(
                    PlaylistEntity(
                        id = 1,
                        name = "Focus Flow",
                        description = "Minimalist repetitions to help you dial in.",
                        songIdsCsv = "2,3,6",
                        isPersonalized = true,
                        coverGradientStart = "#1f4037",
                        coverGradientEnd = "#99f2c8"
                    ),
                    PlaylistEntity(
                        id = 2,
                        name = "Late Night Outrun",
                        description = "Sleek retro-futurism for dark highways.",
                        songIdsCsv = "1,5,7",
                        isPersonalized = true,
                        coverGradientStart = "#9c27b0",
                        coverGradientEnd = "#ff3d00"
                    ),
                    PlaylistEntity(
                        id = 3,
                        name = "Organic Ambient",
                        description = "Lush textures and soothing field recordings.",
                        songIdsCsv = "4,6",
                        isPersonalized = true,
                        coverGradientStart = "#4ca1af",
                        coverGradientEnd = "#c4e0e5"
                    )
                )
                for (pl in seedPlaylists) {
                    musicDao.insertPlaylist(pl)
                }
            }

            val existingSocial = musicDao.getFriendActivities().first()
            if (existingSocial.isEmpty()) {
                val seedActivities = listOf(
                    FriendActivityEntity(
                        id = 1,
                        username = "Alice Chen",
                        avatarColor = "#8E2DE2",
                        action = "shared \"Midnight Drive\" with you",
                        songId = "5"
                    ),
                    FriendActivityEntity(
                        id = 2,
                        username = "Marcus Vance",
                        avatarColor = "#11998e",
                        action = "listening to \"Deep Focus\"",
                        songId = "3"
                    ),
                    FriendActivityEntity(
                        id = 3,
                        username = "Emma Watson",
                        avatarColor = "#ff9966",
                        action = "downloaded \"Static Echoes\" for offline listen",
                        songId = "2"
                    )
                )
                for (act in seedActivities) {
                    musicDao.insertFriendActivity(act)
                }
            }

            val existingDevices = musicDao.getSyncDevices().first()
            if (existingDevices.isEmpty()) {
                val seedDevices = listOf(
                    SyncDeviceEntity(
                        id = "mac_os_desktop",
                        name = "MBP-Desktop-M3",
                        platform = "macOS Client",
                        isSynced = true,
                        lastSyncTime = "3 mins ago"
                    ),
                    SyncDeviceEntity(
                        id = "ios_app",
                        name = "Mini-Companion-Phone",
                        platform = "iOS App",
                        isSynced = true,
                        lastSyncTime = "Synced Just now"
                    ),
                    SyncDeviceEntity(
                        id = "web_player",
                        name = "Chrome Web Player",
                        platform = "Web App (Chrome)",
                        isSynced = false,
                        lastSyncTime = "3 hours ago"
                    )
                )
                for (dev in seedDevices) {
                    musicDao.insertSyncDevice(dev)
                }
            }
        }
    }
}
