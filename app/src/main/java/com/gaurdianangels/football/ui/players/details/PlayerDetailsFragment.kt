package com.gaurdianangels.football.ui.players.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import com.gaurdianangels.football.R
import com.gaurdianangels.football.databinding.PlayerDetailsFragmentBinding
import com.gaurdianangels.football.util.Converters.Companion.getPlayerTypeString

class PlayerDetailsFragment : Fragment(R.layout.player_details_fragment) {

    private val args: PlayerDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = PlayerDetailsFragmentBinding.bind(view)

        val playerModel = args.playerModel

        binding.playerImage.load(playerModel.remoteUri)
        binding.playerTypeTV.text = playerModel.playerType!!.getPlayerTypeString()
        binding.playerNameTV.text = playerModel.playerName
//        binding.playerNumber.text = playerModel.playerNumber
    }
}