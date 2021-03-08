package com.guardianangels.football.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.guardianangels.football.data.Player
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

enum class ToolbarState {
    NormalState,
    MultiSelectState
}

/**
 * Class to handle RecyclerView MultiSelection
 * Not using the recyclerview-selection library since it's buggy and doesn't support single click to select.
 */
class MultiSelectionHandler(private val teamRepository: TeamRepository, private val viewModelScope: CoroutineScope) {

    private val _toolbarState: MutableLiveData<ToolbarState> = MutableLiveData(ToolbarState.NormalState)
    val toolbarState: LiveData<ToolbarState> get() = _toolbarState

    fun setToolbarState(toolbarState: ToolbarState) {
        _toolbarState.value = toolbarState
    }

    fun isMultiSelectionStateActive(): Boolean = _toolbarState.value == ToolbarState.MultiSelectState

    private val _selectedPlayers = MutableLiveData<ArrayList<Player>>(ArrayList())
    val selectedPlayers: LiveData<ArrayList<Player>> get() = _selectedPlayers

    /**
     * Simple method to add or remove Players from the selection.
     * The list is created in the init method when the toolbar state changes to multiSelectState.
     */
    fun addOrRemovePlayersFromSelectedList(player: Player) {
        val list = _selectedPlayers.value

        list?.let {
            if (list.contains(player)) {
                list.remove(player)
                Timber.d("addOrRemovePlayersFromSelectedList: Removed ${player.playerName}")
            } else {
                list.add(player)
                Timber.d("addOrRemovePlayersFromSelectedList: Added ${player.playerName}")
            }
        }

        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") // Shows warning without this.
        _selectedPlayers.value = list!!
    }

    /**
     * Required to clear selection when cancel or confirming selection
     */
    fun clearSelectedList() {
        _selectedPlayers.value?.clear()
        Timber.d("clearSelectedList: Cleared selected players")
    }

    /**
     * Delete the selected Players from firebase
     */
    private val _deletedState = MutableLiveData<NetworkState<Boolean>>()
    val deletedState: LiveData<NetworkState<Boolean>> get() = _deletedState
    fun deletePlayers() {
        viewModelScope.launch {
            selectedPlayers.value?.let {
                teamRepository.deleteMultiplePlayers(it).collect { result ->
                    _deletedState.value = result
                }
            }
        }
    }

    fun setSelectedPlayers(players: Array<Player>?) {
        if (!players.isNullOrEmpty()) {
            Timber.d("setSelectedPlayers: Received again : ${players.size}")
            players.forEach { player ->
                val list = _selectedPlayers.value
                list?.let {
                    if (!list.contains(player)) {
                        list.add(player)
                    }
                }
            }
        }
    }
}