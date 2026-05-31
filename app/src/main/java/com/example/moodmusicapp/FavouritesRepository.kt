package com.example.moodmusicapp

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FavouritesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserFavouritesRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("favourites")

    suspend fun addFavourite(song: Song) {
        val data = hashMapOf(
            "id" to song.id,
            "title" to song.title,
            "artist" to song.artist,
            "imageUrl" to song.imageUrl,
            "mood" to song.mood,
            "addedAt" to Timestamp.now()
        )
        getUserFavouritesRef().document(song.id).set(data).await()
    }

    suspend fun removeFavourite(songId: String) {
        getUserFavouritesRef().document(songId).delete().await()
    }

    suspend fun isFavourite(songId: String): Boolean {
        val doc = getUserFavouritesRef().document(songId).get().await()
        return doc.exists()
    }

    suspend fun getAllFavourites(): List<Song> {
        val snapshot = getUserFavouritesRef()
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            Song(
                id = doc.getString("id") ?: return@mapNotNull null,
                title = doc.getString("title") ?: return@mapNotNull null,
                artist = doc.getString("artist") ?: return@mapNotNull null,
                imageUrl = doc.getString("imageUrl"),
                mood = doc.getString("mood") ?: "Unknown",
                isFavorite = true
            )
        }
    }

    suspend fun getFavouritesCount(): Int {
        val snapshot = getUserFavouritesRef().get().await()
        return snapshot.size()
    }

    fun observeFavourites(onUpdate: (List<Song>) -> Unit) {
        getUserFavouritesRef()
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FAVOURITES", "observeFavourites listen error: ${error.message}")
                }
                val songs = snapshot?.documents?.mapNotNull { doc ->
                    Song(
                        id = doc.getString("id") ?: return@mapNotNull null,
                        title = doc.getString("title") ?: return@mapNotNull null,
                        artist = doc.getString("artist") ?: return@mapNotNull null,
                        imageUrl = doc.getString("imageUrl"),
                        mood = doc.getString("mood") ?: "Unknown",
                        isFavorite = true
                    )
                } ?: emptyList()
                onUpdate(songs)
            }
    }

    // --- STATS ---

    private fun getUserStatsRef() = db.collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("stats")
        .document("summary")

    suspend fun incrementSongsPlayed() {
        try {
            getUserStatsRef().set(
                mapOf("songsPlayed" to FieldValue.increment(1)),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("STATS", "Error incrementing songs played: ${e.message}")
        }
    }

    suspend fun addMoodUsed(mood: String) {
        try {
            getUserStatsRef().set(
                mapOf("moodsUsed" to FieldValue.arrayUnion(mood)),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("STATS", "Error adding mood: ${e.message}")
        }
    }

    fun observeStats(onUpdate: (songsPlayed: Int, moodsUsed: Int) -> Unit) {
        getUserStatsRef().addSnapshotListener { snapshot, _ ->
            val songsPlayed = snapshot?.getLong("songsPlayed")?.toInt() ?: 0
            val moodsUsed = (snapshot?.get("moodsUsed") as? List<*>)?.size ?: 0
            onUpdate(songsPlayed, moodsUsed)
        }
    }
}
