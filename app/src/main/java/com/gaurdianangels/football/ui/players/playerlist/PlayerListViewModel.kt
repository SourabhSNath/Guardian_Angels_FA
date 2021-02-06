package com.gaurdianangels.football.ui.players.playerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.gaurdianangels.football.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerListViewModel @Inject constructor(mainRepository: MainRepository) : ViewModel() {
    val checkLogin = mainRepository.checkLogin().asLiveData()
    val sectionedPlayerResultLiveData = mainRepository.getPlayers().asLiveData()
}