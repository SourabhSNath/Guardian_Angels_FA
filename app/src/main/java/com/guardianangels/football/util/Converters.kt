package com.guardianangels.football.util

import com.guardianangels.football.data.PlayerType

class Converters {
    companion object {

        /**
         * Get the number from a String without NumberFormatException.
         */
        fun String.toEmptySafeInt(): Int = if (this == "") 0 else this.toInt()
        fun String.toEmptySafeFloat(): Float = if (this == "") 0f else this.toFloat()

        fun PlayerType.getPlayerTypeString(singular: Boolean = false): String {
            return when (this) {
                PlayerType.GOAL_KEEPER -> if (!singular) "${Constants.GOAL_KEEPER}s" else Constants.GOAL_KEEPER
                PlayerType.DEFENDER -> if (!singular) "${Constants.DEFENDER}s" else Constants.DEFENDER
                PlayerType.MIDFIELDER -> if (!singular) "${Constants.MIDFIELDER}s" else Constants.MIDFIELDER
                PlayerType.FORWARD -> if (!singular) "${Constants.FORWARD}s" else Constants.FORWARD
                PlayerType.COACH -> if (!singular) Constants.COACH else Constants.COACH
            }
        }

        fun String.getPlayerType(): PlayerType {
            return when (this) {
                "Goal Keeper" -> PlayerType.GOAL_KEEPER
                "Defender" -> PlayerType.DEFENDER
                "Midfielder" -> PlayerType.MIDFIELDER
                "Forward" -> PlayerType.FORWARD
                else -> PlayerType.COACH
            }
        }
    }
}