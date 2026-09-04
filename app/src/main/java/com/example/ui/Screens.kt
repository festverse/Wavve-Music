package com.example.ui

import androidx.compose.foundation.basicMarquee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Close
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote

import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush

@Composable
fun HomeScreen(
    playerViewModel: PlayerViewModel, 
    onNavigateToSettings: () -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {}
) {
    val library by playerViewModel.musicLibrary.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()
    val isLoggedIn by playerViewModel.isLoggedIn.collectAsState()
    val currentUser by playerViewModel.currentUser.collectAsState()
    
    val partyTracks by playerViewModel.homePartyTracks.collectAsState()
    val romanticTracks by playerViewModel.homeRomanticTracks.collectAsState()
    val phonkTracks by playerViewModel.homePhonkTracks.collectAsState()
    val followedArtists by playerViewModel.followedArtists.collectAsState(initial = emptyList())
    
    val dailyPickTrack = remember(library) {
        if (library.isNotEmpty()) {
            val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            library[dayOfYear % library.size]
        } else null
    }
    
    val recentlyPlayed by playerViewModel.recentlyPlayed.collectAsState()
    
    LazyColumn(

        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 24.dp, bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WAVVE MUSIC",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.2.em,
                            fontWeight = FontWeight.Bold
                        ),
                        color = SecondaryText
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Discover",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.03).em
                        ),
                        color = PrimaryText
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isLoggedIn) SurfaceWavve else DividerColor)
                        .border(1.dp, DividerColor, CircleShape)
                        .clickable { onNavigateToSettings() }
                ) {
                    if (isLoggedIn) {
                        val avatarUrl = currentUser?.photoUrl?.toString() ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${currentUser?.uid ?: "default"}"
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUrl)
                                .crossfade(true).build(),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", tint = SecondaryText, modifier = Modifier.fillMaxSize().padding(8.dp))
                    }
                }
            }
        }
        item {
            EditorialCard(modifier = Modifier.padding(horizontal = 24.dp), playerViewModel = playerViewModel, track = dailyPickTrack)
        }
        item { Spacer(Modifier.height(24.dp)) }
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentColor)
                }
            }
        } else {
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    HorizontalTrackList(
                        title = "Recently Played",
                        tracks = recentlyPlayed,
                        playerViewModel = playerViewModel
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            item {
                HorizontalTrackList(
                    title = "You Might Like",
                    tracks = library,
                    playerViewModel = playerViewModel
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
            
            if (partyTracks.isNotEmpty()) {
                item {
                    HorizontalTrackList(
                        title = "Party Starters",
                        tracks = partyTracks,
                        playerViewModel = playerViewModel
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            if (romanticTracks.isNotEmpty()) {
                item {
                    HorizontalTrackList(
                        title = "Romantic & Chill",
                        tracks = romanticTracks,
                        playerViewModel = playerViewModel
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
            
            if (phonkTracks.isNotEmpty()) {
                item {
                    HorizontalTrackList(
                        title = "Phonk Energy",
                        tracks = phonkTracks,
                        playerViewModel = playerViewModel
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            if (followedArtists.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Artists You Follow",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PrimaryText
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(followedArtists.size) { idx ->
                                val artist = followedArtists[idx]
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { onNavigateToArtist(artist.name) }
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(artist.imageUrl.ifEmpty { "https://api.dicebear.com/7.x/avataaars/png?seed=${artist.name}" })
                                            .crossfade(true).build(),
                                        contentDescription = artist.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(100.dp).clip(CircleShape).background(DividerColor)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        artist.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PrimaryText,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun HorizontalTrackList(title: String, tracks: List<androidx.media3.common.MediaItem>, playerViewModel: PlayerViewModel) {
    if (tracks.isEmpty()) return
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryText
            )
        }
        Spacer(Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(minOf(10, tracks.size)) { idx ->
                val track = tracks[idx]
                TrackCard(track = track, playerViewModel = playerViewModel)
            }
        }
    }
}

@Composable
fun TrackCard(track: androidx.media3.common.MediaItem, playerViewModel: PlayerViewModel) {
    val title = track.mediaMetadata.title?.toString() ?: "Unknown"
    val artist = track.mediaMetadata.artist?.toString() ?: "Unknown Artist"
    val artworkUri = track.mediaMetadata.artworkUri
    
    var showMenu by remember { mutableStateOf(false) }
    val playlists by playerViewModel.userPlaylists.collectAsState(initial = emptyList())
    val queueItems by playerViewModel.queueItems.collectAsState(initial = emptyList())
    val isInQueue = queueItems.any { it.mediaId == track.mediaId }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { playerViewModel.playTrack(track) }
    ) {
        Box(modifier = Modifier.size(140.dp)) {
            val isLocal = track.mediaId.startsWith("local_")
            if (isLocal) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DividerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(artworkUri ?: "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=200&auto=format&fit=crop")
                        .crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DividerColor)
                )
            }
            
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(4.dp)
                        .size(16.dp)
                )
            }

            androidx.compose.material3.DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(SurfaceWavve)
            ) {
                if (isInQueue) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Remove from queue", color = PrimaryText) },
                        onClick = {
                            playerViewModel.removeFromQueue(track)
                            showMenu = false
                        }
                    )
                } else {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Add to queue", color = PrimaryText) },
                        onClick = {
                            playerViewModel.addToQueue(track)
                            showMenu = false
                        }
                    )
                }
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("Download", color = PrimaryText) },
                    onClick = {
                        playerViewModel.downloadTrack(track, context)
                        showMenu = false
                    }
                )
                
                if (playlists.isEmpty()) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("No playlists available", color = SecondaryText) },
                        onClick = { showMenu = false }
                    )
                } else {
                    playlists.forEach { pl ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Add to ${pl.name}", color = PrimaryText) },
                            onClick = {
                                scope.launch {
                                    playerViewModel.firestoreManager.addTrackToPlaylist(pl.id, track)
                                    showMenu = false
                                }
                            }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold, color = PrimaryText, maxLines = 1)
        Text(artist, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = SecondaryText, maxLines = 1)
    }
}

@Composable
fun EditorialCard(modifier: Modifier = Modifier, playerViewModel: PlayerViewModel? = null, track: androidx.media3.common.MediaItem? = null) {
    if (track == null) return
    val title = track.mediaMetadata.title?.toString() ?: "Unknown"
    val artist = track.mediaMetadata.artist?.toString() ?: "Unknown"
    
    // Boost image quality specifically for the Daily Pick display since it's a large card
    var artworkUrl = track.mediaMetadata.artworkUri?.toString() ?: "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=1000&auto=format&fit=crop"
    if (artworkUrl.contains("1.200.jpg")) { // Jamendo low res
        artworkUrl = artworkUrl.replace("1.200.jpg", "1.500.jpg")
    } else if (artworkUrl.contains("100x100bb.jpg")) { // iTunes fallback (though repo might already do this)
        artworkUrl = artworkUrl.replace("100x100bb.jpg", "1000x1000bb.jpg")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0x140A0A0A), spotColor = Color(0x140A0A0A))
            .background(SurfaceWavve, RoundedCornerShape(24.dp))
            .clickable {
                playerViewModel?.let {
                    it.playTrack(track)
                }
            }
    ) {
        val isLocal = track.mediaId.startsWith("local_")
        if (isLocal) {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(DividerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(100.dp))
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUrl)
                    .crossfade(true).build(),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                    startY = 100f
                )
            )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(AccentColor, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "DAILY PICK",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.em
                    ),
                    color = Color.White
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "By $artist",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RecentlyPlayedRow(
    modifier: Modifier = Modifier, 
    track: androidx.media3.common.MediaItem? = null, 
    playerViewModel: PlayerViewModel? = null,
    isSearchResult: Boolean = false,
    isDownloaded: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    if (track == null) return
    
    val title = track.mediaMetadata.title?.toString() ?: "Unknown Track"
    val artist = track.mediaMetadata.artist?.toString() ?: "Unknown Artist"
    val artworkUri = track.mediaMetadata.artworkUri
    
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = DividerColor)
            .background(SurfaceWavve, RoundedCornerShape(16.dp))
            .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .clickable {
                if (onClick != null) {
                    onClick()
                } else {
                    playerViewModel?.playTrack(track, isSearchResult)
                }
            }
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(DividerColor), contentAlignment = Alignment.Center) {
            val isLocal = track.mediaId.startsWith("local_")
            if (isLocal) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(24.dp))
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artworkUri ?: "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=200&auto=format&fit=crop")
                        .crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold, color = PrimaryText, maxLines = 1)
            Text(artist, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = SecondaryText, maxLines = 1)
        }
        Spacer(Modifier.width(16.dp))
        
        var showMenu by remember { mutableStateOf(false) }
        val playlists by playerViewModel?.userPlaylists?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
        val queueItems by playerViewModel?.queueItems?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
        val isInQueue = queueItems.any { it.mediaId == track.mediaId }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = PrimaryText)
            }
            androidx.compose.material3.DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(SurfaceWavve)
            ) {
                if (isInQueue) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Remove from queue", color = PrimaryText) },
                        onClick = {
                            playerViewModel?.removeFromQueue(track)
                            showMenu = false
                        }
                    )
                } else {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Add to queue", color = PrimaryText) },
                        onClick = {
                            playerViewModel?.addToQueue(track)
                            showMenu = false
                        }
                    )
                }
                
                if (isDownloaded) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Remove Download", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            playerViewModel?.removeDownloadedTrack(track.mediaId, context)
                            showMenu = false
                        }
                    )
                } else {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Download", color = PrimaryText) },
                        onClick = {
                            playerViewModel?.downloadTrack(track, context)
                            showMenu = false
                        }
                    )
                }
                
                val isLoggedIn = playerViewModel?.isLoggedIn?.collectAsState(initial = false)?.value == true
                if (!isLoggedIn) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Login to save to playlist", color = SecondaryText) },
                        onClick = { 
                            android.widget.Toast.makeText(context, "You need to be logged in to use this feature", android.widget.Toast.LENGTH_SHORT).show()
                            showMenu = false 
                        }
                    )
                } else if (playlists.isEmpty()) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("No playlists available", color = SecondaryText) },
                        onClick = { showMenu = false }
                    )
                } else {
                    playlists.forEach { pl ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Add to ${pl.name}", color = PrimaryText) },
                            onClick = {
                                scope.launch {
                                    playerViewModel?.firestoreManager?.addTrackToPlaylist(pl.id, track)
                                    showMenu = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(
    playerViewModel: PlayerViewModel,
    onNavigateToPlaylist: (String) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {}
) {
    val library by playerViewModel.musicLibrary.collectAsState()
    val isLoading by playerViewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Playlists", "Artists", "Downloads", "Local")
    
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            playerViewModel.loadLocalMedia(context)
        }
    }
    
    val permissionToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_AUDIO
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
    ) {
        Text(
            "Library",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = PrimaryText
        )
        
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundWavve,
            contentColor = AccentColor,
            edgePadding = 16.dp,
            divider = {},
            indicator = {}
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            tabTitle, 
                            style = MaterialTheme.typography.bodyLarge, 
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) PrimaryText else SecondaryText
                        ) 
                    }
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        val playlists by playerViewModel.userPlaylists.collectAsState(initial = emptyList())
        val artists by playerViewModel.followedArtists.collectAsState(initial = emptyList())
        val downloads by playerViewModel.localSavedTracks.collectAsState(initial = emptyList())
        val localTracks by playerViewModel.localTracks.collectAsState()
        val isLoggedIn by playerViewModel.isLoggedIn.collectAsState()
        
        var showCreateDialog by remember { mutableStateOf(false) }
        var newPlaylistName by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create Playlist", color = PrimaryText) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name", color = SecondaryText) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            scope.launch {
                                playerViewModel.firestoreManager.createPlaylist(newPlaylistName)
                                showCreateDialog = false
                                newPlaylistName = ""
                            }
                        }
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = AccentColor)
                    }
                },
                containerColor = SurfaceWavve
            )
        }

        if (!isLoggedIn && selectedTab in 0..2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Log in to save and sync your library.", color = SecondaryText)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Button(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                            ) {
                                Text("Create Playlist", color = Color.White)
                            }
                        }
                        if (playlists.isEmpty()) {
                            item { Text("No playlists yet.", color = SecondaryText) }
                        } else {
                            items(playlists.size) { idx ->
                                val pl = playlists[idx]
                                val collageUrls = pl.tracks.take(4).mapNotNull { it.artworkUri }
                                GenericLibraryRow(
                                    title = pl.name, 
                                    subtitle = "${pl.tracks.size} tracks", 
                                    imageUrls = collageUrls,
                                    onClick = { onNavigateToPlaylist(pl.id) }
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                    1 -> {
                        item {
                            Text("Your Artists", style = MaterialTheme.typography.titleMedium, color = PrimaryText, modifier = Modifier.padding(bottom = 12.dp))
                        }
                        if (artists.isEmpty()) {
                            item { Text("You aren't following anyone yet.", color = SecondaryText, modifier = Modifier.padding(bottom = 24.dp)) }
                        } else {
                            items(artists.size) { idx ->
                                val ar = artists[idx]
                                GenericLibraryRow(title = ar.name, subtitle = "Following", imageUrl = ar.imageUrl, onClick = { onNavigateToArtist(ar.name) })
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                        
                        item {
                            Spacer(Modifier.height(24.dp))
                            Text("Discover Artists", style = MaterialTheme.typography.titleMedium, color = PrimaryText, modifier = Modifier.padding(bottom = 12.dp))
                        }
                        val discoverArtists = listOf(
                            "The Weeknd" to "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/30/05/1e/30051e57-a63a-3acc-4b30-42568293f5f7/15UMGIM36514.rgb.jpg/600x600bb.jpg",
                            "Daft Punk" to "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/e8/43/5f/e8435ffa-b6b9-b171-40ab-4ff3959ab661/886443919266.jpg/600x600bb.jpg",
                            "Billie Eilish" to "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/92/9f/69/929f69f1-9977-3a44-d674-11f70c852d1b/24UMGIM36186.rgb.jpg/600x600bb.jpg",
                            "Arctic Monkeys" to "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/69/9c/b5/699cb5d6-115c-ff73-9d26-e57ea4350d72/887828031795.png/600x600bb.jpg",
                            "Odesza" to "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/76/5d/55/765d554e-e421-0299-783f-d78ad559d5e5/5021392959191.png/600x600bb.jpg",
                            "Ariana Grande" to "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/0a/19/23/0a19235d-8bcc-76d4-58ca-61a65d928587/13UAAIM68293.rgb.jpg/600x600bb.jpg",
                            "G-Eazy" to "https://is1-ssl.mzstatic.com/image/thumb/Music113/v4/8b/0d/3f/8b0d3fd9-7d18-99a6-5123-715f19fe57cb/dj.cziqaejb.jpg/600x600bb.jpg",
                            "Marshmello" to "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/31/14/86/3114862a-40ed-dee0-86b6-5c2612ac0c5f/00602507458782_Cover.jpg/600x600bb.jpg",
                            "Drake" to "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/95/f5/87/95f587f7-21c3-d5f9-d81a-4350f9caa020/16UMGIM27643.rgb.jpg/600x600bb.jpg",
                            "Taylor Swift" to "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/be/e1/48/bee148d6-d16c-d8f7-0173-d6cf6d684aa1/08PNDIM00678.rgb.jpg/600x600bb.jpg",
                            "Eminem" to "https://is1-ssl.mzstatic.com/image/thumb/Music128/v4/78/07/35/78073533-a113-170d-bfab-acc3cec405d1/00602567238218.rgb.jpg/600x600bb.jpg",
                            "Post Malone" to "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/31/57/de/3157dec9-5e26-40d1-d61c-bce30558752d/16UMGIM76041.rgb.jpg/600x600bb.jpg",
                            "Dua Lipa" to "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/0e/c1/57/0ec1575f-5153-ac4b-d578-c5fa3a90bfe1/5021732511676.jpg/600x600bb.jpg",
                            "Ed Sheeran" to "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/15/e6/e8/15e6e8a4-4190-6a8b-86c3-ab4a51b88288/190295851286.jpg/600x600bb.jpg",
                            "Bad Bunny" to "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/b6/74/4d/b6744dbd-77ed-413a-3777-5ac6a2e780eb/197188732554.jpg/600x600bb.jpg"
                        )
                        items(discoverArtists.size) { idx ->
                            val ar = discoverArtists[idx]
                            val isFollowing = artists.any { it.name == ar.first }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = DividerColor)
                                    .background(SurfaceWavve, RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToArtist(ar.first) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(DividerColor)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(ar.second).crossfade(true).build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ar.first, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                                }
                                Button(
                                    onClick = { 
                                        scope.launch {
                                            if (isFollowing) {
                                                playerViewModel.firestoreManager.unfollowArtist(ar.first)
                                            } else {
                                                playerViewModel.firestoreManager.followArtist(ar.first, ar.second)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) DividerColor else AccentColor)
                                ) {
                                    Text(if (isFollowing) "Following" else "Follow", color = if (isFollowing) PrimaryText else Color.White)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                2 -> {
                    if (downloads.isEmpty()) {
                        item { Text("No downloaded tracks.", color = SecondaryText) }
                    } else {
                        items(downloads.size) { idx ->
                            RecentlyPlayedRow(
                                track = downloads[idx],
                                playerViewModel = playerViewModel,
                                isDownloaded = true
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
                3 -> {
                    item {
                        LaunchedEffect(Unit) {
                            val status = ContextCompat.checkSelfPermission(context, permissionToRequest)
                            if (status == PackageManager.PERMISSION_GRANTED) {
                                playerViewModel.loadLocalMedia(context)
                            } else {
                                permissionLauncher.launch(permissionToRequest)
                            }
                        }
                    }
                    if (localTracks.isEmpty()) {
                        item { Text("No local music found or permission denied.", color = SecondaryText) }
                    } else {
                        items(localTracks.size) { idx ->
                            RecentlyPlayedRow(
                                track = localTracks[idx],
                                playerViewModel = playerViewModel
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun GenericLibraryRow(title: String, subtitle: String, imageUrl: String? = null, imageUrls: List<String> = emptyList(), onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = DividerColor)
            .background(SurfaceWavve, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(DividerColor)) {
            if (imageUrls.size >= 4) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.layout.Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(imageUrls[0]).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(imageUrls[1]).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    androidx.compose.foundation.layout.Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(imageUrls[2]).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(imageUrls[3]).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            } else {
                val singleImage = imageUrls.firstOrNull() ?: imageUrl ?: "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=200&auto=format&fit=crop"
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(singleImage)
                        .crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold, color = PrimaryText, maxLines = 1)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp), color = SecondaryText, maxLines = 1)
        }
    }
}

@androidx.compose.foundation.layout.ExperimentalLayoutApi
@Composable
fun SearchScreen(playerViewModel: PlayerViewModel) {
    val searchQuery by playerViewModel.searchQuery.collectAsState()
    val searchResults by playerViewModel.searchResults.collectAsState()
    val isSearching by playerViewModel.isSearching.collectAsState()
    val searchHistory by playerViewModel.searchHistory.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.getOrNull(0)
            if (!spokenText.isNullOrEmpty()) {
                playerViewModel.updateSearchQuery(spokenText)
                playerViewModel.performSearch()
                focusManager.clearFocus()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
    ) {
        Text(
            "Search",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = PrimaryText
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { playerViewModel.updateSearchQuery(it.replace("\n", "")) },
            placeholder = { Text("Search songs or artists", color = SecondaryText) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Search Icon", tint = SecondaryText)
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            playerViewModel.updateSearchQuery("")
                            // Since clearing the query also clears results and shows categories,
                            // we should also clear focus so the keyboard hides.
                            focusManager.clearFocus() 
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = SecondaryText)
                        }
                    }
                    IconButton(onClick = { 
                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Search for music...")
                        }
                        try {
                            speechRecognizerLauncher.launch(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Voice search not available", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice Search", tint = SecondaryText)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWavve,
                unfocusedContainerColor = SurfaceWavve,
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = DividerColor
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { 
                    playerViewModel.performSearch()
                    focusManager.clearFocus() 
                }
            )
        )
        
        Spacer(Modifier.height(24.dp))
        
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)
        ) {
            if (isSearching) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentColor)
                    }
                }
            } else if (searchResults.isNotEmpty()) {
                item {
                    Text(
                        "Search Results",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = PrimaryText
                    )
                }
                items(searchResults.size) { idx ->
                    RecentlyPlayedRow(
                        track = searchResults[idx],
                        playerViewModel = playerViewModel,
                        isSearchResult = true
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else if (searchQuery.isNotBlank()) {
                item {
                    Text("No results found for \"$searchQuery\"", color = SecondaryText)
                }
} else {
                if (searchHistory.isNotEmpty()) {
                    item {
                        Text(
                            "Recent Searches",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = PrimaryText
                        )
                    }
                    item {
                        androidx.compose.foundation.layout.ExperimentalLayoutApi::class
                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            searchHistory.take(9).forEach { query ->
                                androidx.compose.material3.Surface(
                                    color = SurfaceWavve,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                                    modifier = Modifier.clickable {
                                        playerViewModel.updateSearchQuery(query)
                                        playerViewModel.performSearch()
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Text(
                                        text = query,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = SecondaryText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            androidx.compose.material3.Surface(
                                color = SurfaceWavve,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                                modifier = Modifier.clickable {
                                    playerViewModel.clearSearchHistory()
                                }
                            ) {
                                Text(
                                    text = "Clear All",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        "Browse Genres",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = PrimaryText
                    )
                }
                
                // Real genres supported by Audius API
                val genres = listOf(
                    "Electronic" to "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=400",
                    "Hip-Hop/Rap" to "https://images.unsplash.com/photo-1508973379184-7517410fb0bc?q=80&w=400",
                    "Pop" to "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=400",
                    "Lo-Fi" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=400",
                    "R&B/Soul" to "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=400",
                    "Rock" to "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=400",
                    "Alternative" to "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?q=80&w=400",
                    "Jazz" to "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=400"
                )
                
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(500.dp), // Increased height slightly
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(genres.size) { idx ->
                            val genre = genres[idx]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.5f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { playerViewModel.searchByGenre(genre.first) }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(genre.second)
                                        .crossfade(true).build(),
                                    contentDescription = genre.first,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Dark gradient overlay for text readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                            startY = 50f
                                        ))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Text(genre.first, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun NowPlayingScreen(
    title: String,
    artist: String,
    artworkUri: String?,
    isPlaying: Boolean,
    isLocal: Boolean = false,
    currentPosition: Long,
    duration: Long,
    lyrics: List<com.example.data.LyricLine>,
    repeatMode: Int,
    shuffleModeEnabled: Boolean,
    playerViewModel: PlayerViewModel,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSaveToPlaylist: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    onGoToArtist: () -> Unit,
    onViewCredits: () -> Unit,
    onDismissQueue: () -> Unit
) {
    var showLyrics by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    
    androidx.activity.compose.BackHandler(enabled = !showQueueSheet) {
        onClose()
    }
    
    if (showQueueSheet) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            containerColor = BackgroundWavve
        ) {
            val queueItems by playerViewModel.queueItems.collectAsState()
            Text("Up Next", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge, color = PrimaryText)
            androidx.compose.foundation.lazy.LazyColumn(contentPadding = PaddingValues(bottom = 64.dp)) {
                items(queueItems.size) { idx ->
                    val track = queueItems[idx]
                    RecentlyPlayedRow(
                        track = track, 
                        playerViewModel = playerViewModel,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onClick = {
                            playerViewModel.playQueueItem(idx)
                        }
                    )
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundWavve
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = PrimaryText,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(Icons.Filled.QueueMusic, contentDescription = "Queue", tint = PrimaryText)
                    }
                    IconButton(onClick = { showLyrics = !showLyrics }) {
                        Icon(
                            imageVector = Icons.Filled.Notes,
                            contentDescription = "Lyrics Toggle",
                            tint = if (showLyrics) AccentColor else PrimaryText
                        )
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu", tint = PrimaryText)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceWavve)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save to playlist", color = PrimaryText) },
                                onClick = { showMenu = false; onSaveToPlaylist() },
                                leadingIcon = { Icon(Icons.Filled.PlaylistAdd, contentDescription = null, tint = SecondaryText) }
                            )
                            DropdownMenuItem(
                                text = { Text("Add to queue", color = PrimaryText) },
                                onClick = { showMenu = false; onAddToQueue() },
                                leadingIcon = { Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = SecondaryText) }
                            )
                            DropdownMenuItem(
                                text = { Text("Download", color = PrimaryText) },
                                onClick = { showMenu = false; onDownload() },
                                leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null, tint = SecondaryText) }
                            )
                            DropdownMenuItem(
                                text = { Text("Go to artist", color = PrimaryText) },
                                onClick = { showMenu = false; onGoToArtist() },
                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = SecondaryText) }
                            )
                            DropdownMenuItem(
                                text = { Text("View song credits", color = PrimaryText) },
                                onClick = { showMenu = false; onViewCredits() },
                                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = SecondaryText) }
                            )
                            DropdownMenuItem(
                                text = { Text("Dismiss queue", color = PrimaryText) },
                                onClick = { showMenu = false; onDismissQueue() },
                                leadingIcon = { Icon(Icons.Filled.ClearAll, contentDescription = null, tint = SecondaryText) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (showLyrics) {
                LyricsView(lyrics = lyrics, currentPosition = currentPosition, duration = duration, modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                if (isLocal) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(DividerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(100.dp))
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artworkUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(DividerColor)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = title, 
                        style = MaterialTheme.typography.headlineMedium, 
                        color = PrimaryText, 
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = artist, 
                        style = MaterialTheme.typography.titleLarge, 
                        color = AccentColor, 
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            var dragProgress by remember { mutableStateOf<Float?>(null) }
            val currentProgress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
            val displayProgress = dragProgress ?: currentProgress
            
            fun formatTime(ms: Long): String {
                val totalSeconds = ms / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                return String.format("%d:%02d", minutes, seconds)
            }
            
            Slider(
                value = displayProgress, 
                onValueChange = { dragProgress = it }, 
                onValueChangeFinished = {
                    dragProgress?.let { onSeek((it * duration).toLong()) }
                    dragProgress = null
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryText,
                    activeTrackColor = PrimaryText,
                    inactiveTrackColor = DividerColor
                )
            )
            
            val displayPosition = (displayProgress * duration).toLong()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(displayPosition), style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                Text("-${formatTime(maxOf(0L, duration - displayPosition))}", style = MaterialTheme.typography.labelMedium, color = SecondaryText)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle", tint = if (shuffleModeEnabled) AccentColor else SecondaryText)
                }
                
                IconButton(onClick = onPrevious, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.FastRewind, contentDescription = "Previous", tint = PrimaryText, modifier = Modifier.size(36.dp))
                }
                
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = AccentColor,
                    onClick = onPlayPause
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = SurfaceWavve,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                IconButton(onClick = onNext, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.FastForward, contentDescription = "Next", tint = PrimaryText, modifier = Modifier.size(36.dp))
                }
                
                IconButton(onClick = onToggleRepeat, modifier = Modifier.size(48.dp)) {
                    val icon = when (repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_ONE -> Icons.Filled.RepeatOne
                        else -> Icons.Filled.Repeat
                    }
                    val tint = if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_OFF) SecondaryText else AccentColor
                    Icon(icon, contentDescription = "Repeat", tint = tint)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LyricsView(
    lyrics: List<com.example.data.LyricLine>,
    currentPosition: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Find the currently active line index
    val activeIndex = remember(currentPosition, lyrics) {
        val index = lyrics.indexOfLast { it.timeMs <= currentPosition }
        if (index != -1) index else 0
    }
    
    // Auto-scroll to active line
    LaunchedEffect(activeIndex) {
        if (lyrics.isNotEmpty() && activeIndex >= 0) {
            scope.launch {
                listState.animateScrollToItem(activeIndex, scrollOffset = -200)
            }
        }
    }

    if (lyrics.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No synced lyrics available", color = SecondaryText, style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 120.dp)
        ) {
            items(lyrics.size) { idx ->
                val line = lyrics[idx]
                val isActive = idx == activeIndex
                val isPast = idx < activeIndex
                
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isActive) PrimaryText else if (isPast) SecondaryText.copy(alpha = 0.5f) else SecondaryText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}


@Composable
fun PlaylistSelectionDialog(
    track: androidx.media3.common.MediaItem,
    playerViewModel: PlayerViewModel,
    onDismiss: () -> Unit
) {
    val playlists by playerViewModel.userPlaylists.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save to Playlist", color = PrimaryText) },
        text = {
            val isLoggedIn = playerViewModel.isLoggedIn.collectAsState(initial = false).value
            androidx.compose.foundation.lazy.LazyColumn {
                if (!isLoggedIn) {
                    item { Text("You need to be logged in to use this feature", color = SecondaryText) }
                } else if (playlists.isEmpty()) {
                    item { Text("No playlists available", color = SecondaryText) }
                } else {
                    items(playlists.size) { idx ->
                        val pl = playlists[idx]
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(pl.name, color = PrimaryText) },
                            onClick = {
                                scope.launch {
                                    playerViewModel.firestoreManager.addTrackToPlaylist(pl.id, track)
                                }
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentColor)
            }
        },
        containerColor = SurfaceWavve
    )
}
