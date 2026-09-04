package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.foundation.basicMarquee
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@androidx.compose.material3.ExperimentalMaterial3Api
@androidx.compose.foundation.layout.ExperimentalLayoutApi
@Composable
fun WavveApp(playerViewModel: PlayerViewModel) {
    val navController = rememberNavController()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentMediaItem by playerViewModel.currentMediaItem.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val duration by playerViewModel.duration.collectAsState()
val currentLyrics by playerViewModel.currentLyrics.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val shuffleModeEnabled by playerViewModel.shuffleModeEnabled.collectAsState()

    var showCreditsDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showNowPlayingFullScreen by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentMediaItem != null && !showNowPlayingFullScreen) {
                Column {
                    MiniPlayer(
                        title = currentMediaItem?.mediaMetadata?.title?.toString() ?: "Unknown",
                        artist = currentMediaItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
                        artworkUri = currentMediaItem?.mediaMetadata?.artworkUri?.toString(),
                        isLocal = currentMediaItem?.mediaId?.startsWith("local_") == true,
                        isPlaying = isPlaying,
                        onPlayPause = {
                            if (isPlaying) playerViewModel.pause() else playerViewModel.play()
                        },
                        onNext = { playerViewModel.skipToNext() },
                        onPrevious = { playerViewModel.skipToPrevious() },
                        onClick = { showNowPlayingFullScreen = true }
                    )
                    WavveBottomNavigation(navController)
                }
            } else if (!showNowPlayingFullScreen) {
                WavveBottomNavigation(navController)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = BackgroundWavve
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { 
                    HomeScreen(
                        playerViewModel = playerViewModel, 
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToArtist = { arId -> navController.navigate("artist_detail/$arId") }
                    ) 
                }
                composable("library") { 
                    LibraryScreen(
                        playerViewModel = playerViewModel,
                        onNavigateToPlaylist = { plId -> navController.navigate("playlist_detail/$plId") },
                        onNavigateToArtist = { arId -> navController.navigate("artist_detail/$arId") }
                    ) 
                }
                composable("search") { SearchScreen(playerViewModel) }
                composable("artist_detail/{artistId}") { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString("artistId")
                    ArtistDetailScreen(artistId = artistId, playerViewModel = playerViewModel, onBack = { navController.popBackStack() })
                }
                composable(
                    route = "playlist_detail/{playlistId}",
                    deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "wavve://playlist/{playlistId}" })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId")
                    PlaylistDetailScreen(playlistId = playlistId, playerViewModel = playerViewModel, onBack = { navController.popBackStack() })
                }
                composable("settings") { 
                    SettingsScreen(
                        playerViewModel = playerViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAuth = { navController.navigate("auth") }
                    ) 
                }
                composable("auth") {
                    AuthScreen(
                        playerViewModel = playerViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
    


    AnimatedVisibility(
        visible = showNowPlayingFullScreen && currentMediaItem != null,
        modifier = Modifier.fillMaxSize(),
        enter = androidx.compose.animation.slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy)
        ),
        exit = androidx.compose.animation.slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
        )
    ) {
        NowPlayingScreen(
            title = currentMediaItem?.mediaMetadata?.title?.toString() ?: "Unknown",
            artist = currentMediaItem?.mediaMetadata?.artist?.toString() ?: "Unknown Artist",
            artworkUri = currentMediaItem?.mediaMetadata?.artworkUri?.toString(),
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            lyrics = currentLyrics,
            repeatMode = repeatMode,
            shuffleModeEnabled = shuffleModeEnabled,
            playerViewModel = playerViewModel,
            onSeek = { playerViewModel.seekTo(it) },
            onPlayPause = {
                if (isPlaying) playerViewModel.pause() else playerViewModel.play()
            },
            onClose = { showNowPlayingFullScreen = false },
            onNext = { playerViewModel.skipToNext() },
            onPrevious = { playerViewModel.skipToPrevious() },
            onToggleRepeat = { playerViewModel.toggleRepeatMode() },
            onToggleShuffle = { playerViewModel.toggleShuffleMode() },
            onSaveToPlaylist = { showPlaylistDialog = true },
            onAddToQueue = { currentMediaItem?.let { playerViewModel.addToQueue(it) } },
            onDownload = { currentMediaItem?.let { playerViewModel.downloadTrack(it, context) } },
            onGoToArtist = { 
                currentMediaItem?.mediaMetadata?.artist?.let { 
                    playerViewModel.updateSearchQuery(it.toString())
                    playerViewModel.performSearch()
                    navController.navigate("search")
                    showNowPlayingFullScreen = false
                } 
            },
            onViewCredits = { showCreditsDialog = true },
            onDismissQueue = { playerViewModel.clearQueue() }
        )
    }

    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showCreditsDialog = false },
            title = { Text("Song Credits") },
            text = { Text("Title: ${currentMediaItem?.mediaMetadata?.title}\nArtist: ${currentMediaItem?.mediaMetadata?.artist}\n\nProvided by Wavve Music Platform.") },
            confirmButton = { TextButton(onClick = { showCreditsDialog = false }) { Text("OK") } }
        )
    }
    
    if (showPlaylistDialog) {
        currentMediaItem?.let { track ->
            PlaylistSelectionDialog(
                track = track,
                playerViewModel = playerViewModel,
                onDismiss = { showPlaylistDialog = false }
            )
        }
    }
}


@Composable
fun MiniPlayer(
    title: String,
    artist: String,
    artworkUri: String?,
    isLocal: Boolean = false,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .clip(RoundedCornerShape(16.dp)),
            color = AccentColor,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocal) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DividerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(20.dp))
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artworkUri ?: "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?q=80&w=100&auto=format&fit=crop")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DividerColor)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f).height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.1.em
                            ),
                            color = Color.White.copy(alpha = 0.5f),
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE
                            )
                        )
                    }
                    // Fade out edge
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(24.dp)
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, AccentColor)
                                )
                            )
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(
                        imageVector = Icons.Filled.FastRewind,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onPrevious() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = AccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onNext() }
                    )
                }
            }
        }
    }
}

@Composable
fun WavveBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxWidth().background(SurfaceWavve)) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem("Home", Icons.Filled.Home, currentRoute == "home") { navController.navigate("home") }
            BottomNavItem("Search", Icons.Filled.Search, currentRoute == "search") { navController.navigate("search") }
            BottomNavItem("Library", Icons.Filled.LibraryMusic, currentRoute == "library") { navController.navigate("library") }
        }
    }
}

@Composable
fun BottomNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) AccentColor else SecondaryText
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.05).em
            ),
            color = color
        )
    }
}
