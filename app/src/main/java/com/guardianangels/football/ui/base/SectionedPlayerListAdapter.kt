package com.guardianangels.football.ui.base

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.guardianangels.football.data.Player
import com.guardianangels.football.data.SectionedPlayerRecyclerItem
import com.guardianangels.football.databinding.HeaderItemBinding
import com.guardianangels.football.databinding.PlayerListImageItemBinding

class SectionedPlayerListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val selectedPlayers: LiveData<ArrayList<Player>>,
    private val playerClickListener: (Player) -> Unit
) :
    ListAdapter<SectionedPlayerRecyclerItem, RecyclerView.ViewHolder>(DiffItem) {

    companion object {
        private const val ITEM_HEADER = -1
        private const val ITEM_PLAYER = -2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SectionedPlayerRecyclerItem.PlayerTypeItem -> ITEM_HEADER
            is SectionedPlayerRecyclerItem.PlayerItem -> ITEM_PLAYER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_HEADER -> HeaderViewHolder.from(parent)
            ITEM_PLAYER -> PlayerViewHolder.from(parent, this)
            else -> throw ClassCastException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as SectionedPlayerRecyclerItem.PlayerTypeItem)
            is PlayerViewHolder -> holder.bind(getItem(position) as SectionedPlayerRecyclerItem.PlayerItem, this)
        }
    }

    /**
     * ViewHolder for the Player Item
     */
    class PlayerViewHolder(private val binding: PlayerListImageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup, adapter: SectionedPlayerListAdapter): PlayerViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PlayerListImageItemBinding.inflate(inflater, parent, false)
                val playerViewHolder = PlayerViewHolder(binding)

                // Doing this in on onCreate for better performance
                binding.root.setOnClickListener {
                    val pos = playerViewHolder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val playerItem = adapter.getItem(pos) as SectionedPlayerRecyclerItem.PlayerItem
                        adapter.playerClickListener(playerItem.player)
                    }
                }
                return playerViewHolder
            }
        }

        fun bind(playerItem: SectionedPlayerRecyclerItem.PlayerItem, adapter: SectionedPlayerListAdapter) {
            val player = playerItem.player
            binding.playerImage.load(player.remoteUri)
            binding.playerName.text = player.playerName
            binding.playerNumber.text = player.playerNumber
            val materialCardView = binding.playerItem

            // Show a border around the cardView when the item gets added to the list.
            adapter.selectedPlayers.observe(adapter.viewLifecycleOwner) { playerList ->
                if (playerList.contains(player)) {
                    Log.d("SectionedPlayerAdapter", "bind: ${player.playerName}")
                    materialCardView.apply {
                        strokeWidth = 8
                    }
                } else {
                    materialCardView.strokeWidth = 0
                }
            }

        }
    }

    /**
     * ViewHolder for the Section Header
     */
    class HeaderViewHolder(private val binding: HeaderItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HeaderItemBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }

        fun bind(playerTypeItem: SectionedPlayerRecyclerItem.PlayerTypeItem) {
            binding.headerTV.text = playerTypeItem.playerType
        }
    }


    object DiffItem : DiffUtil.ItemCallback<SectionedPlayerRecyclerItem>() {
        override fun areItemsTheSame(oldItem: SectionedPlayerRecyclerItem, newItem: SectionedPlayerRecyclerItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SectionedPlayerRecyclerItem, newItem: SectionedPlayerRecyclerItem): Boolean {
            return oldItem == newItem
        }
    }
}

