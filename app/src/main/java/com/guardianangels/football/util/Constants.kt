package com.guardianangels.football.util

object Constants {
    const val GOAL_KEEPER = "Goal Keeper"
    const val DEFENDER = "Defender"
    const val FORWARD = "Forward"
    const val MIDFIELDER = "Midfielder"
    const val COACH = "Coaching Staff"


    /**
     * Constants for Passing data between fragments
     */
    const val REQUEST_PLAYER_UPLOAD_COMPLETE_KEY = "playerUploadedCheckKey"
    const val BUNDLE_PLAYER_UPLOAD_COMPLETE = "bundlePlayerUpload"

    const val REQUEST_MATCH_UPLOAD_COMPLETE_KEY = "matchUploadedCheckKey"
    const val BUNDLE_MATCH_UPLOAD_COMPLETE = "bundleMatchUpload"

    const val PLAYER_SELECTED_KEY = "playerSelectedKey"
    const val MATCH_UPDATED_RESULT_KEY = "matchUpdatedKey"
    const val MATCH_DELETED_RESULT_KEY = "matchDeletedKey"

    /*** For home fragment ***/
    const val RELOAD_NEXT_UPCOMING_KEY = "reloadNextUpcomingWhenChangedKey"
    const val RELOAD_GAME_STATS_KEY = "reloadStatsWhenChangedKey"
    const val RELOAD_PREVIOUS_MATCHES_KEY = "reloadPreviousMatchesWhenChangeKey"
}