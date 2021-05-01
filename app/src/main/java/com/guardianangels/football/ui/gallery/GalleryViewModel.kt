package com.guardianangels.football.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.guardianangels.football.data.Picture
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(private val galleryRepository: GalleryRepository, auth: FirebaseAuth) : ViewModel() {

    private val _pictureList = MutableLiveData<NetworkState<List<Picture>>>()
    val pictureList: LiveData<NetworkState<List<Picture>>> get() = _pictureList

    val isUserLoggedIn = auth.currentUser != null

    init {
        getGalleryImages()
    }


    fun getGalleryImages(ascending: Boolean = true) {
        viewModelScope.launch {
            galleryRepository.getPictures(ascending).collect {
                _pictureList.value = it
            }
        }
    }
}