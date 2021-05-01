package com.guardianangels.football.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Picture(
    var imageLink: String? = "",
    var imageDescription: String = "",
    var dateAndTime: Long? = null,

    @get:Exclude // Exclude the id since it's only set at the time of download
    var id: String? = null
) : Parcelable
