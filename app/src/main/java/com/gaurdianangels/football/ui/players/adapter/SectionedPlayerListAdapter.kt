package com.gaurdianangels.football.ui.players.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.data.SectionedPlayerRecyclerItem
import com.gaurdianangels.football.databinding.HeaderItemBinding
import com.gaurdianangels.football.databinding.PlayerListImageItemBinding
import java.lang.ClassCastException

private const val ITEM_HEADER = -1
private const val ITEM_PLAYER = -2

class SectionedPlayerListAdapter(private val playerClickListener: (Player) -> Unit) :
    ListAdapter<SectionedPlayerRecyclerItem, RecyclerView.ViewHolder>(DiffItem) {

    companion object DiffItem : DiffUtil.ItemCallback<SectionedPlayerRecyclerItem>() {
        override fun areItemsTheSame(oldItem: SectionedPlayerRecyclerItem, newItem: SectionedPlayerRecyclerItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SectionedPlayerRecyclerItem, newItem: SectionedPlayerRecyclerItem): Boolean {
            return oldItem == newItem
        }
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
            ITEM_PLAYER -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PlayerListImageItemBinding.inflate(inflater, parent, false)
                val playerViewHolder = PlayerViewHolder(binding)
                binding.root.setOnClickListener {
                    val pos = playerViewHolder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val playerItem = getItem(pos) as SectionedPlayerRecyclerItem.PlayerItem
                        playerClickListener(playerItem.player)
                    }
                }
                return playerViewHolder
            }
            else -> throw ClassCastException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as SectionedPlayerRecyclerItem.PlayerTypeItem)

            is PlayerViewHolder -> holder.bind(getItem(position) as SectionedPlayerRecyclerItem.PlayerItem)
        }
    }


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

    class PlayerViewHolder(private val binding: PlayerListImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playerItem: SectionedPlayerRecyclerItem.PlayerItem) {
            val player = playerItem.player
            binding.playerImage.load(player.remoteUri)
            binding.playerName.text = player.playerName
            binding.playerNumber.text = player.playerNumber
        }
    }

}