package com.gaurdianangels.football.ui.login

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.repository.MainRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel @ViewModelInject constructor(
    @Assisted savedState: SavedStateHandle,
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<NetworkState<Boolean>>()
    val loginState: LiveData<NetworkState<Boolean>> get() = _loginState

    val checkLogin get() = mainRepository.checkLogin().asLiveData()


    fun login(password: String) = viewModelScope.launch {
        mainRepository.loginAdmin(password).collect {
            _loginState.value = it
        }
    }

    fun logout() {
        mainRepository.logoutAdmin()
    }
}