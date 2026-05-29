package com.example.moodmusicapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class MoodFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        view.findViewById<CardView>(R.id.btnHappy).setOnClickListener { openSongList("Happy") }
        view.findViewById<CardView>(R.id.btnSad).setOnClickListener { openSongList("Sad") }
        view.findViewById<CardView>(R.id.btnChill).setOnClickListener { openSongList("Chill") }
        view.findViewById<CardView>(R.id.btnRomantic).setOnClickListener { openSongList("Romantic") }
        view.findViewById<CardView>(R.id.btnAngry).setOnClickListener { openSongList("Angry") }

        return view
    }

    private fun openSongList(mood: String) {
        val fragment = SongListFragment.newInstance(mood)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }
}
