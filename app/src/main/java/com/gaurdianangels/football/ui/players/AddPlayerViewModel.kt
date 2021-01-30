package com.gaurdianangels.football.ui.players

import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.gaurdianangels.football.model.Player
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.repository.MainRepository
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AddPlayerViewModel @ViewModelInject constructor(
    @Assisted savedState: SavedStateHandle,
    private val mainRepo: MainRepository
) : ViewModel() {

    private val _playerAddedRef = MutableLiveData<NetworkState<DocumentReference>>()
    val playerAddedRef: LiveData<NetworkState<DocumentReference>> get() = _playerAddedRef

    val checkLogin: LiveData<Boolean> = mainRepo.checkLogin().asLiveData()

    private lateinit var uri: Uri

    fun addPlayer(player: Player) {

        if (this::uri.isInitialized) {

        }
        viewModelScope.launch {
            mainRepo.addPlayer(player).collect {
                _playerAddedRef.value = it
            }
        }
    }

    fun playerImageUri(uri: Uri) {
        this.uri = uri
    }

}