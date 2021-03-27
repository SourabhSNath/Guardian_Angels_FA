package com.guardianangels.football.util

import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.guardianangels.football.R
import timber.log.Timber

fun Any?.toStringOrEmpty(): String {
    if (this is Number) {
        if (this == 0 || this == 0.0) {
            return ""
        }
    }
    return this?.toString() ?: ""
}

fun TextView.getString() = this.text.toString().trim()

/**
 * Get the number from a String without NumberFormatException.
 */
fun String.toEmptySafeInt(): Int = if (this == "") 0 else this.toInt()
fun String.toEmptySafeFloat(): Float = if (this == "") 0f else this.toFloat()

/**
 * To check and set the Team Logos
 */
fun ImageView.setTeamLogo(logoLink: String, teamName: String) {
    if (logoLink.isNotEmpty()) {
        Timber.d("Load team Logo from Firebase.")
        load(logoLink)
    } else {
        if (teamName == context.getString(R.string.guardian_angels)) {
            Timber.d("Load GA logo.")
            load(R.drawable.gaurdian_angels)
        } else {
            Timber.d("Load Football Logo")
            load(R.drawable.ic_football)
        }
    }
}