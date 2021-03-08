package com.guardianangels.football.util

fun Any?.toStringOrEmpty(): String {
    if (this is Number) {
        if (this == 0 || this == 0.0) {
            return ""
        }
    }
    return this?.toString() ?: ""
}