package com.guardianangels.football.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

enum class PlayerType {
    GOAL_KEEPER,
    DEFENDER,
    MIDFIELDER,
    FORWARD,
    COACH
}

/**
 * All are Nullable since firestore requires an empty constructor
 */
@Keep
@Parcelize
data class Player(

    @get:Exclude // Exclude the id since it's only set at the time of download
    var id: String? = "",

    var playerName: String? = "",
    var playerNumber: String? = "",
    var playerType: PlayerType? = null,
    var playerAge: Int? = null,
    var playerHeight: Float? = null,
    var playerWeight: Float? = null,

    var totalGames: Int? = null,
    // PlayerStats
    var totalGoals: Int? = null,

    // Stats for Goal Keeper
    var totalSaves: Int? = null,
    var totalCleanSheets: Int? = null,

    // Stats for Coach
    var totalWins: Int? = null,

    var remoteUri: String? = "", // Points to the image in firebase storage
) : Parcelable