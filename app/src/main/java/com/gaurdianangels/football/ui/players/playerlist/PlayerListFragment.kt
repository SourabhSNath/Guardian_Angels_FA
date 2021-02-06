package com.gaurdianangels.football.ui.players.playerlist

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.databinding.PlayerListFragmentBinding
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.ui.players.adapter.SectionedPlayerListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerListFragment : Fragment(R.layout.player_list_fragment) {

    private val TAG = "PlayerListFragment"
    private val viewModel: PlayerListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = PlayerListFragmentBinding.bind(view)
        val addPlayerButton = binding.addPlayerButton

        addPlayerButton.setOnClickListener {
            findNavController().navigate(PlayerListFragmentDirections.actionPlayersToAddPlayerFragment())
        }

        val sectionAdapter = SectionedPlayerListAdapter { playerItemClickListener(it) }
        binding.playersListRecyclerView.apply {
            this.adapter = sectionAdapter

            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (sectionAdapter.getItemViewType(position) == -2) 1 else gridLayoutManager.spanCount
                }

            }

            layoutManager = gridLayoutManager
        }

        observeViewModel(addPlayerButton, sectionAdapter)
    }

    private fun playerItemClickListener(player: Player) {
        findNavController().navigate(PlayerListFragmentDirections.actionPlayersToPlayerDetailsFragment(player))
    }


    private fun observeViewModel(addPlayerButton: Button, adapter: SectionedPlayerListAdapter) {

        viewModel.checkLogin.observe(viewLifecycleOwner) {
            if (it) {
                addPlayerButton.visibility = View.VISIBLE
            } else {
                addPlayerButton.visibility = View.GONE
            }
        }

        viewModel.sectionedPlayerResultLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Log.d(TAG, "onViewCreated: LoADing")
                is NetworkState.Success -> {
                    adapter.submitList(it.data)
                }
                is NetworkState.Failed -> {
                    Log.d(TAG, "onViewCreated: ${it.message}")
                }
            }
        }
    }

}