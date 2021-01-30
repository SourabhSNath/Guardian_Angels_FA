package com.gaurdianangels.football.ui.players

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gaurdianangels.football.R
import com.gaurdianangels.football.databinding.PlayerListFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerListFragment : Fragment(R.layout.player_list_fragment) {

    private val viewModel: PlayerListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = PlayerListFragmentBinding.bind(view)

        val addPlayerButton = binding.addPlayerButton

        viewModel.checkLogin.observe(viewLifecycleOwner) {
            if (it) {
                addPlayerButton.visibility = View.VISIBLE
            } else {
                addPlayerButton.visibility = View.GONE
            }
        }


        addPlayerButton.setOnClickListener {
            findNavController().navigate(PlayerListFragmentDirections.actionPlayersToAddPlayerFragment())
        }
    }

}