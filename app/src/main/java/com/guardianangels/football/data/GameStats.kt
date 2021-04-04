package com.guardianangels.football.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

enum class GameResults { WIN, LOSS, DRAW }

@Keep
@Parcelize
data class GameStats(
    var games: Int? = null,
    var wins: Int? = null,
    var draws: Int? = null,
    var losses: Int? = null,

    @get:Exclude
    var id: String? = null
) : Parcelable