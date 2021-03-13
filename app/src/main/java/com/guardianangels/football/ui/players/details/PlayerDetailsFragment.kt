package com.guardianangels.football.ui.players.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.guardianangels.football.R
import com.guardianangels.football.data.Player
import com.guardianangels.football.data.PlayerType
import com.guardianangels.football.databinding.PlayerDetailsFragmentBinding
import com.guardianangels.football.util.getPlayerTypeString
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PlayerDetailsFragment : Fragment(R.layout.player_details_fragment) {

    companion object {
        private const val TAG = "PlayerDetailsFragment"
    }

    @Inject
    lateinit var auth: FirebaseAuth

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

        Timber.tag(TAG).d("onViewCreated: ${playerModel.id}")

        val categorySpecificTitle = binding.categorySpecifcTitle
        val categorySpecificScore = binding.totalCategorySpecificScore

        if (auth.currentUser != null) {
            binding.editButton.setOnClickListener {
                findNavController().navigate(PlayerDetailsFragmentDirections.actionPlayerDetailsFragmentToAddPlayerFragment(playerModel))
            }
        } else {
            binding.editButton.visibility = View.GONE
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