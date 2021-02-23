package com.guardianangels.football.data

/**
 * Used to separate the List of Player data into a Sectioned List
 */
sealed class SectionedPlayerRecyclerItem {
    data class PlayerTypeItem(val playerType: String) : SectionedPlayerRecyclerItem() {
        override val id: String = "1"
    }

    data class PlayerItem(val player: Player) : SectionedPlayerRecyclerItem() {
        override val id: String = player.id!!
    }

    abstract val id: String
}