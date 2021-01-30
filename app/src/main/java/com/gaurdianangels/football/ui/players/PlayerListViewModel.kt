package com.gaurdianangels.football.ui.players

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.gaurdianangels.football.repository.MainRepository

class PlayerListViewModel @ViewModelInject constructor(private val mainRepository: MainRepository) : ViewModel() {

    val checkLogin = mainRepository.checkLogin().asLiveData()
}