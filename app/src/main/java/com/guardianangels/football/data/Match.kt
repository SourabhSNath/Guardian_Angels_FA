package com.guardianangels.football.data

import com.google.firebase.firestore.Exclude

data class UpcomingMatch(
    val team1Name: String? = "",
    val team2Name: String? = "",
    var team1Logo: String? = "",
    var team2Logo: String? = "",
    val dateAndTime: Long? = null,
    val team1Team: List<Player>? = null,

    @get:Exclude // Exclude the id since it's only set at the time of download
    val matchID: String? = "",
)