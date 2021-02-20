package com.gaurdianangels.football.ui.players.playerlist

import android.annotation.SuppressLint
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.databinding.PlayerListFragmentBinding
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.ui.ToolbarState
import com.gaurdianangels.football.ui.players.adapter.SectionedPlayerListAdapter
import com.gaurdianangels.football.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerListFragment : Fragment(R.layout.player_list_fragment) {

    companion object {
        private const val TAG = "PlayerListFragment"
    }

    private val viewModel: PlayerListViewModel by viewModels()

    private var _binding: PlayerListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(Constants.REQUEST_PLAYER_UPLOAD_COMPLETE_KEY) { _, bundle ->
            val result = bundle.getBoolean(Constants.BUNDLE_PLAYER_UPLOAD_COMPLETE)
            if (result) {
                viewModel.getSectionedPlayerResultLiveData()
            }
        }
        setFragmentResultListener(Constants.REQUEST_PLAYER_UPDATE_COMPLETE_KEY) { _, bundle ->
            val result = bundle.getBoolean(Constants.BUNDLE_PLAYER_UPDATE_COMPLETE)
            if (result) {
                viewModel.getSectionedPlayerResultLiveData()
            }
        }

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
     * Get player from adapter item click
     */
    private fun playerItemClickListener(player: Player) {
        Log.d(TAG, "playerItemClickListener: Clicked. ${player.playerName}")
        if (viewModel.multiSelectionHandler.isMultiSelectionStateActive()) {
            viewModel.multiSelectionHandler.addOrRemovePlayersFromSelectedList(player)
            Log.d(TAG, "playerItemClickListener: Selected")
        } else {
            Log.d(TAG, "playerItemClickListener: Moved to another Fragment")
            findNavController().navigate(PlayerListFragmentDirections.actionPlayersToPlayerDetailsFragment(player))
        }

    }

    /**
     * Setup button click listeners
     */
    private fun buttonClickListeners(adapter: SectionedPlayerListAdapter) {

        binding.optionsButton.setOnClickListener {
            showPopUpMenu(adapter)
        }

        binding.confirmButton.setOnClickListener {
            deletePlayersWithConfirmation()
        }

        binding.cancelSelectionButton.setOnClickListener {
            viewModel.multiSelectionHandler.setToolbarState(ToolbarState.NormalState)
            viewModel.multiSelectionHandler.clearSelectedList()
            adapter.notifyDataSetChanged()
            resetTitle()
        }
    }

    private fun deletePlayersWithConfirmation() {
        viewModel.multiSelectionHandler.deletePlayers()
    }


    /**
     * Observe viewModel
     */
    @SuppressLint("SetTextI18n")
    private fun observeViewModel(adapter: SectionedPlayerListAdapter) {

        val optionsButton = binding.optionsButton
        viewModel.checkLogin.observe(viewLifecycleOwner) {
            if (it) {
                optionsButton.visibility = View.VISIBLE
            } else {
                optionsButton.visibility = View.GONE
            }
        }

        viewModel.sectionedPlayerResultLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Log.d(TAG, "observeViewModel: Loading")
                is NetworkState.Success -> {
                    adapter.submitList(it.data)
                    binding.playersListRecyclerView.smoothScrollToPosition(0)
                }
                is NetworkState.Failed -> {
                    Log.d(TAG, "onViewCreated: ${it.message}")
                }
            }
        }

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

        viewModel.multiSelectionHandler.selectedPlayers.observe(viewLifecycleOwner) {
            Log.d(TAG, "Selected Players: ${it.size}")
            if (it.size > 0) {
                binding.playersTV.text = "${it.size} Selected"
            } else {
                resetTitle()
            }
        }

        viewModel.multiSelectionHandler.deletedState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> {
                }
                is NetworkState.Success -> {
                    Log.d(TAG, "observeViewModel: Success ${it.data}")
                    viewModel.multiSelectionHandler.clearSelectedList()
                    viewModel.multiSelectionHandler.setToolbarState(ToolbarState.NormalState)
                    resetTitle()
                    viewModel.getSectionedPlayerResultLiveData()
                }
                is NetworkState.Failed -> {
                    Log.d(TAG, "observeViewModel: Failed: ${it.message}")
                }
            }
        }
    }

    private fun resetTitle() {
        binding.playersTV.text = resources.getText(R.string.players)
    }

    private fun setNormalState() {
        binding.optionsButton.visibility = View.VISIBLE
        modeViewVisibility(View.GONE)
    }

    private fun selectedState() {
        binding.optionsButton.visibility = View.GONE
        modeViewVisibility(View.VISIBLE)
    }

    private fun modeViewVisibility(visibility: Int) {
        binding.confirmButton.visibility = visibility
        binding.cancelSelectionButton.visibility = visibility
        binding.confirmButton.visibility = visibility
    }


    @SuppressLint("RestrictedApi") // Required for setOptionalIconsVisible
    private fun showPopUpMenu(adapter: SectionedPlayerListAdapter) {
        val popupMenu = PopupMenu(requireContext(), binding.optionsButton)
        popupMenu.menuInflater.inflate(R.menu.players_popup_menu, popupMenu.menu)

        if (popupMenu.menu is MenuBuilder) {
            val menuBuilder = popupMenu.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)

            for (item in menuBuilder.visibleItems) {
                val iconMarginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, resources.displayMetrics).toInt()
                if (item.icon != null) {
                    item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                }
            }
        }
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addPlayers -> {
                    findNavController().navigate(PlayerListFragmentDirections.actionPlayersToAddPlayerFragment())
                    true
                }

                R.id.deletePlayers -> {
                    viewModel.multiSelectionHandler.setToolbarState(ToolbarState.MultiSelectState)
                    adapter.notifyDataSetChanged()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}