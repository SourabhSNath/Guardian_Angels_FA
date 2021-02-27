package com.guardianangels.football.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.guardianangels.football.data.Match
import com.guardianangels.football.network.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MatchRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val loginRepository: LoginRepository
) {

    private companion object {
        private const val TAG = "MatchRepository"
    }

    /**
     * Reference to the players firestore collection
     */
    private val matchCollectionRef = firestore.collection("matches")
    private val storageReference = storage.reference

    /**
     * Add players to firebase cloud firestore.
     * Emitting a reference to the document along with network states.
     */
    fun addMatchData(match: Match, team1Logo: Uri?, team2Logo: Uri) = flow {
        emit(NetworkState.loading())

        if (team1Logo != null) {
            match.setTeam1Image(team1Logo) // This can be null if the logo is default guardian angels
        }
        match.setTeam2Image(team2Logo)
        matchCollectionRef.add(match).await()

        emit(NetworkState.success(true))

    }.catch {
        emit(NetworkState.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Set the image download url to the Upcoming Match model before uploading it to firestore.
     */
    private suspend inline fun Match.setTeam1Image(uri: Uri) {
        val teamImageDownloadUrl = getUri(uri)
        team1Logo = teamImageDownloadUrl
    }

    private suspend inline fun Match.setTeam2Image(uri: Uri) {
        val teamImageDownloadUrl = getUri(uri)
        team2Logo = teamImageDownloadUrl
    }

    /**
     * Set the image url after uploading it to storage.
     */
    private suspend fun getUri(uri: Uri): String {
        val teamsImageStorageReference = storageReference.child("TeamsImage/${loginRepository.currentUserUid}/${uri.lastPathSegment}")

        return teamsImageStorageReference.putFile(uri)
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()
    }

}