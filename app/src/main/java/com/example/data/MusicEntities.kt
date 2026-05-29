package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSecs: Int,
    val coverColor: String,
    val streamUrl: String,
    val isOffline: Boolean,
    val isFavorite: Boolean,
    val tempoBpm: Int,
    val genre: String,
    val playCount: Int = 0
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val songIdsCsv: String, // Comma-separated Song IDs like "1,3,5"
    val isPersonalized: Boolean = false,
    val coverGradientStart: String = "#FF512F",
    val coverGradientEnd: String = "#DD2476"
)

@Entity(tableName = "friend_activities")
data class FriendActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val avatarColor: String,
    val action: String,
    val songId: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_devices")
data class SyncDeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val platform: String,
    val isSynced: Boolean,
    val lastSyncTime: String
)
