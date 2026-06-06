package com.example.moodmusicapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object PlaylistRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserPlaylistsRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("playlists")

    suspend fun createPlaylist(name: String): String {
        val data = hashMapOf(
            "name" to name,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "songCount" to 0
        )
        val ref = getUserPlaylistsRef().add(data).await()
        return ref.id
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        val songData = hashMapOf(
            "id" to song.id,
            "title" to song.title,
            "artist" to song.artist,
            "imageUrl" to song.imageUrl,
            "mood" to song.mood,
            "addedAt" to com.google.firebase.Timestamp.now()
        )
        getUserPlaylistsRef().document(playlistId)
            .collection("songs").document(song.id)
            .set(songData).await()
        getUserPlaylistsRef().document(playlistId)
            .update("songCount", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        getUserPlaylistsRef().document(playlistId)
            .collection("songs").document(songId)
            .delete().await()
        getUserPlaylistsRef().document(playlistId)
            .update("songCount", com.google.firebase.firestore.FieldValue.increment(-1))
            .await()
    }

    suspend fun deletePlaylist(playlistId: String) {
        getUserPlaylistsRef().document(playlistId).delete().await()
    }

    suspend fun getAllPlaylists(): List<UserPlaylist> {
        val snapshot = getUserPlaylistsRef()
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            UserPlaylist(
                id = doc.id,
                name = doc.getString("name") ?: return@mapNotNull null,
                songCount = doc.getLong("songCount")?.toInt() ?: 0
            )
        }
    }

    suspend fun getSongsInPlaylist(playlistId: String): List<Song> {
        val snapshot = getUserPlaylistsRef()
            .document(playlistId).collection("songs")
            .orderBy("addedAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            Song(
                id = doc.getString("id") ?: return@mapNotNull null,
                title = doc.getString("title") ?: return@mapNotNull null,
                artist = doc.getString("artist") ?: return@mapNotNull null,
                imageUrl = doc.getString("imageUrl"),
                mood = doc.getString("mood") ?: "Unknown"
            )
        }
    }

    fun observePlaylists(onUpdate: (List<UserPlaylist>) -> Unit) {
        getUserPlaylistsRef()
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val playlists = snapshot?.documents?.mapNotNull { doc ->
                    UserPlaylist(
                        id = doc.id,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        songCount = doc.getLong("songCount")?.toInt() ?: 0
                    )
                } ?: emptyList()
                onUpdate(playlists)
            }
    }
}
