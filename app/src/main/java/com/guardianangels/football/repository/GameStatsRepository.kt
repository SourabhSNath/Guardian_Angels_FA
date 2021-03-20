package com.guardianangels.football.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.guardianangels.football.data.GameResults
import com.guardianangels.football.data.GameStats
import com.guardianangels.football.network.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class GameStatsRepository @Inject constructor(firestore: FirebaseFirestore) {

    private companion object {
        private const val DOC_ID = "stats"
    }

    private val matchStatsRef = firestore.collection("match_stats")

    /**
     * Add the game stats.
     * The team scores are checked here and the Result is passed to setScore()
     */
    fun addGameStats(team1Score: Int, team2Score: Int) = flow {
        emit(NetworkState.loading())
        when {
            team1Score > team2Score -> setScore(GameResults.WIN)
            team1Score < team2Score -> setScore(GameResults.LOSS)
            team1Score == team2Score -> setScore(GameResults.DRAW)
        }
        emit(NetworkState.success(true))
    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Get the previous results, and increment the values according to the Match Result.
     */
    private suspend fun setScore(result: GameResults) {
        val gameStats = getGameStatsFromFirestore().also { Timber.d("Previous-> G ${it.games}, W ${it.wins}, D ${it.draws}, L ${it.losses}") }

        when (result) {
            GameResults.WIN -> gameStats.wins = gameStats.wins!!.inc()
            GameResults.LOSS -> gameStats.losses = gameStats.losses!!.inc()
            GameResults.DRAW -> gameStats.draws = gameStats.draws!!.inc()
        }
        gameStats.games = gameStats.games!!.inc()

        matchStatsRef.document(DOC_ID).set(gameStats).await()
    }

    /**
     * Get the stats to be displayed in HomeFragment.
     */
    fun getGameStats() = flow {
        emit(NetworkState.loading())
        val gameStat = getGameStatsFromFirestore()
        emit(NetworkState.success(gameStat))
    }.catch {
        emit(NetworkState.failed(it, it.message.toString()))
    }.flowOn(Dispatchers.IO)

    /**
     * Get the stats from Firestore and check if it exists in Firestore.
     * If it doesn't exist create a new GameStats object and return that instead.
     */
    private suspend fun getGameStatsFromFirestore(): GameStats {
        val doc = matchStatsRef.document(DOC_ID).get().await()

        return if (doc.exists())
            doc.toObject(GameStats::class.java)!!.also { Timber.d("Get gameStats from firestore.") }
        else
            GameStats(0, 0, 0, 0).also { Timber.d("Get new GameStat object.") }
    }
}