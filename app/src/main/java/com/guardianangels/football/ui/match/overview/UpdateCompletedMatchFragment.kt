package com.guardianangels.football.ui.match.overview

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.guardianangels.football.R
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.UpdateCompletedMatchFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants
import com.guardianangels.football.util.getString
import com.guardianangels.football.util.toEmptySafeInt
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Fragment where the match is set as Completed with aditional information.
 * This fragment can be called again from CompletedMatchDetailsFragment for editing the values.
 */
@AndroidEntryPoint
class UpdateCompletedMatchFragment : Fragment(R.layout.update_completed_match_fragment) {

    private val args: UpdateCompletedMatchFragmentArgs by navArgs()

    private var _binding: UpdateCompletedMatchFragmentBinding? = null
    val binding: UpdateCompletedMatchFragmentBinding get() = _binding!!

    private val viewModel: UpdateCompletedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var match = args.match
        _binding = UpdateCompletedMatchFragmentBinding.bind(view)
        val doneButton = binding.gotoPlayerStatsButton

        val navController = findNavController()
        val isPreviousCompletedMatchFragment = navController.previousBackStackEntry?.destination?.id == R.id.completedMatchDetailsFragment
        val done = getString(R.string.done)
        if (isPreviousCompletedMatchFragment) {
            doneButton.text = done
        }

        setLogos(match)
        setDataForUpdate(match)

        if (match.team1TeamIds.isNullOrEmpty()) {
            doneButton.text = done
        }

        doneButton.setOnClickListener {
            val team1ScoreText = binding.team1ScoreET.getString()
            val team2ScoreText = binding.team2ScoreET.getString()

            if (team1ScoreText.isNotEmpty() && team2ScoreText.isNotEmpty()) {

                val team1Score = team1ScoreText.toInt()
                val team2Score = team2ScoreText.toInt()

                val notes = binding.matchNotesET.getString()

                val team1ShootingStats = binding.shootingET.getString().toEmptySafeInt()
                val team2ShootingStats = binding.shootingET2.getString().toEmptySafeInt()
                val team1AttactStats = binding.attackET.getString().toEmptySafeInt()
                val team2AttactStats = binding.attackET2.getString().toEmptySafeInt()
                val team1PossesionStats = binding.possesionsET.getString().toEmptySafeInt()
                val team2PossesionStats = binding.possesionsET2.getString().toEmptySafeInt()
                val team1CardStats = binding.cardsET.getString().toEmptySafeInt()
                val team2CardStats = binding.cardsET2.getString().toEmptySafeInt()
                val team1CornerStats = binding.cornersET.getString().toEmptySafeInt()
                val team2CornerStats = binding.cornersET2.getString().toEmptySafeInt()

                Timber.d("$team1Score, $team2Score, $team1ShootingStats, $team2ShootingStats...")

                /** First update the Game Stats and then return the match after setting the match result. */
                match = viewModel.updateGameStats(team1Score, team2Score, match)
                Timber.d("Match result: ${match.gameResult}")

                viewModel.updateMatch(
                    match,
                    team1Score, team2Score,
                    team1ShootingStats, team2ShootingStats,
                    team1AttactStats, team2AttactStats,
                    team1PossesionStats, team2PossesionStats,
                    team1CardStats, team2CardStats,
                    team1CornerStats, team2CornerStats,
                    notes
                )
            } else {
                Toast.makeText(requireContext(), "Please Enter The Team Scores", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backButton.setOnClickListener { navController.popBackStack() }
        observeViewModel(navController, isPreviousCompletedMatchFragment)
    }


    private fun observeViewModel(navController: NavController, isPreviousCompletedMatchDetailsFragment: Boolean) {
        viewModel.listOfTeamIds.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    /** If the previous fragment is CompletedMatchDetailsFragment, do nothing an and popBack to it.*/
                    if (isPreviousCompletedMatchDetailsFragment) {
                        navController.navigate(
                            UpdateCompletedMatchFragmentDirections.actionUpdateCompletedMatchFragmentToCompletedMatchDetailsFragment(
                                viewModel.completedMatch.value
                            )
                        )
                    } else {
                        val ids = it.data!!
                        if (!ids.isNullOrEmpty()) {
                            Timber.d("Not empty, moving to update players fragment")
                            val isWin = binding.team1ScoreET.getString().toInt() > binding.team2ScoreET.getString().toInt()
                            navController.navigate(
                                UpdateCompletedMatchFragmentDirections.actionUpdateCompletedMatchFragmentToUpdateTeamPlayersFragment(
                                    ids.toTypedArray(),
                                    isWin
                                )
                            )
                        } else {
                            Timber.d("Go back to home fragment and reload completed matches and scores")
                            navController.navigate(UpdateCompletedMatchFragmentDirections.actionUpdateCompletedMatchFragmentToHome(true))
                        }
                    }
                    navController.getBackStackEntry(R.id.home).savedStateHandle.set(Constants.RELOAD_PREVIOUS_MATCHES_KEY, true)
                }
                is NetworkState.Failed -> {
                    Timber.d("${it.exception}, ${it.message}")
                }
            }
        }

        viewModel.updateGameStats.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    /* TODO: Not working as intended. It may not be set at all when the match is edited and updated a second time.
                         Prevents loading GameStats on HomeFragment without restart.*/
                    Timber.tag("Observe updateGameStats").d("RELOAD_GAME_STATS_KEY set to true.")
                    navController.getBackStackEntry(R.id.home).savedStateHandle.set(Constants.RELOAD_GAME_STATS_KEY, true)
                    Toast.makeText(requireContext(), "Updated Game Stats", Toast.LENGTH_SHORT).show()
                }
                is NetworkState.Failed -> {
                    Timber.d("${it.exception}, ${it.message}")
                }
            }
        }
    }

    private fun setDataForUpdate(match: Match) {
        if (match.team1Score != null && match.team2Score != null) {
            binding.team1ScoreET.setNumber(match.team1Score)
            binding.team2ScoreET.setNumber(match.team2Score)
            binding.matchNotesET.setText(match.matchNotes)
            binding.shootingET.setNumber(match.team1ShootingStats)
            binding.shootingET2.setNumber(match.team2ShootingStats)
            binding.attackET.setNumber(match.team1AttactStats)
            binding.attackET2.setNumber(match.team2AttackStats)
            binding.possesionsET.setNumber(match.team1PossesionStats)
            binding.possesionsET2.setNumber(match.team2PossesionStats)
            binding.cardsET.setNumber(match.team1CardStats)
            binding.cardsET2.setNumber(match.team2CardStats)
            binding.cornersET.setNumber(match.team1CornerStats)
            binding.cornersET2.setNumber(match.team2CornerStats)
        }
    }


    private fun setLogos(match: Match) {
        val team1Logo = match.team1Logo
        val team2Logo = match.team2Logo
        if (team1Logo!!.isNotEmpty())
            binding.team1Logo.load(team1Logo)
        else
            setNoLogo(match.team1Name!!)

        if (team2Logo!!.isNotEmpty())
            binding.team2Logo.load(team2Logo)
        else
            setNoLogo(match.team2Name!!)
    }

    private fun setNoLogo(teamName: String) {
        if (teamName == getString(R.string.guardian_angels)) {
            binding.team1Logo.load(R.drawable.gaurdian_angels)
        } else {
            binding.team1Logo.load(R.drawable.ic_football)
        }
    }

    private fun EditText.setNumber(number: Int?) {
        number?.let {
            this.setText(number.toString())
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}