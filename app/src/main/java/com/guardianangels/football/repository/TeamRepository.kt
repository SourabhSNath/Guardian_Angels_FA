package com.guardianangels.football.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.guardianangels.football.data.Player
import com.guardianangels.football.data.SectionedPlayerRecyclerItem
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Converters.Companion.getPlayerTypeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository to handle getting Team Details from firestore and storage.
 * Extends BaseRepository.
 */
class TeamRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    val auth: FirebaseAuth
) {

    private companion object {
        private const val TAG = "TeamRepository"
    }

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
    private suspend inline fun Player.setImage(uri: Uri) {
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
     * Get a list of players using the ids
     */
    fun getPlayers(ids: List<String>) = flow<NetworkState<List<Player>>> {
        emit(NetworkState.loading())
        val players = ArrayList<Player>()
        ids.forEach {
            val player = playerCollectionRef.document(it).get().await().toObject(Player::class.java)
            if (player != null) {
                players.add(player)
            }
        }
        emit(NetworkState.success(players))
    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Delete multiple selected players
     */
    fun deleteMultiplePlayers(players: ArrayList<Player>) = flow {

        emit(NetworkState.loading())
        for (player in players) {
            playerCollectionRef.document(player.id!!).delete().onSuccessTask {
                // When deleting the player is successful
                Timber.tag(TAG).d("deleteMultiplePlayers: ${player.playerName} Deleted, continue with storage deletion")
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
}