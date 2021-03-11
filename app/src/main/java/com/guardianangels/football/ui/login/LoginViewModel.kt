package com.guardianangels.football.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.guardianangels.football.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    companion object {
        /* This is the only email allowed to login. The user will not have the ability to create a new account. */
        private const val EMAIL = "admin@guardian.angels"
    }

    fun checkLogin() = auth.currentUser != null

    private val _loginState = MutableLiveData<NetworkState<Boolean>>()
    val loginState: LiveData<NetworkState<Boolean>> get() = _loginState


    fun login(password: String) {
        viewModelScope.launch(Dispatchers.IO) {

            _loginState.postValue(NetworkState.loading())

            try {
                auth.signInWithEmailAndPassword(EMAIL, password).await()

                // Check if the user has logged in successfully
                if (checkLogin()) {
                    _loginState.postValue(NetworkState.success(true))
                } else {
                    Timber.d("Failed Login")
                    _loginState.postValue(NetworkState.failed(Exception("Failed Login"), "Log in Failed. Please try again."))
                }
            } catch (e: Exception) {
                Timber.d(e.message.toString())
                _loginState.postValue(NetworkState.failed(e, e.message.toString()))
            }

        }
    }

    fun logout() {
        auth.signOut()
    }
}