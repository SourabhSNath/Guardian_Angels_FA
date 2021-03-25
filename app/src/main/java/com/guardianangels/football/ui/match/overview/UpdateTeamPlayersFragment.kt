package com.guardianangels.football.ui.match.overview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.guardianangels.football.R
import com.guardianangels.football.databinding.UpdateTeamPlayersFragmentBinding
import com.guardianangels.football.network.NetworkState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class UpdateTeamPlayersFragment : Fragment(R.layout.update_team_players_fragment) {

    private val args: UpdateTeamPlayersFragmentArgs by navArgs()
    private val viewModel: UpdateTeamPlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playerIDs = args.ids
        val isWin = args.isWin
        viewModel.getPlayersFromIds(playerIDs.toList())
        val binding = UpdateTeamPlayersFragmentBinding.bind(view)

        val adapter = UpdateTeamPlayerAdapter()
        binding.playerRecyclerView.adapter = adapter

        viewModel.playersLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    lifecycleScope.launch {
                        /* Delay to wait for fragment transition animation, prevents lag.*/
                        delay(200)
                        adapter.submitList(it.data)
                    }
                }
                is NetworkState.Failed -> Timber.d("Failed, ${it.exception}, ${it.message}")
            }
        }

        binding.updateButton.setOnClickListener {
            viewModel.updatePlayers(adapter.playerList, isWin)
        }

        viewModel.playersUpdated.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    findNavController().navigate(UpdateTeamPlayersFragmentDirections.actionUpdateTeamPlayersFragmentToHome(true))
                }
                is NetworkState.Failed -> Timber.d("$it, ${it.message}")
            }
        }
    }
}