package com.example.moodmusicapp

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object PlaylistRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserPlaylistsRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("playlists")

    suspend fun createPlaylist(name: String) {
        val ref = getUserPlaylistsRef().document()
        val data = hashMapOf(
            "id" to ref.id,
            "name" to name,
            "songCount" to 0,
            "createdAt" to Timestamp.now(),
            "userId" to (auth.currentUser?.uid ?: "")
        )
        ref.set(data).await()
    }

    suspend fun deletePlaylist(playlistId: String) {
        getUserPlaylistsRef().document(playlistId).delete().await()
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        val playlistRef = getUserPlaylistsRef().document(playlistId)
        val songsRef = playlistRef.collection("songs")
        
        val data = hashMapOf(
            "id" to song.id,
            "title" to song.title,
            "artist" to song.artist,
            "imageUrl" to song.imageUrl,
            "mood" to song.mood,
            "addedAt" to Timestamp.now()
        )
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(playlistRef)
            val currentCount = snapshot.getLong("songCount") ?: 0
            transaction.set(songsRef.document(song.id), data)
            transaction.update(playlistRef, "songCount", currentCount + 1)
        }.await()
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val playlistRef = getUserPlaylistsRef().document(playlistId)
        val songRef = playlistRef.collection("songs").document(songId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(playlistRef)
            val currentCount = snapshot.getLong("songCount") ?: 0
            transaction.delete(songRef)
            transaction.update(playlistRef, "songCount", if (currentCount > 0) currentCount - 1 else 0)
        }.await()
    }

    suspend fun getPlaylistSongs(playlistId: String): List<Song> {
        val snapshot = getUserPlaylistsRef().document(playlistId)
            .collection("songs")
            .orderBy("addedAt", Query.Direction.DESCENDING)
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
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PLAYLISTS", "Listen error: ${error.message}")
                    return@addSnapshotListener
                }
                val playlists = snapshot?.documents?.mapNotNull { doc ->
                    UserPlaylist(
                        id = doc.getString("id") ?: return@mapNotNull null,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        songCount = doc.getLong("songCount")?.toInt() ?: 0,
                        userId = doc.getString("userId") ?: ""
                    )
                } ?: emptyList()
                onUpdate(playlists)
            }
    }
}
