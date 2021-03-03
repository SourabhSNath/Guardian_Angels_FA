package com.guardianangels.football.data

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Match(
    val team1Name: String? = "",
    val team2Name: String? = "",
    var team1Logo: String? = "",
    var team2Logo: String? = "",
    val dateAndTime: Long? = null,
    var tournamentName: String? = "",
    var locationName: String? = "",
    val team1TeamIds: List<String>? = null,
    var isCompleted: Boolean? = null,

    @get:Exclude // Exclude the id since it's only set at the time of download
    var matchID: String? = "",
) : Parcelable