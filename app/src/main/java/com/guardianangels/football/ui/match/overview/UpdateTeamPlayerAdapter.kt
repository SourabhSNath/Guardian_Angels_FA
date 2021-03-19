package com.guardianangels.football.ui.match.overview

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.guardianangels.football.data.Player
import com.guardianangels.football.data.PlayerType
import com.guardianangels.football.databinding.UpdateTeamPlayerItemBinding
import com.guardianangels.football.util.getPlayerTypeString
import com.guardianangels.football.util.toEmptySafeInt
import timber.log.Timber


class UpdateTeamPlayerAdapter : RecyclerView.Adapter<UpdateTeamPlayerAdapter.ViewHolder>() {

    var playerList = emptyList<Player>()
        private set

    fun submitList(players: List<Player>) {
        playerList = players
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = UpdateTeamPlayerItemBinding.inflate(layoutInflater, parent, false)

        /**
         * Set TextWatcher here for better performance.
         */
        return ViewHolder(binding, CustomTextWatcher())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adapterPosition = holder.adapterPosition
        if (adapterPosition != RecyclerView.NO_POSITION) {
            holder.bindData(playerList[adapterPosition], adapterPosition)
        }
    }

    inner class ViewHolder(
        private val binding: UpdateTeamPlayerItemBinding,
        private val customTextWatcher: CustomTextWatcher
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(player: Player, adapterPosition: Int) {
            if (player.playerType != PlayerType.COACH) {
                customTextWatcher.update(adapterPosition, player.playerType!!, player.totalSaves!!, player.totalGoals!!)
            }

            binding.playerTypeTV.text = player.playerType!!.getPlayerTypeString(true)
            binding.playerImage.load(player.remoteUri)
            binding.playerName.text = player.playerName

            when (player.playerType) {
                PlayerType.GOAL_KEEPER -> {
                    binding.categorySpecificTF.hint = "Saves"
                    binding.cleanSheetCheckBox.apply {
                        visibility = View.VISIBLE
                        check(adapterPosition)
                    }
                }
                PlayerType.COACH -> {
                    binding.categorySpecificET.visibility = View.GONE
                    binding.cleanSheetCheckBox.visibility = View.GONE
                }
                else -> {
                    binding.categorySpecificTF.hint = "Goals"
                    binding.cleanSheetCheckBox.visibility = View.GONE
                }
            }
        }

        private fun CheckBox.check(adapterPosition: Int) {
            val originalCleanSheet = playerList[adapterPosition].totalCleanSheets
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    playerList[adapterPosition].totalCleanSheets = originalCleanSheet!!.inc()
                    Timber.d("${playerList[adapterPosition].playerName}: Original: $originalCleanSheet, New: ${playerList[adapterPosition].totalCleanSheets}")
                } else {
                    playerList[adapterPosition].totalCleanSheets = originalCleanSheet
                    Timber.d("Revert to original clean sheet value")
                }
            }
        }

        fun enableTextWatcher() {
            Timber.d("Add textChangeListener")
            binding.categorySpecificET.addTextChangedListener(customTextWatcher)
        }

        fun disableTextWatcher() {
            Timber.d("Remove textChangeListener")
            binding.categorySpecificET.removeTextChangedListener(customTextWatcher)
        }
    }

    /**
     * Enable and remove the textWatcher when the view is attached or detached.
     */
    override fun onViewAttachedToWindow(holder: ViewHolder) {
        holder.enableTextWatcher()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.disableTextWatcher()
    }


    inner class CustomTextWatcher : TextWatcher {

        private var pos = 0
        private var type: PlayerType? = null
        var originalTotalSaves = 0
        var originalGoals = 0
        fun update(adapterPosition: Int, type: PlayerType, totalSaves: Int, totalGoals: Int) {
            pos = adapterPosition
            this.type = type
            originalTotalSaves = totalSaves.also {
                Timber.d("Original ${playerList[pos].playerName} $it")
            }
            originalGoals = totalGoals.also {
                Timber.d("Original ${playerList[pos].playerName} $it")
            }
        }

        override fun afterTextChanged(s: Editable?) {
            val value = s.toString().toEmptySafeInt()

            type?.let {
                when (it) {
                    PlayerType.GOAL_KEEPER -> {
                        Timber.d("${playerList[pos].playerName}: $originalTotalSaves + $value = ${originalTotalSaves + value}")
                        playerList[pos].totalSaves = originalTotalSaves.plus(value)
                    }
                    else -> {
                        Timber.d("${playerList[pos].playerName}: $originalGoals + $value = ${originalGoals + value}")
                        playerList[pos].totalGoals = originalGoals.plus(value)
                    }
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    }


    override fun getItemCount(): Int = playerList.size
    override fun getItemViewType(position: Int): Int = position

}