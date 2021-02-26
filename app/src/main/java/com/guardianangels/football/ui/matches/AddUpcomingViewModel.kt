package com.guardianangels.football.ui.matches

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardianangels.football.data.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddUpcomingViewModel @Inject constructor() : ViewModel() {

    private val dateChannel = Channel<LocalDate>()
    private val timeChannel = Channel<LocalTime>()

    private val _team1ImageUri = MutableLiveData<Uri?>()
    val team1ImageUri: LiveData<Uri?> get() = _team1ImageUri

    private val _team2ImageUri = MutableLiveData<Uri>()
    val team2ImageUri: LiveData<Uri> get() = _team2ImageUri

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
        Timber.tag("AddUpcomingViewModel")
            .d("$team1Name, $team2Name, ${dateChannel.poll()}, ${timeChannel.poll()} , ${if (team.isEmpty()) "Players not selected" else team[0].playerName}, ${team1ImageUri.value}, ${team2ImageUri.value}")
    }


}