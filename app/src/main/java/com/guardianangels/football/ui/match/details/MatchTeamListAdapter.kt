package com.guardianangels.football.ui.match.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.guardianangels.football.data.Player
import com.guardianangels.football.databinding.MatchSelectedPlayersBinding

class MatchTeamListAdapter : ListAdapter<Player, MatchTeamListAdapter.ViewHolder>(DiffItem) {

    companion object DiffItem : DiffUtil.ItemCallback<Player>() {

        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(private val binding: MatchSelectedPlayersBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Player) {
            binding.playerImage.load(item.remoteUri)
            binding.playerName.text = item.playerName
            binding.playerNumber.text = item.playerNumber
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MatchSelectedPlayersBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}