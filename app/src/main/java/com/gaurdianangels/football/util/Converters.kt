package com.gaurdianangels.football.util

import com.gaurdianangels.football.data.PlayerType

class Converters {
    companion object {
        fun PlayerType.getPlayerTypeString(): String {
            return when (this) {
                PlayerType.GOAL_KEEPER -> "${Constants.GOAL_KEEPER}s"
                PlayerType.DEFENDER -> "${Constants.DEFENDER}s"
                PlayerType.MIDFIELDER -> "${Constants.MIDFIELDER}s"
                PlayerType.FORWARD -> "${Constants.FORWARD}s"
                PlayerType.COACH -> Constants.COACH
            }
        }

        fun getPlayerType(typeString: String): PlayerType {
            return when (typeString) {
                "Goal Keeper" -> PlayerType.GOAL_KEEPER
                "Defender" -> PlayerType.DEFENDER
                "Midfielder" -> PlayerType.MIDFIELDER
                "Forward" -> PlayerType.FORWARD
                else -> PlayerType.COACH
            }
        }
    }
}