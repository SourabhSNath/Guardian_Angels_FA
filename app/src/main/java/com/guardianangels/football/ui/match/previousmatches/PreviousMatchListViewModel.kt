package com.guardianangels.football.ui.match.previousmatches

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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PreviousMatchListViewModel @Inject constructor(private val matchRepository: MatchRepository) : ViewModel() {

    private val _previousCompletedMatches = MutableLiveData<NetworkState<List<Match>>>()
    val previousCompletedMatches: LiveData<NetworkState<List<Match>>> get() = _previousCompletedMatches

    init {
        getPreviousMatches()
    }

    fun getPreviousMatches() {
        viewModelScope.launch {
            matchRepository.getCompletedMatches().collect {
                Timber.d("Get Previous matches")
                _previousCompletedMatches.value = it
            }
        }
    }
}