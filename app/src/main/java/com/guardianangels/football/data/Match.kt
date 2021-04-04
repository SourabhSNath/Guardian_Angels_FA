package com.guardianangels.football.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Match(
    var team1Name: String? = "",
    var team2Name: String? = "",
    var team1Logo: String? = "",
    var team2Logo: String? = "",
    var dateAndTime: Long? = null,
    var tournamentName: String? = "",
    var locationName: String? = "",
    var team1TeamIds: List<String>? = null,

    var isCompleted: Boolean? = null,

    val team1Score: Int? = null,
    val team2Score: Int? = null,

    val team1ShootingStats: Int? = null,
    val team2ShootingStats: Int? = null,
    val team1AttactStats: Int? = null,
    val team2AttackStats: Int? = null,
    val team1PossesionStats: Int? = null,
    val team2PossesionStats: Int? = null,
    val team1CardStats: Int? = null,
    val team2CardStats: Int? = null,
    val team1CornerStats: Int? = null,
    val team2CornerStats: Int? = null,

    val matchNotes: String? = "",

    var gameResult: GameResults? = null,

    @get:Exclude // Exclude the id since it's only set at the time of download
    var matchID: String? = "",
) : Parcelable