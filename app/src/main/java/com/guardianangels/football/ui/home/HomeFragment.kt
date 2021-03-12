package com.guardianangels.football.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.guardianangels.football.R
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.HomeFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants.RELOAD_NEXT_UPCOMING_KEY
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.home_fragment) {

    private val viewModel: HomeViewModel by viewModels()

    private var _binding: HomeFragmentBinding? = null
    private val binding: HomeFragmentBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = HomeFragmentBinding.bind(view)

        val navController = findNavController()

        observeData(navController)

        /* Hide the login fragment behind the title. */
        binding.appTitle.setOnLongClickListener {
            navController.navigate(HomeFragmentDirections.actionHomeToLoginFragment())
            false
        }

        binding.moreUpcomingTV.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionHomeToMatchListFragment())
        }

    }

    private fun observeData(navController: NavController) {

        /**
         * Reload next upcoming match when a match is Added, Updated or Deleted.
         */
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(RELOAD_NEXT_UPCOMING_KEY)?.observe(viewLifecycleOwner) {
            if (it) {
                Timber.d("Reload UpcomingMatchCard")
                viewModel.getNextUpcomingMatch()
                navController.currentBackStackEntry?.savedStateHandle?.set(RELOAD_NEXT_UPCOMING_KEY, false)
            }
        }

        /**
         * Get the next upcoming match
         */
        viewModel.upcomingMatch.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> {
                    Timber.d("Loading")
                    binding.loadingProgress.visibility = View.VISIBLE
                    binding.addAMatchTV.visibility = View.GONE
                    setUpcomingViewsVisibiltiy(View.GONE)
                }
                is NetworkState.Success -> {
                    setupUpComingMatchCard(it.data, navController)
                    binding.loadingProgress.visibility = View.GONE
                    binding.addAMatchTV.visibility = View.GONE
                    setUpcomingViewsVisibiltiy(View.VISIBLE)
                }
                is NetworkState.Failed -> {
                    Timber.d("${it.exception}, ${it.message}")
                    if (it.exception is IndexOutOfBoundsException) {
                        binding.loadingProgress.visibility = View.GONE
                        binding.addAMatchTV.visibility = View.VISIBLE
                        binding.upcomingMatchCard.setOnClickListener {
                            Timber.d("Clicked Upcoming match card")
                            navController.navigate(HomeFragmentDirections.actionHomeToAddUpcomingMatchFragment())
                        }
                        setUpcomingViewsVisibiltiy(View.GONE)
                    }
                }
            }
        }
    }

    /**
     * Setup the upcoming match card and set the clickListener on the card to navigate to detailsFragment.
     */
    private fun setupUpComingMatchCard(matchInfo: Match, navController: NavController) {
        val team1LogoLink = matchInfo.team1Logo
        if (team1LogoLink!!.isNotEmpty()) {
            binding.team1Logo.load(team1LogoLink)
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
            val localTime = "KO: " + zonedDateAndTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))

            binding.matchDate.text = localDate
            binding.matchKickOffTime.text = localTime
        }

        binding.upcomingMatchCard.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionHomeToMatchDetailsFragment(matchInfo))
        }
    }

    /**
     * Control the visibility of the imageViews and text.
     */
    private fun setUpcomingViewsVisibiltiy(isVisible: Int) {
        binding.bg.visibility = isVisible
        binding.team1Logo.visibility = isVisible
        binding.team2Logo.visibility = isVisible
        binding.team1TV.visibility = isVisible
        binding.team2TV.visibility = isVisible
        binding.vs.visibility = isVisible
        binding.matchDate.visibility = isVisible
        binding.matchKickOffTime.visibility = isVisible
        binding.moreUpcomingTV.visibility = isVisible
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}