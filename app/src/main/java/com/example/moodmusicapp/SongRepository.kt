package com.example.moodmusicapp

object SongRepository {
    val allSongs = mutableListOf(
        Song("1", "Cheap Thrills", "Sia", "Happy", "cheap_thrills", "cheap_thrills"),
        Song("2", "Pasoori", "Shae Gill & Ali Sethi", "Happy", "pasoori", "pasoori"),
        Song("3", "Ye ishq hai", "Shreya Ghoshal", "Happy", "ye_ishq_hai", "ye_ishq_hai"),
        Song("4", "Boyfriend", "Karan Aujla", "Happy", "boyfriend", "boyfriend"),
        Song("5", "Mang le je dil launa chani ayn", "Diljit & Sia", "Happy", "mang_le", "mang_le"),
        Song("6", "Tere Liye", "Atif Aslam", "Happy", "tere_liye", "tere_liye"),
        Song("7", "Dil diyan gallan", "Atif Aslam", "Romantic", "dil_diyan_gallan", "dil_diyan_gallan"),
        Song("8", "Gerua", "Arijit Singh", "Romantic", "gerua", "gerua"),
        Song("9", "Gehra hua", "Arijit Singh", "Romantic", "gehra_hua", "gehra_hua"),
        Song("10", "Aarzu", "Noor, Khan & Madhurxo", "Romantic", "aarzu", "aarzu"),
        Song("11", "Khat", "Navjot Ahuja", "Romantic", "khat", "khat"),
        Song("12", "Hum", "Murtaza Qizilbash", "Romantic", "hum", "hum"),
        Song("13", "Baat Unkahi", "Kaavish", "Sad", "baat_unkahi", "baat_unkahi"),
        Song("14", "The night we met", "Lord Huron", "Sad", "the_night_we_met", "the_night_we_met"),
        Song("15", "Mujhy tum nazar se gira to rhy ho", "Mehdi Hassan", "Sad", "mujhy_tum_nazar_se", "mujhy_tum_nazar_se"),
        Song("16", "Faasle", "Kaavish & Quratulain Balouch", "Sad", "faasle", "faasle"),
        Song("17", "Afsos", "Anuv Jain", "Sad", "afsos", "afsos"),
        Song("18", "Obvious", "Hassan Raheem", "Sad", "obvious", "obvious"),
        Song("19", "Chalo Door Kahin", "Samar Jafri", "Chill", "chalo_door_kahin", "chalo_door_kahin"),
        Song("20", "A Thousand Years", "Christina Perri", "Chill", "thousand_years", "thousand_years"),
        Song("21", "I Think They Call This Love", "Metthew Ifield", "Chill", "they_call_this_love", "they_call_this_love"),
        Song("22", "Tose Naina Laage", "Shilpa Rao", "Chill", "tose_naina_laage", "tose_naina_laage"),
        Song("23", "Gal Sun Janiya Janiya", "Nehal Naseem & Aashir Wajahat", "Chill", "gal_sun_janiya", "gal_sun_janiya"),
        Song("24", "Samjho Na", "Aditiya Rikhari", "Chill", "smjho_na", "smjho_na"),
        Song("25", "Believer", "Imagine Dragons", "Angry", "believer", "believer"),
        Song("26", "One Dance", "Drake", "Angry", "one_dance", "one_dance"),
        Song("27", "The Beast", "Cheema Y", "Angry", "the_beast", "the_beast"),
        Song("28", "Wavy", "Karan Aujla", "Angry", "wavy", "wavy"),
        Song("29", "DarkSide", "NEONI", "Angry", "darkside", "darkside"),
        Song("30", "On Top", "Karan Aujla", "Angry", "on_top", "on_top")
    )

    fun getSongsByMood(mood: String): List<Song> {
        return allSongs.filter { it.mood.equals(mood, ignoreCase = true) }
    }

    fun getFavoriteSongs(): List<Song> {
        return allSongs.filter { it.isFavorite }
    }

    // Placeholder for playlists - currently returns empty
    fun getFavoritePlaylists(): List<Any> {
        return emptyList()
    }
}
