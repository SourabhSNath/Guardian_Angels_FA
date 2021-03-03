package com.guardianangels.football.ui.matches

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import com.guardianangels.football.R
import com.guardianangels.football.databinding.MatchDetailsFragmentBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MatchDetailsFragment : Fragment(R.layout.match_details_fragment) {

    private val args: MatchDetailsFragmentArgs by navArgs()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = MatchDetailsFragmentBinding.bind(view)

        val matchInfo = args.matchInfo

        val team1LogoLink = matchInfo.team1Logo
        if (team1LogoLink!!.isNotEmpty()) {
            binding.team1Logo.load(matchInfo.team1Logo)
        } else {
            binding.team1Logo.load(R.drawable.gaurdian_angels)
        }

        binding.team2Logo.load(matchInfo.team2Logo)
        binding.team1Name.text = matchInfo.team1Name
        binding.team2Name.text = matchInfo.team2Name

        val location = matchInfo.locationName!!
        if (location.isNotEmpty())
            binding.location.text = matchInfo.locationName
        else
            binding.location.text = "Undecided"

        val tournamentName = matchInfo.tournamentName!!
        if (tournamentName.isNotEmpty()) {
            binding.tournamentTitle.text = tournamentName
        } else {
            binding.tournamentTitle.text = getString(R.string.upcoming)
        }

        val dateTime = matchInfo.dateAndTime
        if (dateTime != null) {
            val zonedDateAndTime = Instant.ofEpochSecond(dateTime).atZone(ZoneId.systemDefault())
            val formattedDateTime = zonedDateAndTime.format(DateTimeFormatter.ofPattern("EEE dd MMM yyyy, hh:mm a"))
            binding.date.text = formattedDateTime
        }

        val team1 = matchInfo.team1TeamIds!!
        if (team1.isNotEmpty()) {
            // TODO
        } else {
            // TODO
        }
    }
}