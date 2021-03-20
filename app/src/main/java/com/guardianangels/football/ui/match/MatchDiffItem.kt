package com.guardianangels.football.ui.match

import androidx.recyclerview.widget.DiffUtil
import com.guardianangels.football.data.Match

/**
 * Common DiffUtil.ItemCallback for RecyclerView Adapters that display Matches.
 */
object MatchDiffItem : DiffUtil.ItemCallback<Match>() {
    override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean {
        return oldItem.matchID == newItem.matchID
    }

    override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean {
        return oldItem == newItem
    }
}