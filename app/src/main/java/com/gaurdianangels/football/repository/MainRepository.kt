package com.gaurdianangels.football.repository

import com.gaurdianangels.football.model.Player
import com.gaurdianangels.football.network.NetworkState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    companion object {
        /* This is the only email allowed to login. The user will not have the ability to create a new account. */
        const val EMAIL = "admin@gaurdian.angels"
    }

    /***************************
     * Auth
     ***************************/

    /**
     * Check if the user is logged in
     */
    fun checkLogin() = flow { emit(auth.currentUser != null) }

    /**
     * Login user and return a flow
     */
    fun loginAdmin(password: String) = flow<NetworkState<Boolean>> {
        emit(NetworkState.loading())
        auth.signInWithEmailAndPassword(EMAIL, password).await()
        if (checkLogin().first())
            emit(NetworkState.success(true))
        else
            emit(NetworkState.failed("Log in Failed. Please try again."))
    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /**
     * Logout
     */
    fun logoutAdmin() {
        auth.signOut()
    }


    /*************************************
     * Firestore
     *************************************/

    /**
     * Reference to the players firestore collection
     */
    private val playerCollectionRef = firestore.collection("players")

    /**
     * Add players to firebase cloud firestore.
     * Emitting a reference to the document along with network states.
     */
    fun addPlayer(player: Player) = flow<NetworkState<DocumentReference>> {
        emit(NetworkState.loading())

        val playerCollection = playerCollectionRef.add(player).await()

        emit(NetworkState.success(playerCollection))

    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /*************************************
     * Storage
     *************************************/

    fun uploadPhotos() {

    }
}