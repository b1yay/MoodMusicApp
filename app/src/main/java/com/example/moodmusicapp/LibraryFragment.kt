package com.example.moodmusicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LibraryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        val rvLibrary = view.findViewById<RecyclerView>(R.id.rvLibrary)
        val allSongs = SongRepository.allSongs

        val adapter = SongAdapter(allSongs, { song ->
            MusicPlayer.setPlaylist(allSongs)
            MusicPlayer.playSong(requireContext(), song)
        }, { song ->
            // Favorite status updated in adapter
        })

        rvLibrary.layoutManager = LinearLayoutManager(requireContext())
        rvLibrary.adapter = adapter

        return view
    }
}
