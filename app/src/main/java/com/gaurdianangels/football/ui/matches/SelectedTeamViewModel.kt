package com.gaurdianangels.football.ui.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.gaurdianangels.football.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectedTeamViewModel @Inject constructor(mainRepository: MainRepository) : ViewModel() {
    val teamList = mainRepository.getPlayers().asLiveData()
}