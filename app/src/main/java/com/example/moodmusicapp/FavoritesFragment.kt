package com.example.moodmusicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class FavoritesFragment : Fragment() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var emptyStateContainer: View
    private lateinit var tvFavCount: TextView
    private lateinit var tvPlaylistCount: TextView
    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        rvFavorites = view.findViewById(R.id.rvFavorites)
        emptyStateContainer = view.findViewById(R.id.emptyStateCard)
        tvFavCount = view.findViewById(R.id.tvFavCount)
        tvPlaylistCount = view.findViewById(R.id.tvPlaylistCount)
        val btnExploreMoods = view.findViewById<View>(R.id.btnExploreMoods)

        setupRecyclerView()
        updateUI()

        btnExploreMoods.setOnClickListener {
            // Navigate back to MoodFragment via BottomNav
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
            bottomNav.selectedItemId = R.id.nav_mood
        }

        return view
    }

    private fun setupRecyclerView() {
        val favoriteSongs = SongRepository.getFavoriteSongs()
        adapter = SongAdapter(favoriteSongs, { song ->
            MusicPlayer.setPlaylist(favoriteSongs)
            MusicPlayer.playSong(requireContext(), song)
            adapter.notifyDataSetChanged()
        }, { song ->
            updateUI()
        })

        rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        rvFavorites.adapter = adapter
    }

    private fun updateUI() {
        val favoriteSongs = SongRepository.getFavoriteSongs()
        tvFavCount.text = favoriteSongs.size.toString()
        
        // Now using data from repository, currently 0
        val favoritePlaylists = SongRepository.getFavoritePlaylists()
        tvPlaylistCount.text = favoritePlaylists.size.toString()

        if (favoriteSongs.isEmpty()) {
            rvFavorites.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
        } else {
            rvFavorites.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
            adapter.updateData(favoriteSongs)
        }
    }
}
