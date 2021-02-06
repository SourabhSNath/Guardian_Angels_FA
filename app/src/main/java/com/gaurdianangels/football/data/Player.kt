package com.gaurdianangels.football.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

public enum class PlayerType {
    GOAL_KEEPER,
    DEFENDER,
    MIDFIELDER,
    FORWARD,
    COACH
}

/**
 * All are Nullable since firestore requires an empty constructor
 */
@Parcelize
data class Player(
    var id: String? = "",
    var playerName: String? = "",
    var playerNumber: String? = "",
    var playerType: PlayerType? = null,
    var playerAge: Int? = null,
    var playerHeight: Int? = null,
    var playerWeight: Int? = null,

    var totalGames: Int? = null,
    // PlayerStats
    var totalGoals: Int? = null,

    // Stats for Goal Keeper
    var totalSaves: Int? = null,
    var totalCleanSheets: Int? = null,

    // Stats for Coach
    var totalWins: Int? = null,

    var remoteUri: String? = "" // Points to the image in firebase storage
) : Parcelable