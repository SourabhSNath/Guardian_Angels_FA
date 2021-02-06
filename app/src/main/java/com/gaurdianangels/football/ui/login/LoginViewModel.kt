package com.gaurdianangels.football.ui.login

import androidx.lifecycle.*
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
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