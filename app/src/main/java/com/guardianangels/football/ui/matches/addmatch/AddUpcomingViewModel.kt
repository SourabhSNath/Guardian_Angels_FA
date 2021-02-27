package com.guardianangels.football.ui.matches.addmatch

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Match
import com.guardianangels.football.data.Player
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddUpcomingViewModel @Inject constructor(private val matchRepository: MatchRepository) : ViewModel() {

    private val dateChannel = Channel<LocalDate>()
    private val timeChannel = Channel<LocalTime>()

    private val _team1ImageUri = MutableLiveData<Uri?>()
    val team1ImageUri: LiveData<Uri?> get() = _team1ImageUri

    private val _team2ImageUri = MutableLiveData<Uri>()
    val team2ImageUri: LiveData<Uri> get() = _team2ImageUri

    private val _matchUploadResult = MutableLiveData<NetworkState<Boolean>>()
    val matchUploadResult: LiveData<NetworkState<Boolean>> get() = _matchUploadResult

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

    fun addMatchData(team1Name: String, team2Name: String, team: List<Player>) {
        val date = dateChannel.poll()
        val time = timeChannel.poll()

        val dateTime = date?.atTime(time)!!
        val zoneID = ZoneId.systemDefault()
        val epoch: Long = dateTime.atZone(zoneID).toEpochSecond()

        Timber.tag("AddUpcomingViewModel")
            .d("$team1Name, $team2Name, $dateTime == $epoch, ${if (team.isEmpty()) "Players not selected" else team[0].playerName}")

        val listOfIds = arrayListOf<String>()
        team.forEach { listOfIds.add(it.id!!) }

        val match = Match(team1Name, team2Name, dateAndTime = epoch, team1TeamIds = listOfIds)

        viewModelScope.launch {
            matchRepository.addMatchData(match, team1ImageUri.value, team2ImageUri.value!!).collect {
                _matchUploadResult.value = it
            }
        }
    }


}