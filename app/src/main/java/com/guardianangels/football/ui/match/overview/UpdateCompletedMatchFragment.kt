package com.guardianangels.football.ui.match.overview

import android.os.Bundle
import android.view.View
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
import com.guardianangels.football.util.getString
import com.guardianangels.football.util.toEmptySafeInt
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class UpdateCompletedMatchFragment : Fragment(R.layout.update_completed_match_fragment) {

    private val args: UpdateCompletedMatchFragmentArgs by navArgs()

    private var _binding: UpdateCompletedMatchFragmentBinding? = null
    val binding: UpdateCompletedMatchFragmentBinding get() = _binding!!

    private val viewModel: UpdateCompletedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val match = args.match
        _binding = UpdateCompletedMatchFragmentBinding.bind(view)
        val doneButton = binding.gotoPlayerStatsButton

        setLogos(match)

        if (match.team1TeamIds.isNullOrEmpty()) {
            doneButton.text = getString(R.string.done)
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

        val navController = findNavController()
        binding.backButton.setOnClickListener { navController.popBackStack() }
        observeViewModel(navController)
    }


    private fun observeViewModel(navController: NavController) {
        viewModel.listOfTeamIds.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("Loading")
                is NetworkState.Success -> {
                    val ids = it.data!!
                    if (!ids.isNullOrEmpty()) {
                        Timber.d("Not empty, moving to update players fragment")
                        val isWin = binding.team1ScoreET.getString().toInt() > binding.team2ScoreET.getString().toInt()
                        navController.navigate(
                            UpdateCompletedMatchFragmentDirections.actionUpdateCompletedMatchFragmentToUpdateTeamPlayersFragment(ids.toTypedArray(), isWin)
                        )
                    } else {
                        Timber.d("Go back to home fragment and reload completed matches and scores")
                        navController.navigate(UpdateCompletedMatchFragmentDirections.actionUpdateCompletedMatchFragmentToHome(true))
                    }
                }

                is NetworkState.Failed -> {
                    Timber.d("${it.exception}, ${it.message}")
                }
            }
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}