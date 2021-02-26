package com.guardianangels.football.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<NetworkState<Boolean>>()
    val loginState: LiveData<NetworkState<Boolean>> get() = _loginState

    fun checkLogin() = loginRepository.checkLogin()

    fun login(password: String) = viewModelScope.launch {
        loginRepository.loginAdmin(password).collect {
            _loginState.value = it
        }
    }

    fun logout() {
        loginRepository.logoutAdmin()
    }
}