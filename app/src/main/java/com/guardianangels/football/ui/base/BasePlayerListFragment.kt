package com.guardianangels.football.ui.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.guardianangels.football.R
import com.guardianangels.football.data.Player
import com.guardianangels.football.databinding.PlayerListFragmentBinding
import com.guardianangels.football.network.NetworkState
import timber.log.Timber

abstract class BasePlayerListFragment : Fragment(R.layout.player_list_fragment) {

    protected val viewModel: BasePlayerListViewModel by viewModels()
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
            cancelSelection(adapter)
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
                is NetworkState.Loading -> Timber.tag(TAG).d("observeViewModel: Loading")
                is NetworkState.Success -> {
                    adapter.submitList(it.data)
                }
                is NetworkState.Failed -> {
                    Timber.tag(TAG).d("onViewCreated: ${it.message}")
                }
            }
        }

        /**
         * Handle the toolbar state.
         *
         * Callback is used to control the back press clicks when the playerItems are selected.
         */

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { cancelSelection(adapter) }

        viewModel.multiSelectionHandler.toolbarState.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    ToolbarState.NormalState -> {
                        setNormalStateLayout()
                        callback.isEnabled = false
                    }

                    ToolbarState.MultiSelectState -> {
                        setSelectedStateLayout()
                        callback.isEnabled = true
                    }
                }
            }
        }

        /**
         * Set the count in the toolbar
         */
        viewModel.multiSelectionHandler.selectedPlayers.observe(viewLifecycleOwner) {
            if (it.size > 0) {
                Timber.tag(TAG).d("Selected Players: ${it.size}")
                binding.playersTV.text = "${it.size} Selected"
                setPlayersList(it)
            } else {
                resetTitle()
            }
        }
    }

    // Open so that it's implemented only fragments that requires to override it
    protected open fun setPlayersList(selectedPlayers: List<Player>) {}

    /**
     * Used to cancel the selection.
     */
    private fun cancelSelection(adapter: SectionedPlayerListAdapter) {
        viewModel.multiSelectionHandler.setToolbarState(ToolbarState.NormalState)
        viewModel.multiSelectionHandler.clearSelectedList()
        adapter.notifyDataSetChanged()
        resetTitle()
    }

    /**
     * For fragment result api.
     * To be only used when player is added, updated or deleted.
     */
    protected abstract fun listenForFragmentResult()

    protected abstract fun playerItemClickListener(player: Player)

    protected abstract fun setSelectedStateLayout()

    protected abstract fun setNormalStateLayout()

    protected fun resetTitle() {
        binding.playersTV.text = resources.getText(R.string.team)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}