package com.guardianangels.football.ui.match.upcomingmatches

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Match
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchListViewModel @Inject constructor(private val matchRepository: MatchRepository) : ViewModel() {

    private val _upcomingMatches = MutableLiveData<NetworkState<List<Match>>>()
    val upcomingMatch: LiveData<NetworkState<List<Match>>> get() = _upcomingMatches

    init {
        getUpcomingMatches()
    }

    val isUserLoggedIn: Boolean = matchRepository.auth.currentUser != null

    fun getUpcomingMatches() {
        viewModelScope.launch {
            matchRepository.getAllUpcomingMatches().collect {
                _upcomingMatches.value = it
            }
        }
    }

}