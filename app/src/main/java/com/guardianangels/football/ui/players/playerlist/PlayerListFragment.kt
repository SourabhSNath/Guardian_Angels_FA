package com.guardianangels.football.ui.players.playerlist

import android.annotation.SuppressLint
import android.graphics.drawable.InsetDrawable
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.guardianangels.football.R
import com.guardianangels.football.data.Player
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.ui.base.ToolbarState
import com.guardianangels.football.ui.base.BasePlayerListFragment
import com.guardianangels.football.ui.base.SectionedPlayerListAdapter
import com.guardianangels.football.util.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerListFragment : BasePlayerListFragment() {

    companion object {
        const val TAG = "PlayerListFragment"
    }

    override fun listenForFragmentResult() {
        setFragmentResultListener(Constants.REQUEST_PLAYER_UPLOAD_COMPLETE_KEY) { _, bundle ->
            val result = bundle.getBoolean(Constants.BUNDLE_PLAYER_UPLOAD_COMPLETE)
            if (result) {
                viewModel.getSectionedPlayerResultLiveData()
            }
        }
    }

    /**
     * To handle recyclerview item clicks
     */
    override fun playerItemClickListener(player: Player) {
        Log.d(TAG, "playerItemClickListener: Clicked. ${player.playerName}")
        if (viewModel.multiSelectionHandler.isMultiSelectionStateActive()) {
            viewModel.multiSelectionHandler.addOrRemovePlayersFromSelectedList(player)
            Log.d(TAG, "playerItemClickListener: Selected")
        } else {
            Log.d(TAG, "playerItemClickListener: Moved to another Fragment")
            findNavController().navigate(PlayerListFragmentDirections.actionPlayersToPlayerDetailsFragment(player))
        }
    }

    override fun buttonClickListeners(adapter: SectionedPlayerListAdapter) {
        super.buttonClickListeners(adapter)

        binding.confirmButton.setOnClickListener {
            deletePlayersWithConfirmation()
        }

        binding.optionsButton.setOnClickListener {
            showPopUpMenu()
        }
    }

    private fun deletePlayersWithConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("The selected players will be permanently deleted.")
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.multiSelectionHandler.deletePlayers()
                dialog.dismiss()
            }.show()
    }

    override fun observeViewModel(adapter: SectionedPlayerListAdapter) {
        super.observeViewModel(adapter)
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

    /**
     * Toolbar states
     */
    override fun setNormalStateLayout() {
        binding.optionsButton.visibility = View.VISIBLE
        modeViewVisibility(View.GONE)
    }

    override fun setSelectedStateLayout() {
        binding.optionsButton.visibility = View.GONE
        modeViewVisibility(View.VISIBLE)
    }

    private fun modeViewVisibility(visibility: Int) {
        binding.confirmButton.visibility = visibility
        binding.cancelSelectionButton.visibility = visibility
        binding.confirmButton.visibility = visibility
    }


    /**
     * Options menu
     */
    @SuppressLint("RestrictedApi") // Required for setOptionalIconsVisible
    private fun showPopUpMenu() {
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
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }


}