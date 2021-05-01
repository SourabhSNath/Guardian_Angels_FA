package com.guardianangels.football.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.guardianangels.football.data.Picture
import com.guardianangels.football.network.NetworkState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class GalleryRepository @Inject constructor(firestore: FirebaseFirestore, private val storage: FirebaseStorage) {

    private val storageReference = storage.reference
    private val firestoreCollectionRef = firestore.collection("gallery")

    fun addPictures(uris: List<Uri>, picture: Picture) = flow {
        emit(NetworkState.loading())

        for (uri in uris) {
            picture.addImage(uri)
            picture.dateAndTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
            firestoreCollectionRef.add(picture).await()
        }

        emit(NetworkState.success(true))
    }.catch { emit(NetworkState.failed(it, it.message.toString())) }

    private suspend inline fun Picture.addImage(uri: Uri) {
        val pictureRef = storageReference.child("GalleryPictures/${uri.lastPathSegment}")

        val downloadUrl = pictureRef.putFile(uri)
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()

        imageLink = downloadUrl
    }

    fun getPictures(ascending: Boolean) = flow {
        emit(NetworkState.loading())

        val pictureList = firestoreCollectionRef.get().await().map {
            it.toObject(Picture::class.java).setId(it.id)
        }.sortedByDescending {
            it.dateAndTime
        }

        if (ascending) {
            emit(NetworkState.success(pictureList))
        } else {
            emit(NetworkState.success(pictureList.reversed()))
        }

    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }


    fun deletePictures(picture: Picture) = flow {
        emit(NetworkState.loading())

        firestoreCollectionRef.document(picture.id!!).delete().onSuccessTask {
            storage.getReferenceFromUrl(picture.imageLink!!).delete()
        }.await()

        emit(NetworkState.success(true))
    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }

    private fun Picture.setId(id: String): Picture {
        this.id = id
        return this
    }
}