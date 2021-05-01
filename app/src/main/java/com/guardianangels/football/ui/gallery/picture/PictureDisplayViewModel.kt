package com.guardianangels.football.ui.gallery.picture

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.guardianangels.football.data.Picture
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PictureDisplayViewModel @Inject constructor(private val galleryRepository: GalleryRepository, auth: FirebaseAuth) : ViewModel() {

    val isUserLoggedIn = auth.currentUser != null

    private val _deleteCompleteStatus = MutableLiveData<NetworkState<Boolean>>()
    val deleteCompleteStatus: LiveData<NetworkState<Boolean>> get() = _deleteCompleteStatus

    fun deletePicture(picture: Picture) {
        viewModelScope.launch(Dispatchers.IO) {
            galleryRepository.deletePictures(picture).collect {
                _deleteCompleteStatus.postValue(it)
            }
        }
    }
}