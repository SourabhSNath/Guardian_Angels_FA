package com.gaurdianangels.football.data

data class UpcomingMatch(
    val matchID: String? = "",
    val team1Name: String? = "",
    val team2Name: String? = "",
    val team1Logo: String? = "",
    val team2Logo: String? = "",
//    val date: String? = "",
//    val time: String? = "",
    val team1Team: List<Player>? = null
)