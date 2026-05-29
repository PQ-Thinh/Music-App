package com.example

import android.app.Application
import com.example.data.MusicDatabase
import com.example.data.MusicRepository

class MusicApplication : Application() {
    val database by lazy { MusicDatabase.getDatabase(this) }
    val repository by lazy { MusicRepository(database.musicDao()) }
}
