package com.gaurdianangels.football.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaurdianangels.football.data.SectionedPlayerRecyclerItem
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BasePlayerListViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {
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