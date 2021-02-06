package com.gaurdianangels.football.repository

import android.net.Uri
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.data.SectionedPlayerRecyclerItem
import com.gaurdianangels.football.network.NetworkState
import com.gaurdianangels.football.util.Converters.Companion.getPlayerTypeString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MainRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    storage: FirebaseStorage
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

        // Check if the user has logged in successfully
        if (checkLogin().first()) {
            emit(NetworkState.success(true))
        } else
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
    private val storageReference = storage.reference

    /**
     * Add players to firebase cloud firestore.
     * Emitting a reference to the document along with network states.
     */
    fun addPlayer(player: Player, uri: Uri) = flow<NetworkState<DocumentReference>> {
        emit(NetworkState.loading())
        val playerImageReference = storageReference.child("PlayerImages/${auth.currentUser?.uid}/${uri.lastPathSegment}")

        val playerImageDownloadUrl = playerImageReference.putFile(uri)
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()

        player.remoteUri = playerImageDownloadUrl
        val playerCollection = playerCollectionRef.add(player).await()

        emit(NetworkState.success(playerCollection))

    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /**
     * Get a list of all Players
     */
    fun getPlayers() = flow<NetworkState<List<SectionedPlayerRecyclerItem>>> {

        emit(NetworkState.loading())

        /*
         * Returns a map -> Map<Type, List<Player>>
         */
        val playersSectionGroup = playerCollectionRef.get().await().map {
            it.toObject(Player::class.java).setID(it.id)
        }.groupBy { it.playerType }.toSortedMap(compareBy { playerType ->
            playerType
        })

        /*
         * Take the Map and turn it into List<SectionedPlayerRecyclerItem>
         * Coach [Header]
         *  Player
         * Forward [Header]
         *  Player
         *  Player
         */
        val sectionedPlayerRecyclerItem = ArrayList<SectionedPlayerRecyclerItem>()
        for (type in playersSectionGroup.keys) {

            val playerType = type!!.getPlayerTypeString()

            playerType.let { sectionedPlayerRecyclerItem.add(SectionedPlayerRecyclerItem.PlayerTypeItem(playerType)) }
            for (player in playersSectionGroup.getValue(type)) {
                sectionedPlayerRecyclerItem.add(SectionedPlayerRecyclerItem.PlayerItem(player))
            }
        }

        emit(NetworkState.success(sectionedPlayerRecyclerItem))
    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    private fun Player.setID(id: String): Player {
        this.id = id
        return this
    }
}