package com.example.moodmusicapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SongAdapter(
    private var songs: List<Song>,
    private val onPlayClick: (Song) -> Unit,
    private val onFavoriteClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvSongTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val btnPlay: ImageView = view.findViewById(R.id.btnPlay)
        val btnFavorite: ImageView = view.findViewById(R.id.btnFavorite)
        val ivAlbumArt: ImageView = view.findViewById(R.id.ivSongIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        // Handle image loading cases
        if (song.imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(song.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivAlbumArt)
        } else if (song.imageFileName != null) {
            val resId = holder.itemView.context.resources.getIdentifier(
                song.imageFileName, "drawable", holder.itemView.context.packageName
            )
            holder.ivAlbumArt.setImageResource(if (resId != 0) resId else R.drawable.logo_glow_ring)
        } else {
            // Default placeholder image
            holder.ivAlbumArt.setImageResource(R.drawable.logo_glow_ring)
        }

        // Highlight currently playing song
        if (MusicPlayer.currentPlayingSongId == song.id) {
            holder.tvTitle.setTextColor(Color.parseColor("#C084FC"))
            holder.btnPlay.setImageResource(
                if (MusicPlayer.isPlaying()) android.R.drawable.ic_media_pause 
                else android.R.drawable.ic_media_play
            )
        } else {
            holder.tvTitle.setTextColor(Color.WHITE)
            holder.btnPlay.setImageResource(android.R.drawable.ic_media_play)
        }

        // Favorite State
        holder.btnFavorite.setImageResource(
            if (song.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_empty
        )
        holder.btnFavorite.setColorFilter(if (song.isFavorite) Color.RED else Color.parseColor("#C9B8FF"))

        holder.btnPlay.setOnClickListener {
            onPlayClick(song)
            notifyDataSetChanged()
        }

        holder.btnFavorite.setOnClickListener {
            song.isFavorite = !song.isFavorite
            onFavoriteClick(song)
            val msg = if (song.isFavorite) "Added to favorites ❤️" else "Removed from favorites"
            Toast.makeText(holder.itemView.context, msg, Toast.LENGTH_SHORT).show()
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = songs.size

    fun updateData(newSongs: List<Song>) {
        songs = newSongs.toMutableList()
        notifyDataSetChanged()
    }
}
