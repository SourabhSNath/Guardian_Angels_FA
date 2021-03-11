package com.guardianangels.football.ui.home

import androidx.lifecycle.*
import com.guardianangels.football.data.Match
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val matchRepository: MatchRepository) : ViewModel() {

    private val _upcomingMatch = MutableLiveData<NetworkState<Match>>()
    val upcomingMatch: LiveData<NetworkState<Match>> get() = _upcomingMatch

    init {
        getNextUpcomingMatch()
    }

    fun getNextUpcomingMatch() {
        viewModelScope.launch {
            matchRepository.getNextUpcomingMatch().collect {
                _upcomingMatch.value = it
            }
        }
    }
}