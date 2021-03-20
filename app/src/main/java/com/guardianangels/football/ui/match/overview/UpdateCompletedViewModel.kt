package com.guardianangels.football.ui.match.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Match
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.GameStatsRepository
import com.guardianangels.football.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateCompletedViewModel @Inject constructor(private val matchRepository: MatchRepository, private val gameStatsRepository: GameStatsRepository) :
    ViewModel() {

    private val _listOfTeamIds = MutableLiveData<NetworkState<List<String>?>>()
    val listOfTeamIds: LiveData<NetworkState<List<String>?>> get() = _listOfTeamIds

    private val _updateGameStat = MutableLiveData<NetworkState<Boolean>>()
    val updateGameStats: LiveData<NetworkState<Boolean>> get() = _updateGameStat

    /**
     *  Update the Game Stats with the Match Result
     */
    fun updateGameStats(team1Score: Int, team2Score: Int) {
        viewModelScope.launch {
            gameStatsRepository.addGameStats(team1Score, team2Score).collect {
                _updateGameStat.value = it
            }
        }
    }

    fun updateMatch(
        match: Match,
        team1Score: Int, team2Score: Int,
        team1ShootingStats: Int, team2ShootingStats: Int,
        team1AttactStats: Int, team2AttactStats: Int,
        team1PossesionStats: Int, team2PossesionStats: Int,
        team1CardStats: Int, team2CardStats: Int,
        team1CornerStats: Int, team2CornerStats: Int,
        notes: String
    ) {
        val matchCompleteData = match.copy(
            isCompleted = true,
            team1Score = team1Score,
            team2Score = team2Score,
            team1ShootingStats = team1ShootingStats,
            team2ShootingStats = team2ShootingStats,
            team1AttactStats = team1AttactStats,
            team2AttackStats = team2AttactStats,
            team1PossesionStats = team1PossesionStats,
            team2PossesionStats = team2PossesionStats,
            team1CardStats = team1CardStats,
            team2CardStats = team2CardStats,
            team1CornerStats = team1CornerStats,
            team2CornerStats = team2CornerStats,
            matchNotes = notes
        )

        viewModelScope.launch {
            /** Update the Match Stats */
            matchRepository.updateCompletedMatch(matchCompleteData).collect {
                _listOfTeamIds.value = it
            }
        }
    }
}