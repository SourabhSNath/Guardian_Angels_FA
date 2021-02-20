package com.gaurdianangels.football.ui.players.details

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.data.PlayerType
import com.gaurdianangels.football.databinding.PlayerDetailsFragmentBinding
import com.gaurdianangels.football.util.Converters.Companion.getPlayerTypeString

class PlayerDetailsFragment : Fragment(R.layout.player_details_fragment) {

    companion object {
        private const val TAG = "PlayerDetailsFragment"
    }

    /**
     * Can receive data from PlayerListFragment and AddPlayerFragment.
     * 1. From PlayerListFragment when opening a player item
     * 2. From AddPlayerFragment when updating a player item
     */
    private val args: PlayerDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = PlayerDetailsFragmentBinding.bind(view)

        val playerModel: Player = args.playerModel

        Log.d(TAG, "onViewCreated: ${playerModel.id}")

        val categorySpecificTitle = binding.categorySpecifcTitle
        val categorySpecificScore = binding.totalCategorySpecificScore

        binding.editButton.setOnClickListener {
            findNavController().navigate(PlayerDetailsFragmentDirections.actionPlayerDetailsFragmentToAddPlayerFragment(playerModel))
        }

        binding.playerImage.load(playerModel.remoteUri)
        binding.playerTypeTV.text = playerModel.playerType!!.getPlayerTypeString(true)
        binding.playerNameTV.text = playerModel.playerName

        binding.totalGamesTV.text = playerModel.totalGames?.toString()

        val saves = playerModel.totalSaves
        val wins = playerModel.totalWins

        when (playerModel.playerType) {
            PlayerType.GOAL_KEEPER -> {
                categorySpecificTitle.text = "Saves"
                categorySpecificScore.text = saves.toString()

                binding.cleanSheetCard.visibility = View.VISIBLE
                binding.totalCleanSheets.text = playerModel.totalCleanSheets.toString()
            }

            PlayerType.COACH -> {
                categorySpecificTitle.text = "Wins"
                categorySpecificScore.text = wins.toString()
            }

            else -> {
                categorySpecificTitle.text = "Goals"
                categorySpecificScore.text = playerModel.totalGoals.toString()
            }
        }

        binding.ageTV.text = playerModel.playerAge.toString()
        binding.heightTV.text = playerModel.playerHeight.toString()
        binding.weightTV.text = playerModel.playerWeight.toString()


        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

    }
}