package com.gaurdianangels.football.ui.players.addplayer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.repository.MainRepository
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPlayerViewModel @Inject constructor(
    private val mainRepo: MainRepository
) : ViewModel() {

    companion object {
        const val TAG = "AddPlayerViewModel"
    }

    private val _playerAddedRef = MutableLiveData<NetworkState<DocumentReference>>()
    val playerAddedRef: LiveData<NetworkState<DocumentReference>> get() = _playerAddedRef


    // Channel to wait for the uri to arrive and then pass it to the mainRepo
    private val uriChannel = Channel<Uri>()

    fun addPlayer(player: Player) = viewModelScope.launch {
        mainRepo.addPlayer(player, uriChannel.receive()).collect {
            _playerAddedRef.value = it
        }
    }

    fun playerImageUri(uri: Uri) {
        viewModelScope.launch { uriChannel.send(uri) }
    }

}