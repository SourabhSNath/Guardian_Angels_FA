package com.guardianangels.football.ui.match.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Player
import com.guardianangels.football.data.PlayerType
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UpdateTeamPlayerViewModel @Inject constructor(private val teamRepository: TeamRepository) : ViewModel() {

    private val _playersLiveData = MutableLiveData<NetworkState<List<Player>>>()
    val playersLiveData: LiveData<NetworkState<List<Player>>> get() = _playersLiveData

    fun getPlayersFromIds(ids: List<String>) {
        viewModelScope.launch {
            teamRepository.getPlayers(ids).collect {
                _playersLiveData.value = it
            }
        }
    }

    private val _playersUpdate = MutableLiveData<NetworkState<Boolean>>()
    val playersUpdated: LiveData<NetworkState<Boolean>> get() = _playersUpdate

    fun updatePlayers(players: List<Player>, isWin: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            for (n in players.indices) {
                players[n].apply {
                    totalGames = totalGames!!.inc()
                    if (playerType == PlayerType.COACH && isWin) {
                        Timber.d("$playerType && $isWin")
                        totalWins = totalWins!!.inc()
                    } else {
                        Timber.d("$playerType && $isWin")
                    }
                }
            }

            Timber.d("Total Games: ${players.map { it.totalGames }}")

            teamRepository.updatePlayers(players).collect {
                _playersUpdate.postValue(it)
            }
        }
    }
}