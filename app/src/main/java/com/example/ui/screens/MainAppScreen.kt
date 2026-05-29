package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.composed

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.PlaylistEntity
import com.example.data.SongEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MusicViewModel) {
    var activeTab by remember { mutableStateOf("discover") }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showPlayerExpanded by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // User Profile Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "For Alex",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("app_title")
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick search trigger icon
                            IconButton(
                                onClick = { viewModel.updateSearchQuery("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Bar focus",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Sync button Indicator
                            IconButton(
                                onClick = { showSyncDialog = true },
                                modifier = Modifier.testTag("top_sync_button")
                            ) {
                                Icon(
                                    imageVector = if (isSyncing) Icons.Filled.SyncProblem else Icons.Filled.Sync,
                                    contentDescription = "Sync Manager",
                                    tint = if (isSyncing) SleekActiveGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .pulseEffect(isSyncing)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column {
                // Mini Player pinned directly above the main bottom navigation
                currentSong?.let { song ->
                    MiniPlayerBar(
                        song = song,
                        isPlaying = isPlaying,
                        viewModel = viewModel,
                        onClick = { showPlayerExpanded = true }
                    )
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = activeTab == "discover",
                        onClick = { activeTab = "discover" },
                        modifier = Modifier.testTag("tab_discover"),
                        icon = {
                            Icon(
                                imageVector = if (activeTab == "discover") Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Discover"
                            )
                        },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == "library",
                        onClick = { activeTab = "library" },
                        modifier = Modifier.testTag("tab_library"),
                        icon = {
                            Icon(
                                imageVector = if (activeTab == "library") Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                contentDescription = "My Library"
                            )
                        },
                        label = { Text("Library") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = activeTab == "social",
                        onClick = { activeTab = "social" },
                        modifier = Modifier.testTag("tab_social"),
                        icon = {
                            Icon(
                                imageVector = if (activeTab == "social") Icons.Filled.PeopleAlt else Icons.Outlined.PeopleAlt,
                                contentDescription = "Friends Feed"
                            )
                        },
                        label = { Text("Social") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "discover" -> DiscoverTab(viewModel = viewModel)
                "library" -> LibraryTab(
                    viewModel = viewModel,
                    onCreatePlaylistClick = { showCreatePlaylistDialog = true }
                )
                "social" -> SocialTab(viewModel = viewModel)
            }
        }
    }

    // --- Dynamic Sheets / Dialog Box Modal popups ---
    if (showSyncDialog) {
        SyncManagerDialog(
            viewModel = viewModel,
            onDismiss = { showSyncDialog = false }
        )
    }

    if (showPlayerExpanded) {
        currentSong?.let { song ->
            PlayerExpandedDialog(
                song = song,
                isPlaying = isPlaying,
                viewModel = viewModel,
                onDismiss = { showPlayerExpanded = false }
            )
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            viewModel = viewModel,
            onDismiss = { showCreatePlaylistDialog = false }
        )
    }
}

// --- Discover Screen / Tab Content ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverTab(viewModel: MusicViewModel) {
    val songs by viewModel.filteredSongs.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showOfflineOnly by viewModel.showOfflineOnly.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Quick Navigation Section (Mockup Quick picks layout)
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text(
                    text = "QUICK NAVIGATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 2x2 custom grid responsive layout representing Quick Picks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Liked Picks Card
                    val isLikedActive = searchQuery == "Favorite"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLikedActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (isLikedActive) viewModel.updateSearchQuery("") else viewModel.updateSearchQuery("Favorite")
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SleekPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("❤️", fontSize = 16.sp)
                            }
                            Text(
                                "Liked",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Sleep Picks Card
                    val isSleepActive = searchQuery == "Ambient"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSleepActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (isSleepActive) viewModel.updateSearchQuery("") else viewModel.updateSearchQuery("Ambient")
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SleekLavender),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🌙", fontSize = 16.sp)
                            }
                            Text(
                                "Sleep",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Focus Picks Card
                    val isFocusActive = searchQuery == "Techno"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFocusActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (isFocusActive) viewModel.updateSearchQuery("") else viewModel.updateSearchQuery("Techno")
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⚡", fontSize = 16.sp)
                            }
                            Text(
                                "Focus",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Offline Picks Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (showOfflineOnly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                viewModel.toggleOfflineFilter()
                            }
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💾", fontSize = 16.sp)
                            }
                            Text(
                                "Offline",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Personalized Discovery Hero Card (Made for you mockup component)
        item {
            Column {
                Text(
                    text = "MADE FOR YOU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SleekPrimary, SleekAccent)
                            )
                        )
                        .drawBehind {
                            // Artistic overlapping blurry ambient circles from mockup
                            clipRect {
                                drawCircle(
                                    color = Color(0xFFD0BCFF).copy(alpha = 0.15f),
                                    radius = 120.dp.toPx(),
                                    center = Offset(size.width + 10.dp.toPx(), -20.dp.toPx())
                                )
                                drawCircle(
                                    color = Color(0xFFFFD8E4).copy(alpha = 0.08f),
                                    radius = 100.dp.toPx(),
                                    center = Offset(-15.dp.toPx(), size.height + 15.dp.toPx())
                                )
                            }
                        }
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PERSONALIZED DAILY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Discovery Mix",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Based on your listening history with SZA, Tame Impala, and Kaytranada.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (songs.isNotEmpty()) {
                                        viewModel.selectSong(songs.first(), autoPlay = true)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD0BCFF),
                                    contentColor = SleekAccent
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("PLAY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Download hero playlist shortcut
                            IconButton(
                                onClick = {
                                    songs.take(3).forEach { viewModel.startDownload(it) }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Save Discovery Mix offline",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modern Search & Track list section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "EXPLORE TRACKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Compact filter mode indicator
                    if (searchQuery.isNotEmpty() || showOfflineOnly) {
                        TextButton(
                            onClick = {
                                viewModel.updateSearchQuery("")
                                if (showOfflineOnly) viewModel.toggleOfflineFilter()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Clear Filters", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Modern Search Field with soft elegant surface colors
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar_input"),
                    placeholder = { Text("Search songs, artists, or genres...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            }
        }

        // List of songs
        if (songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showOfflineOnly) "No offline downloaded songs present" else "No matching music found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(songs) { song ->
                SongRowItem(song = song, viewModel = viewModel)
            }
        }

        // Bottom spacer to ensure scrolling leaves player room
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Library Screen / Tab Content ---
@Composable
fun LibraryTab(viewModel: MusicViewModel, onCreatePlaylistClick: () -> Unit) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val downloadedSongs = allSongs.filter { it.isOffline }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Playlists",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Button(
                    onClick = onCreatePlaylistClick,
                    modifier = Modifier.testTag("add_playlist_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No playlists yet. Create one above!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(playlists) { pl ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clickable {
                            val songIds = pl.songIdsCsv.split(",")
                            val matchingSongs = allSongs.filter { it.id in songIds }
                            if (matchingSongs.isNotEmpty()) {
                                viewModel.selectSong(matchingSongs.first())
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small gradient album art cover
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(android.graphics.Color.parseColor(pl.coverGradientStart)),
                                        Color(android.graphics.Color.parseColor(pl.coverGradientEnd))
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FeaturedPlayList, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = pl.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                        Text(text = pl.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "${pl.songIdsCsv.split(",").filter { it.isNotEmpty() }.size} tracks",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deletePlaylist(pl.id) }
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete Playlist", tint = SleekCoralRed)
                    }
                }
            }
        }

        item {
            Text(
                text = "Offline Music (${downloadedSongs.size})",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (downloadedSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No offline downloads yet", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text("Tap the download icon on any song to save offline.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(downloadedSongs) { song ->
                SongRowItem(song = song, viewModel = viewModel)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Friend Community Screen / Tab Content ---
@Composable
fun SocialTab(viewModel: MusicViewModel) {
    val activities by viewModel.friendActivities.collectAsStateWithLifecycle()
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text(
                    text = "Friend Activity",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "See what files and streams your circle is sharing.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        if (activities.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No social activities detected.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(activities) { act ->
                val associatedSong = allSongs.find { it.id == act.songId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Profile Circle with stylish green online activity indicator badge
                    Box(
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(act.avatarColor))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = act.username.take(1).uppercase(Locale.getDefault()),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                        // Green Dot online badge (Styling element from mockup)
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(SleekActiveGreen)
                                .border(1.5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = act.username, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            Text(text = "Now", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Text(text = act.action, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

                        associatedSong?.let { s ->
                            Spacer(modifier = Modifier.height(10.dp))
                            // Playable track snippet
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .clickable { viewModel.selectSong(s) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(android.graphics.Color.parseColor(s.coverColor)))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = s.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(text = s.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play shared track", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Individual Song Component ---
@Composable
fun SongRowItem(song: SongEntity, viewModel: MusicViewModel) {
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val isDownloading = downloadProgress.containsKey(song.id)
    val progressPercent = downloadProgress[song.id] ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("song_row_${song.id}")
            .background(Color.Transparent)
            .clickable { viewModel.selectSong(song) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Geometric colorful album art representation
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(android.graphics.Color.parseColor(song.coverColor))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Tracks metadata details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (song.isOffline) {
                    Icon(
                        imageVector = Icons.Filled.OfflinePin,
                        contentDescription = "Offline Available",
                        tint = SleekActiveGreen,
                        modifier = Modifier
                            .size(13.dp)
                            .padding(end = 4.dp)
                    )
                }
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action Toggles (Download / Favorite)
        if (isDownloading) {
            Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progressPercent / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = SleekActiveGreen,
                    strokeWidth = 2.dp
                )
            }
        } else if (!song.isOffline) {
            IconButton(
                onClick = { viewModel.startDownload(song) },
                modifier = Modifier.testTag("download_track_${song.id}")
            ) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Download Song", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        } else {
            IconButton(
                onClick = { viewModel.removeDownload(song) },
                modifier = Modifier.testTag("remove_download_${song.id}")
            ) {
                Icon(imageVector = Icons.Default.FolderDelete, contentDescription = "Remove From Cache", tint = SleekActiveGreen, modifier = Modifier.size(20.dp))
            }
        }

        IconButton(
            onClick = { viewModel.toggleSongFavorite(song) },
            modifier = Modifier.testTag("favorite_track_${song.id}")
        ) {
            Icon(
                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite Song",
                tint = if (song.isFavorite) SleekCoralRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- Playlist Item Card ---
@Composable
fun PlaylistItemCard(playlist: PlaylistEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
            .testTag("playlist_card_${playlist.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(android.graphics.Color.parseColor(playlist.coverGradientStart)),
                                Color(android.graphics.Color.parseColor(playlist.coverGradientEnd))
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicVideo, contentDescription = null, tint = Color.Black, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = playlist.name,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = playlist.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Sticky Mini Player Bar at bottom ---
@Composable
fun MiniPlayerBar(
    song: SongEntity,
    isPlaying: Boolean,
    viewModel: MusicViewModel,
    onClick: () -> Unit
) {
    val progressSec by viewModel.playbackProgressSec.collectAsStateWithLifecycle()
    val progressFraction = progressSec.toFloat() / song.durationSecs.coerceAtLeast(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() }
            .testTag("mini_player_surface"),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(android.graphics.Color.parseColor(song.coverColor))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.genre}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Visual wave indicator
                if (isPlaying) {
                    MinimalSoundWaveVisualizer()
                    Spacer(modifier = Modifier.width(12.dp))
                }

                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.testTag("mini_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.skipNext() },
                    modifier = Modifier.testTag("mini_skip_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Skip Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Real-time micro progress line at the base of Mini Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    .align(Alignment.BottomStart)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// --- Expanded Player Dialog Custom Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerExpandedDialog(
    song: SongEntity,
    isPlaying: Boolean,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit
) {
    val progressSec by viewModel.playbackProgressSec.collectAsStateWithLifecycle()
    var selectedFriendToShare by remember { mutableStateOf<String?>(null) }
    var userCustomShareMsg by remember { mutableStateOf("") }

    val formatTime: (Int) -> String = { totalSecs ->
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        String.format("%01d:%02d", mins, secs)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .testTag("expanded_player_modal"),
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("player_collapse_button")) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse Player", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(32.dp))
                        }

                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = { viewModel.toggleSongFavorite(song) }
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (song.isFavorite) SleekCoralRed else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Artistic Colored Cover Vinyl Representation
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(android.graphics.Color.parseColor(song.coverColor)))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "spin")
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(8000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "angle"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayerSpin(if (isPlaying) angle else 0f)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.fillMaxSize(0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title + Artist Description
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ALBUM: ${song.album} • ${song.genre} • ${song.tempoBpm} BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Media progress interactive slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = progressSec.toFloat(),
                            onValueChange = { viewModel.seekTo(it.toInt()) },
                            valueRange = 0f..song.durationSecs.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag("full_player_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formatTime(progressSec), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text(text = formatTime(song.durationSecs), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Media Buttons Player toolbar Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconButton(onClick = { viewModel.skipPrev() }) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous Song", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(36.dp))
                        }

                        FloatingActionButton(
                            onClick = { viewModel.togglePlayPause() },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("modal_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause Icon",
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.skipNext() }, modifier = Modifier.testTag("expanded_skip_next")) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next Song", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(36.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Interactive quick share tool with friends
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Share with Friends", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            val friends = listOf(
                                Pair("Alice Chen", "#8E2DE2"),
                                Pair("Marcus Vance", "#11998e"),
                                Pair("Emma Watson", "#ff9966")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                friends.forEach { fr ->
                                    val isSelected = selectedFriendToShare == fr.first
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                                            .clickable {
                                                selectedFriendToShare = if (isSelected) null else fr.first
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = fr.first,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (selectedFriendToShare != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = userCustomShareMsg,
                                    onValueChange = { userCustomShareMsg = it },
                                    placeholder = { Text("E.g: Listen to this heavy vibe!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.background,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val fColor = friends.find { it.first == selectedFriendToShare }?.second ?: "#FFA500"
                                        viewModel.shareSongWithFriend(song, selectedFriendToShare!!, fColor, userCustomShareMsg)
                                        selectedFriendToShare = null
                                        userCustomShareMsg = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Send Shared File", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// Custom Rotate Graphic extension to avoid JVM errors is straightforward
fun Modifier.graphicsLayerSpin(angle: Float): Modifier = this.drawWithContent {
    drawContext.transform.apply {
        rotate(angle, center)
    }
    drawContent()
}

// --- Dynamic pulsing animation modifier helper ---
fun Modifier.pulseEffect(enabled: Boolean): Modifier = if (enabled) {
    this.composed {
        val transition = rememberInfiniteTransition(label = "pulse")
        val scale by transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }
} else this

// Sound Wave visualizer in Compose
@Composable
fun MinimalSoundWaveVisualizer() {
    val transition = rememberInfiniteTransition(label = "wave")
    val h1 by transition.animateFloat(
        initialValue = 4f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 18f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(animation = tween(450, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(animation = tween(750, easing = FastOutLinearInEasing), repeatMode = RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(26.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).height(h1.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(3.dp).height(h2.dp).background(SleekActiveGreen, RoundedCornerShape(1.dp)))
        Box(modifier = Modifier.width(3.dp).height(h3.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp)))
    }
}

// --- Sync Management Dialog Modal Popup ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncManagerDialog(viewModel: MusicViewModel, onDismiss: () -> Unit) {
    val devices by viewModel.syncDevices.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = { if (!isSyncing) onDismiss() },
        modifier = Modifier.testTag("sync_manager_dialog"),
        confirmButton = {
            Button(
                onClick = { viewModel.synchronizeCrossPlatform() },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("sync_now_button")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Sync Devices Now", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSyncing) {
                Text("Close", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Device Synchronization", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Seamlessly synchronize all customized playlists, download state, and streaming configurations between synchronized platforms.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                Text("Registered Devices", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)

                devices.forEach { dev ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = dev.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = dev.platform, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (dev.isSynced && !isSyncing) Icons.Filled.CheckCircle else Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = if (dev.isSynced && !isSyncing) SleekActiveGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else dev.lastSyncTime,
                                color = if (dev.isSynced && !isSyncing) SleekActiveGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    )
}

// --- Create Playlist Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(viewModel: MusicViewModel, onDismiss: () -> Unit) {
    var pName by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val selectedSongIds = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("create_playlist_modal_dialog"),
        confirmButton = {
            Button(
                onClick = {
                    if (pName.isNotBlank() && selectedSongIds.isNotEmpty()) {
                        viewModel.createCustomPlaylist(pName, pDesc, selectedSongIds)
                        onDismiss()
                    }
                },
                enabled = pName.isNotBlank() && selectedSongIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Create Playlist", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Create Custom Playlist", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = pName,
                    onValueChange = { pName = it },
                    label = { Text("Playlist Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                TextField(
                    value = pDesc,
                    onValueChange = { pDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Text(
                    text = "Select Tracks (${selectedSongIds.size} chosen)",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 13.sp
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allSongs) { song ->
                        val isSelected = selectedSongIds.contains(song.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                .clickable {
                                    if (isSelected) {
                                        selectedSongIds.remove(song.id)
                                    } else {
                                        selectedSongIds.add(song.id)
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (isSelected) {
                                        selectedSongIds.remove(song.id)
                                    } else {
                                        selectedSongIds.add(song.id)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = song.title, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    )
}
