package com.gaurdianangels.football.ui.matches

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.ui.base.BasePlayerListFragment
import com.gaurdianangels.football.ui.base.ToolbarState
import com.gaurdianangels.football.ui.base.SectionedPlayerListAdapter
import com.gaurdianangels.football.util.Constants.PLAYER_SELECTED_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchPlayerListFragment : BasePlayerListFragment() {

    companion object {
        const val TAG = "MatchPlayerListFragment"
    }

    private val args: MatchPlayerListFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.multiSelectionHandler.setToolbarState(ToolbarState.MultiSelectState) // Start with multi selection

        binding.optionsButton.visibility = View.GONE
        binding.backButton.visibility = View.VISIBLE

        binding.confirmButton.apply {
            val params = this.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = 48
            text = resources.getString(R.string.confirm)
            icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_done, null)
        }
    }

    /**
     * Take the previously selected list of players and set it as the currently selected players
     */
    override fun listenForFragmentResult() {
        val players = args.playerList
        if (players != null) {
            viewModel.multiSelectionHandler.setSelectedPlayers(players)
        }
    }

    override fun playerItemClickListener(player: Player) {
        viewModel.multiSelectionHandler.addOrRemovePlayersFromSelectedList(player)
    }

    override fun buttonClickListeners(adapter: SectionedPlayerListAdapter) {
        super.buttonClickListeners(adapter)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Pass selected players list to the previous fragment.
     * Can pass empty list.
     */
    override fun setPlayersList(selectedPlayers: List<Player>) {
        binding.confirmButton.setOnClickListener {
            Log.d(TAG, "setPlayersList: ${selectedPlayers.size}")
            val navController = findNavController()
            navController.previousBackStackEntry?.savedStateHandle?.set(PLAYER_SELECTED_KEY, selectedPlayers)
            navController.popBackStack()
        }
    }

    override fun setNormalStateLayout() {
        setToolbarTitle()
    }

    private fun setToolbarTitle() {
        binding.playersTV.text = resources.getString(R.string.select_team)
    }

    // Nothing to do here
    override fun setSelectedStateLayout() {}

}