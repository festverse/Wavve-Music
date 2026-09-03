# Wavve Music

Wavve is a minimalist, editorial-grade music streaming application inspired by the design languages of Apple Music and Spotify. Built entirely with modern Android development standards, the app serves as a comprehensive showcase of scalable architecture, background media processing, and polished user interface design. 

This repository hosts the release APK for demonstration purposes.

## Tech Stack

The application is built natively for Android using a modern Kotlin and Jetpack Compose stack:

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architecture:** MVVM (Model-View-ViewModel) with unidirectional data flow
*   **Media Playback:** AndroidX Media3 (ExoPlayer) & MediaLibraryService for background audio
*   **Asynchronous Programming:** Kotlin Coroutines and StateFlow
*   **Backend & Authentication:** Firebase Auth, Firebase Firestore
*   **Local Storage:** Room Database and SharedPreferences (State persistence)
*   **Image Loading:** Coil
*   **Networking:** Retrofit / OkHttp (REST API integrations)

## Key Features

*   **Seamless Dual-API Search:** The app concurrently fetches and merges data from both the iTunes Search API (commercial tracks) and the Jamendo API (independent tracks), presenting them in a unified, fluid search experience.
*   **Background Playback & Lockscreen Controls:** Implements a full MediaSession service, allowing playback to continue smoothly in the background, complete with system notification controls and lockscreen integration.
*   **Persistent Playback State:** The application automatically serializes the active audio queue, current track index, and exact millisecond timestamp. When the app is killed and relaunched, it instantly restores the user's exact listening state.
*   **Dynamic Queue Management:** Users can add tracks to the queue, remove them, or jump to specific songs seamlessly via a custom modal bottom sheet. 
*   **Real-time Synced Lyrics:** Features a custom LRC parser that synchronizes lyrics to the exact millisecond of the playing audio track.
*   **User Authentication & Profiles:** Secure login system via Firebase, allowing users to save personal playlists, track listening history, and customize their profile using the modern Android 13+ Photo Picker.
*   **Polished UI/UX:** Features advanced Compose layout techniques, including custom sliders for precise audio seeking, marquee scrolling text for long titles, gradient fading edges, and fluid transitions.

## Limitations

*   **30-Second Commercial Previews:** Because Wavve is a non-commercial portfolio project, it does not have licensing agreements with major record labels. To comply with copyright laws, mainstream commercial tracks (fetched via the iTunes API) are strictly limited to 30-second audio previews. Full-length playback is only available for independent, royalty-free tracks sourced from the Jamendo catalog.
*   **Lyrics Availability:** Synchronized lyrics are dependent on the public Lyrics.ovh database. If a specific track's LRC data is missing from the public database, the lyrics feature will fallback to a "Not Available" state.

## Credits & APIs

Wavve relies on the following public APIs to populate its content:

*   **iTunes Search API:** For mainstream track metadata and audio previews.
*   **Jamendo API:** For independent music and full-length royalty-free streaming.
*   **Lyrics.ovh:** For retrieving synchronized song lyrics.
*   **DiceBear API:** For generating fallback user profile avatars.

## Contact

Developed as a showcase of modern Android engineering. If you are reviewing this project for a technical evaluation or hiring process and would like to request access to the private source code repository, please reach out directly.
