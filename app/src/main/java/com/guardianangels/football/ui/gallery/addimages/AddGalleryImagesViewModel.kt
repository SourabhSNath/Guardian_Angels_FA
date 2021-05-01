package com.guardianangels.football.ui.gallery.addimages

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Picture
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddGalleryImagesViewModel @Inject constructor(private val galleryRepository: GalleryRepository) : ViewModel() {

    private val _addStatus = MutableLiveData<NetworkState<Boolean>>()
    val addStatus: LiveData<NetworkState<Boolean>> get() = _addStatus

    fun addGalleryImages(pictureUris: List<Uri>, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            galleryRepository.addPictures(pictureUris, Picture(imageDescription = description)).collect {
                _addStatus.postValue(it)
            }
        }
    }
}