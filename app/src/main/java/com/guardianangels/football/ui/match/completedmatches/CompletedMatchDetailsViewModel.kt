package com.guardianangels.football.ui.match.completedmatches

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
class CompletedMatchDetailsViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

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

    private val _isMatchDeleted = MutableLiveData<NetworkState<Boolean>>()
    val isMatchDeleted: LiveData<NetworkState<Boolean>> get() = _isMatchDeleted
    fun deleteMatch(match: Match) {
        viewModelScope.launch {
            matchRepository.deleteMatch(match).collect {
                _isMatchDeleted.value = it
            }
        }
    }
}