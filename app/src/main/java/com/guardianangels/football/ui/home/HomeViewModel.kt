package com.guardianangels.football.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.GameStats
import com.guardianangels.football.data.Match
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.GameStatsRepository
import com.guardianangels.football.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val gameStatsRepository: GameStatsRepository
) : ViewModel() {

    private val _upcomingMatch = MutableLiveData<NetworkState<Match>>()
    val upcomingMatch: LiveData<NetworkState<Match>> get() = _upcomingMatch

    init {
        viewModelScope.launch {
            getNextUpcomingMatch()
            delay(100) // Prevents a crash due to the everything being called at the same time.
            getGameStats()
            getPreviousMatch()
        }
    }

    fun isUserLoggedIn() = matchRepository.auth.currentUser != null

    fun getNextUpcomingMatch() {
        viewModelScope.launch {
            matchRepository.getNextUpcomingMatch().collect {
                Timber.d("Get upcoming match")
                _upcomingMatch.value = it
            }
        }
    }


    private val _gameStats = MutableLiveData<NetworkState<GameStats>>()
    val gameStats: LiveData<NetworkState<GameStats>> get() = _gameStats
    fun getGameStats() {
        viewModelScope.launch {
            gameStatsRepository.getGameStats().collect {
                Timber.d("Get game stats")
                _gameStats.value = it
            }
        }
    }

    private val _previousMatch = MutableLiveData<NetworkState<Match>>()
    val previousMatch: LiveData<NetworkState<Match>> get() = _previousMatch

    fun getPreviousMatch() {
        viewModelScope.launch {
            matchRepository.getPreviousMatch().collect {
                _previousMatch.value = it
            }
        }
    }
}