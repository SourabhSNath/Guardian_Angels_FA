package com.guardianangels.football.ui.match.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.guardianangels.football.R
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.MatchDetailsFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.ui.match.MatchTeamListAdapter
import com.guardianangels.football.util.Constants
import com.guardianangels.football.util.Constants.MATCH_DELETED_RESULT_KEY
import com.guardianangels.football.util.Constants.RELOAD_NEXT_UPCOMING_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MatchDetailsFragment : Fragment(R.layout.match_details_fragment) {

    private val args: MatchDetailsFragmentArgs by navArgs()
    private val viewModel: MatchDetailsViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = MatchDetailsFragmentBinding.bind(view)

        val navController = findNavController()

        var matchInfo = args.matchInfo
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Match>(Constants.MATCH_UPDATED_RESULT_KEY)
            ?.observe(viewLifecycleOwner) { result ->
                Timber.d("Reviced: ${result.team2Name}")
                matchInfo = result
            }

        if (!viewModel.isUserLoggedIn) {
            binding.editButton.visibility = View.GONE
            binding.deleteButton.visibility = View.GONE
            binding.matchCompleteButton.visibility = View.GONE
        } else {
            setupButtons(binding, navController, matchInfo)
        }

        val team1LogoLink = matchInfo.team1Logo
        if (team1LogoLink!!.isNotEmpty()) {
            binding.team1Logo.load(team1LogoLink)
            Timber.d("$team1LogoLink")
        } else {
            binding.team1Logo.load(R.drawable.gaurdian_angels)
        }

        val team2LogoLink = matchInfo.team2Logo
        if (team2LogoLink!!.isNotEmpty()) {
            binding.team2Logo.load(team2LogoLink)
        } else {
            binding.team2Logo.load(R.drawable.ic_football)
        }

        binding.team1TV.text = matchInfo.team1Name
        binding.team2TV.text = matchInfo.team2Name

        val dateTime = matchInfo.dateAndTime
        if (dateTime != null) {
            val zonedDateAndTime = Instant.ofEpochSecond(dateTime).atZone(ZoneId.systemDefault())

            val localDate = zonedDateAndTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            val localTime = zonedDateAndTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))

            binding.matchDate.text = localDate
            binding.matchKickOffTime.text = localTime
        }

        val tournamentName = matchInfo.tournamentName!!
        if (tournamentName.isNotEmpty()) {
            binding.tournamentTV.text = tournamentName
        } else {
            binding.tournamentTV.text = "Undecided"
        }

        val location = matchInfo.locationName!!
        if (location.isNotEmpty())
            binding.locationTV.text = matchInfo.locationName
        else
            binding.locationTV.text = "Undecided"


        val team1Ids = matchInfo.team1TeamIds!!
        if (team1Ids.isNotEmpty()) {
            viewModel.getPlayers(team1Ids)
        } else {
            binding.teamTitle.visibility = View.GONE
        }

        val adapter = MatchTeamListAdapter()
        binding.recyclerview.adapter = adapter

        viewModel.players.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    lifecycleScope.launch {
                        /* Delay for reducing lag during fragment transition animation */
                        delay(200)
                        Timber.d("Success")
                        adapter.submitList(it.data)
                    }
                }
                is NetworkState.Failed -> {
                    Timber.d(it.message)
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }


        viewModel.deleteState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    Toast.makeText(requireContext(), "Match has been deleted", Toast.LENGTH_SHORT).show()
                    findNavController().apply {
                        previousBackStackEntry?.savedStateHandle?.set(MATCH_DELETED_RESULT_KEY, it.data)
                        getBackStackEntry(R.id.home).savedStateHandle.set(RELOAD_NEXT_UPCOMING_KEY, true)
                        popBackStack()
                    }
                }
                is NetworkState.Failed -> {
                    Timber.d("deleteState: ${it.message}")
                    Toast.makeText(requireContext(), "Failed Deleting : ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }
    }

    private fun setupButtons(
        binding: MatchDetailsFragmentBinding,
        navController: NavController,
        matchInfo: Match
    ) {
        binding.editButton.setOnClickListener {
            navController.navigate(MatchDetailsFragmentDirections.actionMatchDetailsFragmentToAddUpcomingMatchFragment(matchInfo))
        }

        binding.deleteButton.setOnClickListener {
            deleteConfirmation(matchInfo)
        }

        binding.matchCompleteButton.setOnClickListener {
            navController.navigate(MatchDetailsFragmentDirections.actionMatchDetailsFragmentToUpdateCompletedMatchFragment(matchInfo))
        }
    }

    private fun deleteConfirmation(match: Match) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Match will be permanently deleted.")
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteMatch(match)
                dialog.dismiss()
            }
            .show()
    }
}