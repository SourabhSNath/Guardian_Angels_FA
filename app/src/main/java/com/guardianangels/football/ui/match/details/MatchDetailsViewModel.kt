package com.guardianangels.football.ui.match.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Match
import com.guardianangels.football.data.Player
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.MatchRepository
import com.guardianangels.football.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchDetailsViewModel @Inject constructor(private val matchRepository: MatchRepository, private val teamRepository: TeamRepository) :
    ViewModel() {

    private val _players = MutableLiveData<NetworkState<List<Player>>>()
    val players: LiveData<NetworkState<List<Player>>> get() = _players

    val isUserLoggedIn: Boolean = matchRepository.auth.currentUser != null

    fun getPlayers(team1Ids: List<String>) {
        viewModelScope.launch {
            teamRepository.getPlayers(team1Ids).collect {
                _players.value = it
            }
        }
    }

    private val _deleteState = MutableLiveData<NetworkState<Boolean>>()
    val deleteState: LiveData<NetworkState<Boolean>> get() = _deleteState
    fun deleteMatch(match: Match) {
        viewModelScope.launch {
            matchRepository.deleteMatch(match).collect {
                _deleteState.value = it
            }
        }
    }

}