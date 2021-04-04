package com.guardianangels.football.ui.match.upcomingmatches

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.guardianangels.football.R
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.MatchListFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants.BUNDLE_MATCH_UPLOAD_COMPLETE
import com.guardianangels.football.util.Constants.MATCH_DELETED_RESULT_KEY
import com.guardianangels.football.util.Constants.REQUEST_MATCH_UPLOAD_COMPLETE_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MatchListFragment : Fragment(R.layout.match_list_fragment) {

    private val viewModel: MatchListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = MatchListFragmentBinding.bind(view)
        val navController = findNavController()
        listenForFragmentResults(navController)

        val recyclerView = binding.recyclerview
        val adapter = UpcomingMatchListAdapter(::navigateToDetailsFragment)

        recyclerView.adapter = adapter

        if (viewModel.isUserLoggedIn) {
            binding.addMatchButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    navController.navigate(MatchListFragmentDirections.actionMatchListFragmentToAddUpcomingMatchFragment())
                }
            }
        }

        observeViewModel(adapter, binding)
    }

    private fun navigateToDetailsFragment(match: Match) {
        findNavController().navigate(MatchListFragmentDirections.actionMatchListFragmentToMatchDetailsFragment(match))
    }

    private fun observeViewModel(adapter: UpcomingMatchListAdapter, binding: MatchListFragmentBinding) {
        // Delay to wait for fragment transition.
        lifecycleScope.launch {
            delay(200)
            Timber.d("Load Matches after 200 ms")
            viewModel.upcomingMatch.observe(viewLifecycleOwner) {
                when (it) {
                    is NetworkState.Loading -> {
                        binding.progressBar.show()
                    }
                    is NetworkState.Success -> {
                        binding.progressBar.hide()
                        adapter.submitList(it.data)
                    }
                    is NetworkState.Failed -> {
                        binding.progressBar.hide()
                        showToast(it.message)
                        Timber.d(it.message)
                    }
                }
            }
        }
    }

    private fun listenForFragmentResults(navController: NavController) {
        /**
         * Reload the list if match was added or updated
         */
        setFragmentResultListener(REQUEST_MATCH_UPLOAD_COMPLETE_KEY) { _, bundle ->
            val result = bundle.getBoolean(BUNDLE_MATCH_UPLOAD_COMPLETE)
            if (result) {
                viewModel.getUpcomingMatches()
            }
        }

        /**
         * Reload if match was deleted
         */
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(MATCH_DELETED_RESULT_KEY)
            ?.observe(viewLifecycleOwner) {
                if (it) viewModel.getUpcomingMatches()
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}