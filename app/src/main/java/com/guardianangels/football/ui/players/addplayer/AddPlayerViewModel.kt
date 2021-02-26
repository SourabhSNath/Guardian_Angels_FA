package com.guardianangels.football.ui.players.addplayer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Player
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.TeamRepository
import com.guardianangels.football.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddPlayerViewModel @Inject constructor(
    private val teamRepository: TeamRepository
) : ViewModel() {

    companion object {
        const val TAG = "AddPlayerViewModel"
    }

    val dropDownList = arrayListOf(Constants.GOAL_KEEPER, Constants.DEFENDER, Constants.FORWARD, Constants.MIDFIELDER, Constants.COACH)

    private val _playerAddedRef = MutableLiveData<NetworkState<Boolean>>()
    val playerAddedRef: LiveData<NetworkState<Boolean>> get() = _playerAddedRef

    private val _playerUpdatedResult = MutableLiveData<NetworkState<Player?>>()
    val playerUpdatedResult: LiveData<NetworkState<Player?>> get() = _playerUpdatedResult

    // Channel to wait for the uri to arrive and then pass it to the teamRepository
    private val uriChannel = Channel<Uri>()

    /**
     * Add a new player
     */
    fun addPlayer(player: Player) {
        viewModelScope.launch {
            teamRepository.addPlayer(player, uriChannel.receive()).collect {
                _playerAddedRef.value = it
            }
        }
    }

    /**
     * Update the player
     */
    fun updatePlayer(player: Player) {
        viewModelScope.launch {
            Timber.tag(TAG).d("updatePlayer: Passing to teamRepository")
            teamRepository.updatePlayer(player, uriChannel.poll()).collect {
                _playerUpdatedResult.value = it
            }
        }
    }

    fun playerImageUri(uri: Uri) {
        viewModelScope.launch {
            uriChannel.poll() // Remove the previous image uri here. Poll seems to not work as intended when using it as receive.
            uriChannel.send(uri)
        }
    }
}