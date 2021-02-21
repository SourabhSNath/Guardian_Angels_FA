package com.gaurdianangels.football.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.databinding.PlayerListFragmentBinding
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.ui.players.adapter.SectionedPlayerListAdapter
import com.gaurdianangels.football.ui.players.playerlist.PlayerListViewModel

abstract class BasePlayerListFragment : Fragment(R.layout.player_list_fragment) {

    protected val viewModel: PlayerListViewModel by viewModels()
    private var _binding: PlayerListFragmentBinding? = null
    protected val binding get() = _binding!!

    private companion object {
        private const val TAG = "BasePlayerListFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listenForFragmentResult()

        _binding = PlayerListFragmentBinding.bind(view)


        val sectionAdapter = SectionedPlayerListAdapter(
            viewLifecycleOwner,
            viewModel.multiSelectionHandler.selectedPlayers
        ) {
            playerItemClickListener(it)
        }

        binding.playersListRecyclerView.apply {
            this.adapter = sectionAdapter
            val gridLayoutManager = GridLayoutManager(requireContext(), 2)

            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // If header make it full size. Else split in 2
                    return if (sectionAdapter.getItemViewType(position) == -2)
                        1
                    else
                        gridLayoutManager.spanCount
                }
            }

            layoutManager = gridLayoutManager
        }

        buttonClickListeners(sectionAdapter)
        observeViewModel(sectionAdapter)
    }


    /**
     * Method to set button click listeners.
     * Can be overridden to add fragment specific functionality.
     */
    protected open fun buttonClickListeners(adapter: SectionedPlayerListAdapter) {

        binding.cancelSelectionButton.setOnClickListener {
            viewModel.multiSelectionHandler.setToolbarState(ToolbarState.NormalState)
            viewModel.multiSelectionHandler.clearSelectedList()
            adapter.notifyDataSetChanged()
            resetTitle()
        }
    }


    /**
     * Method to observe values from viewModel.
     * Can be overridden to add fragment specific functionality.
     */
    @SuppressLint("SetTextI18n")
    protected open fun observeViewModel(adapter: SectionedPlayerListAdapter) {
        /**
         * Get the player list
         */
        viewModel.sectionedPlayerResultLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Log.d(TAG, "observeViewModel: Loading")
                is NetworkState.Success -> {
                    adapter.submitList(it.data)
                }
                is NetworkState.Failed -> {
                    Log.d(TAG, "onViewCreated: ${it.message}")
                }
            }
        }

        /**
         * Handle the toolbar state
         */
        viewModel.multiSelectionHandler.toolbarState.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    ToolbarState.NormalState -> {
                        setNormalState()
                    }

                    ToolbarState.MultiSelectState -> {
                        selectedState()
                    }
                }
            }
        }

        /**
         * Set the count in the toolbar
         */
        viewModel.multiSelectionHandler.selectedPlayers.observe(viewLifecycleOwner) {
            Log.d(TAG, "Selected Players: ${it.size}")
            if (it.size > 0) {
                binding.playersTV.text = "${it.size} Selected"
            } else {
                resetTitle()
            }
        }
    }

    /**
     * For fragment result api.
     * To be only used when player is added, updated or deleted.
     */
    protected abstract fun listenForFragmentResult()

    protected abstract fun playerItemClickListener(player: Player)

    protected abstract fun selectedState()

    protected abstract fun setNormalState()

    protected fun resetTitle() {
        binding.playersTV.text = resources.getText(R.string.players)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}