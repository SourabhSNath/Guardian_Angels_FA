package com.guardianangels.football.ui.match.upcomingmatches

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.guardianangels.football.R
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.MatchListItemBinding
import com.guardianangels.football.ui.match.MatchDiffItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UpcomingMatchListAdapter(val clickListener: (Match) -> Unit) : ListAdapter<Match, UpcomingMatchListAdapter.ViewHolder>(MatchDiffItem) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: MatchListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup, adapter: UpcomingMatchListAdapter): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MatchListItemBinding.inflate(layoutInflater, parent, false)
                val viewHolder = ViewHolder(binding)

                binding.root.setOnClickListener {
                    val pos = viewHolder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val match = adapter.getItem(pos) as Match
                        adapter.clickListener(match)
                    }
                }

                return viewHolder
            }
        }

        fun bind(item: Match) {
            if (item.team1Logo!!.isNotEmpty()) {
                binding.team1Logo.load(item.team1Logo)
            } else {
                binding.team1Logo.load(R.drawable.gaurdian_angels)
            }
            binding.team2Logo.load(item.team2Logo)
            binding.team1TV.text = item.team1Name
            binding.team2TV.text = item.team2Name


            val zonedDateTime = Instant.ofEpochSecond(item.dateAndTime!!).atZone(ZoneId.systemDefault())
            val date = zonedDateTime.toLocalDate()
            val formattedDate = date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"))

            val time = zonedDateTime.toLocalTime()
            val formattedTime = time.format(DateTimeFormatter.ofPattern("hh:mm a"))

            binding.dateTV.text = formattedDate
            binding.timeTV.text = formattedTime
        }
    }
}