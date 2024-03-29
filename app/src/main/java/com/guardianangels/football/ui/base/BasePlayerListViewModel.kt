package com.guardianangels.football.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.SectionedPlayerRecyclerItem
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BasePlayerListViewModel @Inject constructor(private val teamRepository: TeamRepository) : ViewModel() {
    private val _sectionedPlayerResultLiveData = MutableLiveData<NetworkState<List<SectionedPlayerRecyclerItem>>>()
    val sectionedPlayerResultLiveData: LiveData<NetworkState<List<SectionedPlayerRecyclerItem>>> get() = _sectionedPlayerResultLiveData

    val multiSelectionHandler = MultiSelectionHandler(teamRepository, viewModelScope)
    val isUserLoggedIn = teamRepository.auth.currentUser != null

    init {
        getSectionedPlayerResultLiveData()
    }

    fun getSectionedPlayerResultLiveData() {
        viewModelScope.launch {
            teamRepository.getPlayers().collect {
                _sectionedPlayerResultLiveData.value = it
            }
        }
    }

}