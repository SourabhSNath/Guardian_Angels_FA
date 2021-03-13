package com.guardianangels.football.util

fun Any?.toStringOrEmpty(): String {
    if (this is Number) {
        if (this == 0 || this == 0.0) {
            return ""
        }
    }
    return this?.toString() ?: ""
}

/**
 * Get the number from a String without NumberFormatException.
 */
fun String.toEmptySafeInt(): Int = if (this == "") 0 else this.toInt()
fun String.toEmptySafeFloat(): Float = if (this == "") 0f else this.toFloat()