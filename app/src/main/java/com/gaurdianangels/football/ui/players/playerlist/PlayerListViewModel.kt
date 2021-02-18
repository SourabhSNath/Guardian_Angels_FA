package com.gaurdianangels.football.ui.players.playerlist

import androidx.lifecycle.*
import com.gaurdianangels.football.data.SectionedPlayerRecyclerItem
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.repository.MainRepository
import com.gaurdianangels.football.ui.MultiSelectionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerListViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {
    val checkLogin = mainRepository.checkLogin().asLiveData()
    private val _sectionedPlayerResultLiveData = MutableLiveData<NetworkState<List<SectionedPlayerRecyclerItem>>>()
    val sectionedPlayerResultLiveData: LiveData<NetworkState<List<SectionedPlayerRecyclerItem>>> get() = _sectionedPlayerResultLiveData

    val multiSelectionHandler = MultiSelectionHandler(mainRepository, viewModelScope)

    init {
        getSectionedPlayerResultLiveData()
    }

    fun getSectionedPlayerResultLiveData() {
        viewModelScope.launch {
            mainRepository.getPlayers().collect {
                _sectionedPlayerResultLiveData.value = it
            }
        }
    }


}