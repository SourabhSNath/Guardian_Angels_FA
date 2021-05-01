package com.guardianangels.football.ui.match.previousmatches

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.guardianangels.football.R
import com.guardianangels.football.data.GameResults
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.PreviousMatchResultItemBinding
import com.guardianangels.football.ui.match.MatchDiffItem
import com.guardianangels.football.util.setCardStroke

private typealias ClickListener = (Match) -> Unit

class PreviousMatchesAdapter(val clickListener: ClickListener) : ListAdapter<Match, PreviousMatchesAdapter.ViewHolder>(MatchDiffItem) {

    inner class ViewHolder(private val binding: PreviousMatchResultItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Match) {

            val context = binding.root.context

            binding.team1.text = item.team1Name
            binding.team2.text = item.team2Name

            if (item.team1Logo!!.isNotEmpty()) {
                binding.team1Logo.load(item.team1Logo)
            } else {
                if (item.team1Name == context.getString(R.string.guardian_angels))
                    binding.team1Logo.load(R.drawable.gaurdian_angels)
                else
                    binding.team1Logo.load(R.drawable.ic_football)
            }

            if (item.team2Logo!!.isNotEmpty()) {
                binding.team2Logo.load(item.team2Logo)
            } else {
                if (item.team2Name == context.getString(R.string.guardian_angels))
                    binding.team2Logo.load(R.drawable.gaurdian_angels)
                else
                    binding.team2Logo.load(R.drawable.ic_football)
            }

            binding.team1Score.text = item.team1Score!!.toString()
            binding.team2Score.text = item.team2Score!!.toString()

            val cardView = binding.card
            when (item.gameResult) {
                GameResults.WIN -> cardView.setCardStroke(context, R.color.mainStatWon)
                GameResults.LOSS -> cardView.setCardStroke(context, R.color.mainStatLost)
                GameResults.DRAW -> cardView.setCardStroke(context, R.color.mainStatDraw)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PreviousMatchResultItemBinding.inflate(layoutInflater, parent, false)
        val viewHolder = ViewHolder(binding)

        binding.root.setOnClickListener {
            val adapterPos = viewHolder.adapterPosition
            if (adapterPos != RecyclerView.NO_POSITION) {
                clickListener(getItem(adapterPos))
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}