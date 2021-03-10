package com.guardianangels.football.ui.match.addmatch

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Match
import com.guardianangels.football.data.Player
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.MatchRepository
import com.guardianangels.football.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.*
import javax.inject.Inject

@HiltViewModel
class AddUpcomingViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {

    private val dateChannel = Channel<LocalDate>()
    private val timeChannel = Channel<LocalTime>()

    private val _team1ImageUri = MutableLiveData<Uri?>()
    val team1ImageUri: LiveData<Uri?> get() = _team1ImageUri

    private val _team2ImageUri = MutableLiveData<Uri>()
    val team2ImageUri: LiveData<Uri> get() = _team2ImageUri

    private val _matchUploadResult = MutableLiveData<NetworkState<Boolean>>()
    val matchUploadResult: LiveData<NetworkState<Boolean>> get() = _matchUploadResult

    private val _matchUpdateResult = MutableLiveData<NetworkState<Match>>()
    val matchUpdateResult: LiveData<NetworkState<Match>> get() = _matchUpdateResult

    fun setDate(localDate: LocalDate) {
        viewModelScope.launch {
            dateChannel.send(localDate)
        }
    }

    fun setTime(time12: LocalTime) {
        viewModelScope.launch {
            timeChannel.send(time12)
        }
    }

    fun team1Image(it: Uri?) {
        _team1ImageUri.value = it
    }

    fun team2Image(it: Uri) {
        _team2ImageUri.value = it
    }

    private val _team = MutableLiveData<NetworkState<List<Player>>>()
    val team: LiveData<NetworkState<List<Player>>> get() = _team
    fun getTeamListFromIds(teamIds: List<String>) {
        viewModelScope.launch {
            teamRepository.getPlayers(teamIds).collect {
                _team.value = it
            }
        }
    }

    fun addMatchData(team1Name: String, team2Name: String, tournamentName: String, locationName: String, team: List<Player>) {
        viewModelScope.launch(Dispatchers.Default) {
            val date = dateChannel.poll()
            val time = timeChannel.poll()

            if (date != null && time != null) {
                val dateTime = date.atTime(time)!!
                val zoneID = ZoneId.systemDefault()
                val epoch: Long = dateTime.atZone(zoneID).toEpochSecond()

                Timber.d("$team1Name, $team2Name, $dateTime == $epoch, ${if (team.isEmpty()) "Players not selected" else team[0].playerName}")

                val listOfIds = arrayListOf<String>()
                team.sortedBy { it.playerType }.mapTo(listOfIds) { it.id!! } // Sort them by player type and get the ids.

                val match = Match(team1Name, team2Name, dateAndTime = epoch, team1TeamIds = listOfIds)
                if (tournamentName.isNotEmpty()) match.tournamentName = tournamentName
                if (locationName.isNotEmpty()) match.locationName = locationName

                matchRepository.addMatchData(match, team1ImageUri.value, team2ImageUri.value!!).collect {
                    _matchUploadResult.postValue(it)
                }

            }
        }
    }

    fun updateMatchData(match: Match, team: List<Player>) {
        viewModelScope.launch(Dispatchers.Default) {
            val date = dateChannel.poll()
            val time = timeChannel.poll()

            Timber.d("Date: $date Time: $time")
            val zoneId = ZoneId.systemDefault()
            val matchDateTIme = match.dateAndTime!!
            val dateTime: LocalDateTime? = when {
                date != null && time == null -> {
                    val localTime = getZonedDateTime(matchDateTIme, zoneId).toLocalTime()
                    date.atTime(localTime)!!
                }
                time != null && date == null -> {
                    val localDate = getZonedDateTime(matchDateTIme, zoneId).toLocalDate()
                    localDate.atTime(time)
                }
                time != null && date != null -> {
                    date.atTime(time)
                }
                else -> null
            }

            val listOfIds = arrayListOf<String>()
            team.sortedBy { it.playerType }.mapTo(listOfIds) { it.id!! } // Sort them by player type and get the ids.

            /**
             * If its null, the previous data will not be changed.
             */
            if (dateTime != null) {
                match.dateAndTime = dateTime.atZone(zoneId).toEpochSecond()
            }

            match.team1TeamIds = listOfIds

            Timber.d("${match.dateAndTime}")
            Timber.d("${team1ImageUri.value}, ${team2ImageUri.value}")
            matchRepository.updateMatchData(match, team1ImageUri.value, team2ImageUri.value)
                .collect {
                    _matchUpdateResult.postValue(it)
                }
        }
    }

    fun getZonedDateTime(matchDateTime: Long, zoneId: ZoneId): ZonedDateTime = Instant.ofEpochSecond(matchDateTime).atZone(zoneId)
}