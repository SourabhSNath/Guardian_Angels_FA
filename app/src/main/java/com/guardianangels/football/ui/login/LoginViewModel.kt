package com.guardianangels.football.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<NetworkState<Boolean>>()
    val loginState: LiveData<NetworkState<Boolean>> get() = _loginState

    fun checkLogin() = teamRepository.loginRepository.checkLogin()

    fun login(password: String) = viewModelScope.launch {
        teamRepository.loginRepository.loginAdmin(password).collect {
            _loginState.value = it
        }
    }

    fun logout() {
        teamRepository.loginRepository.logoutAdmin()
    }
}