package com.guardianangels.football.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.guardianangels.football.data.Match
import com.guardianangels.football.network.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MatchRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    val auth: FirebaseAuth
) {

    /**
     * Reference to the match firestore collection
     */
    private val matchCollectionRef = firestore.collection("matches")
    private val storageReference = storage.reference


    fun addMatchData(match: Match, team1Logo: Uri?, team2Logo: Uri) = flow {
        emit(NetworkState.loading())

        if (team1Logo != null) {
            match.setTeam1ImageUrl(team1Logo) // This can be null if the logo is default guardian angels
        }
        match.setTeam2ImageUrl(team2Logo)

        matchCollectionRef.add(match).await() // Add the match object to firestore

        emit(NetworkState.success(true))
    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.IO)


    fun updateMatchData(match: Match, team1Logo: Uri?, team2Logo: Uri?) = flow {
        emit(NetworkState.loading())

        if (team1Logo != null) {

            val remoteTeam1Logo = match.team1Logo!!
            match.setTeam1ImageUrl(team1Logo)

            if (remoteTeam1Logo.isNotEmpty()) {
                /* Delete the previous team 1 logo from storage,
                 * No check can be done to see if the logos are the same since different UUIDs are generated each time,
                 * and a different file can have the same name. */
                storage.getReferenceFromUrl(remoteTeam1Logo).delete().await()
                Timber.d("Deleted Previous Team1 logo")
            }
        }

        if (team2Logo != null) {

            val remoteTeam2Logo = match.team2Logo!!
            match.setTeam2ImageUrl(team2Logo)

            if (remoteTeam2Logo.isNotEmpty()) {
                storage.getReferenceFromUrl(remoteTeam2Logo).delete().await() // Delete the previous team 2 logo from storage
                Timber.d("Deleted Previous Team2 Logo")
            }

        }

        val matchModel: Match? = updateFirestoreMatch(match)
        emit(NetworkState.success(matchModel!!))

    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Update the match when it's over.
     * Return the list of Team members, this will be used in the next step where there stats would be updated.
     */
    fun updateCompletedMatch(match: Match) = flow {
        emit(NetworkState.loading())

        val matchModel = updateFirestoreMatch(match)
        emit(NetworkState.success(matchModel?.team1TeamIds))

    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.IO)


    private suspend fun updateFirestoreMatch(match: Match): Match? {
        var matchModel: Match? = null
        matchCollectionRef.document(match.matchID!!).set(match).onSuccessTask {
            matchCollectionRef.document(match.matchID!!).get()
                .addOnSuccessListener {
                    matchModel = it.toObject(Match::class.java)?.setId(it.id)!!
                }
        }.await()
        return matchModel
    }


    /**
     * Set the image download url to the Upcoming Match model before uploading it to firestore.
     */
    private suspend inline fun Match.setTeam1ImageUrl(uri: Uri) {
        team1Logo = getUri(uri)
    }

    private suspend inline fun Match.setTeam2ImageUrl(uri: Uri) {
        team2Logo = getUri(uri)
    }

    /**
     * Set the image url after uploading it to storage.
     */
    private suspend fun getUri(uri: Uri): String {
        val uuid = UUID.randomUUID()
        val teamsImageStorageReference = storageReference.child("TeamsImage/${uuid}_${uri.lastPathSegment}")

        return teamsImageStorageReference.putFile(uri)
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()
    }


    fun getAllUpcomingMatches() = flow {
        emit(NetworkState.loading())

        val matches = getUpcomingMatchList()

        emit(NetworkState.success(matches))
    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.Default)

    /**
     * Get nextUpcomingMatch, to be displayed in HomeFragment
     */
    fun getNextUpcomingMatch() = flow {
        emit(NetworkState.loading())
        val match = getUpcomingMatchList()[0]
        emit(NetworkState.success(match))
    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.Default)


    /**
     * Get a list of first 10 completed matches.
     */
    fun getCompletedMatches() = flow {
        emit(NetworkState.loading())

        /* Get top 10 matches after deleting older matches */
        val matches: List<Match> = getMatchList()
            .filter { it.isCompleted == true }
            .sortedByDescending { it.dateAndTime }
            .deleteOlderCompletedMatches()

        emit(NetworkState.success(matches))

    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.Default)

    /**
     * List of all matches
     */
    private suspend fun getMatchList() = withContext(Dispatchers.IO) {
        matchCollectionRef.get().await().map { it.toObject(Match::class.java).setId(it.id) }
    }

    /**
     * Get upcoming matches
     */
    private suspend fun getUpcomingMatchList() = getMatchList().filter { it.isCompleted == null || it.isCompleted == false }.sortedBy { it.dateAndTime }

    private fun Match.setId(id: String): Match {
        this.matchID = id
        return this
    }

    /**
     * Delete a match
     */
    fun deleteMatch(match: Match) = flow {
        emit(NetworkState.loading())
        deleteMatchFromFirebase(match)
        emit(NetworkState.success(true))
    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Delete completed matches if there are more than 10
     */
    private suspend inline fun List<Match>.deleteOlderCompletedMatches(): List<Match> {
        if (size > 9) {
            for (i in 10 until size) {
                deleteMatchFromFirebase(this[i])
            }
        }
        return this
    }


    private suspend fun deleteMatchFromFirebase(match: Match) {
        val team1Logo = match.team1Logo!!
        val team2Logo = match.team2Logo!!

        matchCollectionRef.document(match.matchID!!).delete().addOnSuccessListener {
            if (team1Logo.isNotEmpty()) storage.getReferenceFromUrl(team1Logo).delete()
            if (team2Logo.isNotEmpty()) storage.getReferenceFromUrl(team2Logo).delete()
        }.await()
    }
}