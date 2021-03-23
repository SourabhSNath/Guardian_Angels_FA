package com.guardianangels.football.ui.match.completedmatches

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.guardianangels.football.R
import com.guardianangels.football.databinding.CompletedMatchDetailsFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.ui.match.MatchTeamListAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class CompletedMatchDetailsFragment : Fragment(R.layout.completed_match_details_fragment) {

    private val detailsViewModel: CompletedMatchDetailsViewModel by viewModels()
    private val args: CompletedMatchDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = CompletedMatchDetailsFragmentBinding.bind(view)

        val match = args.match

        /**
         * Logos
         */
        if (match.team1Logo!!.isNotEmpty()) {
            binding.team1Logo.load(match.team1Logo)
        } else {
            if (match.team1Name == getString(R.string.guardian_angels))
                binding.team1Logo.load(R.drawable.gaurdian_angels)
            else
                binding.team1Logo.load(R.drawable.ic_football)
        }

        if (match.team2Logo!!.isNotEmpty()) {
            binding.team2Logo.load(match.team2Logo)
        } else {
            if (match.team2Name == getString(R.string.guardian_angels))
                binding.team2Logo.load(R.drawable.gaurdian_angels)
            else
                binding.team2Logo.load(R.drawable.ic_football)
        }

        /**
         * Score
         */
        binding.team1Score.text = match.team1Score.toString()
        binding.team2Score.text = match.team2Score.toString()

        /**
         * Team Names
         */
        binding.team1Name.text = match.team1Name
        binding.team2Name.text = match.team2Name

        /**
         * Convert to Date and Time
         */
        val zonedDateAndTime = Instant.ofEpochSecond(match.dateAndTime!!).atZone(ZoneId.systemDefault())
        val localDateTime = zonedDateAndTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
        binding.matchDate.text = localDateTime

        /**
         * Location
         */
        if (match.locationName!!.isNotEmpty())
            binding.locationTV.text = match.locationName
        else {
            binding.locationTitle.visibility = View.GONE
            binding.locationTV.visibility = View.GONE
        }

        /**
         * Tounrament
         */
        if (match.tournamentName!!.isNotEmpty())
            binding.tournamentTV.text = match.tournamentName

        /**
         * Notes / Summary
         */
        if (match.matchNotes!!.isNotEmpty()) {
            binding.summaryBody.text = match.matchNotes
        } else {
            binding.summaryTitle.visibility = View.GONE
            binding.summaryBody.visibility = View.GONE
        }

        /**
         * Match Details
         */
        val shootStat1 = match.team1ShootingStats
        val shootStat2 = match.team2ShootingStats
        val attack1 = match.team1AttactStats
        val attack2 = match.team2AttackStats
        val poss1 = match.team1PossesionStats
        val poss2 = match.team2PossesionStats
        val card1 = match.team1CardStats
        val card2 = match.team2CardStats
        val corner1 = match.team1CornerStats
        val corner2 = match.team2CornerStats

        val shootVisibility = if (shootStat1 == null && shootStat2 == null) true else shootStat1 == 0 && shootStat2 == 0
        val attackVisibiltiy = if (attack1 == null && attack2 == null) true else attack1 == 0 && attack2 == 0
        val possessVisibility = if (poss1 == null && poss2 == null) true else poss1 == 0 && poss2 == 0
        val cardsVisibility = if (card1 == null && card2 == null) true else card1 == 0 && card2 == 0
        val cornerVisibility = if (corner1 == null && corner2 == null) true else corner1 == 0 && corner2 == 0

        if (shootVisibility) {
            binding.shootingTeam1.visibility = View.GONE
            binding.shootinTeam2.visibility = View.GONE
            binding.shootingTitle.visibility = View.GONE
        } else {
            binding.shootingTeam1.text = shootStat1.toString()
            binding.shootinTeam2.text = shootStat2.toString()
        }

        if (attackVisibiltiy) {
            binding.attacksTeam1.visibility = View.GONE
            binding.attacksTeam2.visibility = View.GONE
            binding.attacksTitle.visibility = View.GONE
        } else {
            binding.attacksTeam1.text = attack1.toString()
            binding.attacksTeam2.text = attack2.toString()
        }

        if (possessVisibility) {
            binding.possesionsTeam2.visibility = View.GONE
            binding.possesionsTeam1.visibility = View.GONE
            binding.possesionsTitle.visibility = View.GONE
        } else {
            binding.possesionsTeam1.text = poss1.toString()
            binding.possesionsTeam2.text = poss2.toString()
        }

        if (cardsVisibility) {
            binding.cardsTeam2.visibility = View.GONE
            binding.cardsTeam1.visibility = View.GONE
            binding.cardsTitle.visibility = View.GONE
        } else {
            binding.cardsTeam1.text = card1.toString()
            binding.cardsTeam2.text = card2.toString()
        }

        if (cornerVisibility) {
            binding.cornersTeam1.visibility = View.GONE
            binding.cornersTeam2.visibility = View.GONE
            binding.cornersTitle.visibility = View.GONE
        } else {
            binding.cornersTeam1.text = corner1.toString()
            binding.cornersTeam2.text = corner2.toString()
        }

        if (shootVisibility && attackVisibiltiy && possessVisibility && cardsVisibility && cornerVisibility) {
            binding.detailsTitle.visibility = View.GONE
        }

        /**
         * Player List
         */
        val recyclerView = binding.teamRecyclerView
        val adapter = MatchTeamListAdapter()
        recyclerView.adapter = adapter

        val team1Ids = match.team1TeamIds!!
        if (team1Ids.isNotEmpty()) {
            detailsViewModel.getPlayers(team1Ids)
        } else {
            binding.teamTitle.visibility = View.GONE
        }

        observeData(adapter)
        setUpButtons(binding)
    }

    private fun observeData(adapter: MatchTeamListAdapter) {
        /**
         * Get the match players
         */
        detailsViewModel.players.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    Timber.d("Success")
                    adapter.submitList(it.data)
                }
                is NetworkState.Failed -> {
                    Timber.d("${it.exception} ${it.message}")
                    Toast.makeText(requireContext(), "${it.exception}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * Observe to check if match has been deleted
         */
        detailsViewModel.isMatchDeleted.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    Toast.makeText(requireContext(), "Match deleted", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is NetworkState.Failed -> {
                    Timber.d("${it.exception} ${it.message}")
                    Toast.makeText(requireContext(), "${it.exception}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun setUpButtons(binding: CompletedMatchDetailsFragmentBinding) {

        val navController = findNavController()
        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }

        binding.editButton.setOnClickListener {
            navController.navigate(CompletedMatchDetailsFragmentDirections.actionCompletedMatchFragmentToUpdateCompletedMatchFragment(args.match))
        }

        binding.deleteButton.setOnClickListener {
            detailsViewModel.deleteMatch(args.match)
        }
    }
}