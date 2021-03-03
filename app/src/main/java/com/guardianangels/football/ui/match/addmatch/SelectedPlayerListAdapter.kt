package com.guardianangels.football.ui.match.addmatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.guardianangels.football.data.Player
import com.guardianangels.football.databinding.AddUpcomingMatchSelectedTeamListItemBinding

class SelectedPlayerListAdapter : ListAdapter<Player, SelectedPlayerListAdapter.ViewHolder>(DiffItem) {

    private object DiffItem : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: AddUpcomingMatchSelectedTeamListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Player?) {
            binding.playerName.text = item?.playerName
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddUpcomingMatchSelectedTeamListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

}