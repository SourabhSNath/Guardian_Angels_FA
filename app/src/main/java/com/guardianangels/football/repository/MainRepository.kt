package com.guardianangels.football.repository

import android.net.Uri
import android.util.Log
import com.guardianangels.football.data.Player
import com.guardianangels.football.data.SectionedPlayerRecyclerItem
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Converters.Companion.getPlayerTypeString
import com.google.firebase.auth.FirebaseAuth
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
    private val storage: FirebaseStorage
) {

    companion object {
        private const val TAG = "MainRepository"

        /* This is the only email allowed to login. The user will not have the ability to create a new account. */
        private const val EMAIL = "admin@guardian.angels"
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
    fun addPlayer(player: Player, uri: Uri) = flow {
        emit(NetworkState.loading())

        player.setImage(uri)
        playerCollectionRef.add(player).await()

        emit(NetworkState.success(true))

    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /**
     * Update the player.
     */
    fun updatePlayer(player: Player, uri: Uri?) = flow {
        emit(NetworkState.loading())

        if (uri != null) {
            storage.getReferenceFromUrl(player.remoteUri!!).delete().await() // First delete the previous image from storage.
            player.setImage(uri)
        }

        var playerModel: Player? = null
        playerCollectionRef.document(player.id!!).set(player).onSuccessTask {
            playerCollectionRef.document(player.id!!).get()
                .addOnSuccessListener {
                    playerModel = it.toObject(Player::class.java)?.setID(it.id)!!
                }
        }.await()

        emit(NetworkState.success(playerModel))
    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /**
     * Set the image url after uploading it to storage.
     */
    private suspend fun Player.setImage(uri: Uri) {
        val playerImageReference = storageReference.child("PlayerImages/${auth.currentUser?.uid}/${uri.lastPathSegment}")

        val playerImageDownloadUrl = playerImageReference.putFile(uri)
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()

        remoteUri = playerImageDownloadUrl
    }


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


    /**
     * Delete multiple selected players
     */
    fun deleteMultiplePlayers(players: ArrayList<Player>) = flow {

        Log.d(TAG, "deleteMultiplePlayers Called: list size = ${players.size}")
        emit(NetworkState.loading())
        for (player in players) {
            Log.d(TAG, "deleteMultiplePlayers: ${player.id}")

            playerCollectionRef.document(player.id!!).delete().onSuccessTask {
                // When deleting the player is successful
                Log.d(TAG, "deleteMultiplePlayers: Player Deleted, continue with storage deletion")
                storage.getReferenceFromUrl(player.remoteUri!!).delete() // Delete using the url, since no direct path is available
            }.await()
        }
        emit(NetworkState.success(true))
    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /**
     * Set player id, get the id from the query doc and set it to the object
     */
    private fun Player.setID(id: String): Player {
        this.id = id
        return this
    }

    /**
     * Upcoming Matches
     */

    private val upcomingMatchCollectionRef = firestore.collection("upcoming_matches")

    fun addUpcomingMatch() {}
}