package com.guardianangels.football.ui.match.previousmatches

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.guardianangels.football.R
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.PreviousMatchListFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PreviousMatchListFragment : Fragment(R.layout.previous_match_list_fragment) {

    private val viewModel: PreviousMatchListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = PreviousMatchListFragmentBinding.bind(view)

        val navController = findNavController()

        val adapter = PreviousMatchesAdapter(::onMatchClick)
        binding.recyclerview.adapter = adapter


        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }

        /**
         * Get the previous completed matches.
         */
        viewModel.previousCompletedMatches.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.tag("previousCompleted").d("Loading")
                is NetworkState.Success -> {
                    if (it.data.isNotEmpty()) adapter.submitList(it.data)
                }
                is NetworkState.Failed -> {
                    Timber.tag("previousCompleted").d("${it.exception}")
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(Constants.PREVIOUS_MATCH_DELETED_RESULT_KEY)
            ?.observe(viewLifecycleOwner) {
                if (it) {
                    viewModel.getPreviousMatches()
                    Timber.d("Match deleted reloading list.")
                }
            }
    }


    /**
     * Get the onClcik data from the previous matches list.
     * Navigate to completedMatchFragment
     */
    private fun onMatchClick(match: Match) {
        Timber.d("Names: [${match.team1Name}, ${match.team2Name}], Scores: [${match.team1Score}, ${match.team2Score}]")
        findNavController().navigate(PreviousMatchListFragmentDirections.actionPreviousMatchListFragmentToCompletedMatchDetailsFragment(match))
    }
}