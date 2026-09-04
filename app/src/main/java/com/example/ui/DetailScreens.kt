package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: String?,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    if (artistId == null) {
        onBack()
        return
    }

    var tracks by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(artistId) {
        isLoading = true
        tracks = playerViewModel.searchTracksForDetail(artistId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artistId, fontWeight = FontWeight.Bold, color = PrimaryText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWavve
                )
            )
        },
        containerColor = BackgroundWavve
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentColor)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 120.dp, start = 16.dp, end = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Top Songs", style = MaterialTheme.typography.titleMedium, color = PrimaryText, modifier = Modifier.padding(bottom = 16.dp))
                }

                if (tracks.isEmpty()) {
                    item { Text("No tracks found for this artist.", color = SecondaryText) }
                } else {
                    items(tracks.size) { idx ->
                        val track = tracks[idx]
                        DetailTrackRow(
                            track = track,
                            index = idx + 1,
                            onClick = { playerViewModel.playTrack(track) },
                            playerViewModel = playerViewModel
                        )
                        if (idx < tracks.size - 1) {
                            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String?,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    if (playlistId == null) {
        onBack()
        return
    }

    val playlists by playerViewModel.userPlaylists.collectAsState(initial = emptyList())
    var sharedPlaylist by remember { mutableStateOf<PlaylistData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(playlistId, playlists) {
        val local = playlists.find { it.id == playlistId }
        if (local != null) {
            sharedPlaylist = local
            isLoading = false
        } else {
            sharedPlaylist = playerViewModel.firestoreManager.getSharedPlaylist(playlistId)
            isLoading = false
        }
    }

    val playlist = sharedPlaylist

    if (isLoading) {
        Scaffold(containerColor = BackgroundWavve) { _ ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentColor)
            }
        }
        return
    }

    if (playlist == null) {
        Scaffold(containerColor = BackgroundWavve) { _ ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Playlist not found", color = SecondaryText)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Go Back") }
                }
            }
        }
        return
    }

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by playerViewModel.currentUser.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist.name, fontWeight = FontWeight.Bold, color = PrimaryText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this playlist!")
                            putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${playlist.name} on Wavve: wavve://playlist/${playlist.id}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Playlist"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = AccentColor)
                    }
                    if (playlist.ownerId == currentUser?.uid) {
                        IconButton(onClick = {
                            scope.launch {
                                val success = playerViewModel.firestoreManager.deletePlaylist(playlistId)
                                if (success) onBack()
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Playlist", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWavve
                )
            )
        },
        containerColor = BackgroundWavve
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 120.dp, start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("${playlist.tracks.size} tracks", style = MaterialTheme.typography.bodyMedium, color = SecondaryText, modifier = Modifier.padding(bottom = 16.dp))
            }

            if (playlist.tracks.isEmpty()) {
                item { Text("No tracks in this playlist yet.", color = SecondaryText) }
            } else {
                items(playlist.tracks.size) { idx ->
                    val trackData = playlist.tracks[idx]
                    val mediaItem = MediaItem.Builder()
                        .setMediaId(trackData.id)
                        .setUri(trackData.mediaUri?.let { Uri.parse(it) } ?: Uri.EMPTY)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(trackData.title)
                                .setArtist(trackData.artist)
                                .setArtworkUri(trackData.artworkUri?.let { Uri.parse(it) })
                                .build()
                        ).build()

                    DetailTrackRow(
                        track = mediaItem,
                        index = idx + 1,
                        onClick = {
                            playerViewModel.playTrack(mediaItem)
                        },
                        playerViewModel = playerViewModel,
                        onRemove = if (playlist.ownerId == currentUser?.uid) {
                            {
                                scope.launch {
                                    playerViewModel.firestoreManager.removeTrackFromPlaylist(playlistId, trackData.id)
                                }
                            }
                        } else null
                    )
                    if (idx < playlist.tracks.size - 1) {
                        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

/**
 * A simple row for displaying a track in a detail screen.
 * Shows index, artwork, title, artist, and a download action via the DropdownMenu.
 */
@Composable
fun DetailTrackRow(
    track: MediaItem,
    index: Int,
    onClick: () -> Unit,
    playerViewModel: PlayerViewModel,
    onRemove: (() -> Unit)? = null
) {
    val title = track.mediaMetadata.title?.toString() ?: "Unknown"
    val artist = track.mediaMetadata.artist?.toString() ?: "Unknown Artist"
    val artworkUri = track.mediaMetadata.artworkUri
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            modifier = Modifier.width(28.dp)
        )
        val isLocal = track.mediaId.startsWith("local_")
        if (isLocal) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DividerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(24.dp))
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUri ?: "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=200&auto=format&fit=crop")
                    .crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DividerColor)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp), fontWeight = FontWeight.SemiBold, color = PrimaryText, maxLines = 1)
            Text(artist, style = MaterialTheme.typography.bodySmall, color = SecondaryText, maxLines = 1)
        }
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
            val queueItems by playerViewModel.queueItems.collectAsState(initial = emptyList())
            val isInQueue = queueItems.any { it.mediaId == track.mediaId }
            val playlists by playerViewModel.userPlaylists.collectAsState(initial = emptyList())
            val isLoggedIn = playerViewModel.isLoggedIn.collectAsState(initial = false).value
            val scope = rememberCoroutineScope()

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(SurfaceWavve)
            ) {
                if (isInQueue) {
                    DropdownMenuItem(
                        text = { Text("Remove from queue", color = PrimaryText) },
                        onClick = {
                            playerViewModel.removeFromQueue(track)
                            showMenu = false
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Add to queue", color = PrimaryText) },
                        onClick = {
                            playerViewModel.addToQueue(track)
                            showMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Download", color = PrimaryText) },
                    onClick = {
                        showMenu = false
                        playerViewModel.downloadTrack(track, context)
                    }
                )
                
                if (onRemove == null) {
                    if (!isLoggedIn) {
                        DropdownMenuItem(
                            text = { Text("Login to save to playlist", color = SecondaryText) },
                            onClick = { 
                                android.widget.Toast.makeText(context, "You need to be logged in to use this feature", android.widget.Toast.LENGTH_SHORT).show()
                                showMenu = false 
                            }
                        )
                    } else if (playlists.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No playlists available", color = SecondaryText) },
                            onClick = { showMenu = false }
                        )
                    } else {
                        playlists.forEach { pl ->
                            DropdownMenuItem(
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
                
                if (onRemove != null) {
                    DropdownMenuItem(
                        text = { Text("Remove from playlist", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}
