package com.guardianangels.football.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.card.MaterialCardView
import com.guardianangels.football.R
import com.guardianangels.football.data.GameResults
import com.guardianangels.football.data.Match
import com.guardianangels.football.databinding.HomeFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants.RELOAD_GAME_STATS_KEY
import com.guardianangels.football.util.Constants.RELOAD_NEXT_UPCOMING_KEY
import com.guardianangels.football.util.Constants.RELOAD_PREVIOUS_MATCHES_KEY
import com.guardianangels.football.util.setCardStroke
import com.guardianangels.football.util.setTeamLogo
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.home_fragment) {

    private val viewModel: HomeViewModel by viewModels()

    private var _binding: HomeFragmentBinding? = null
    private val binding: HomeFragmentBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = HomeFragmentBinding.bind(view)

        val navController = findNavController()

        /* The login fragment is accessed through the title. */
        binding.appTitle.setOnLongClickListener {
            navController.navigate(HomeFragmentDirections.actionHomeToLoginFragment())
            false
        }

        binding.moreUpcomingTV.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionHomeToMatchListFragment())
        }

        binding.morePreviousTV.setOnClickListener {
            navController.navigate(HomeFragmentDirections.actionHomeToPreviousMatchListFragment())
        }

        observeData(navController)
    }

    private fun observeData(navController: NavController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

        /**
         * Reload next upcoming match when a match is Added, Updated or Deleted.
         */
        savedStateHandle?.getLiveData<Boolean>(RELOAD_NEXT_UPCOMING_KEY)?.observe(viewLifecycleOwner) {
            if (it) {
                Timber.d("Reload UpcomingMatchCard")
                viewModel.getNextUpcomingMatch()
                navController.currentBackStackEntry?.savedStateHandle?.set(RELOAD_NEXT_UPCOMING_KEY, false) // Reset to false.
            }
        }

        /**
         * Reload the game stats when the stats are Changed.
         */
        savedStateHandle?.getLiveData<Boolean>(RELOAD_GAME_STATS_KEY)?.observe(viewLifecycleOwner) {
            if (it) {
                Timber.d("Reload Game Stats")
                viewModel.getGameStats()
                navController.currentBackStackEntry?.savedStateHandle?.set(RELOAD_GAME_STATS_KEY, false)
            }
        }

        /**
         * Reload previous match when a completed match is added, updated or deleted.
         */
        savedStateHandle?.getLiveData<Boolean>(RELOAD_PREVIOUS_MATCHES_KEY)?.observe(viewLifecycleOwner) {
            if (it) {
                Timber.d("Reload previous matches")
                viewModel.getPreviousMatch()
                navController.currentBackStackEntry?.savedStateHandle?.set(RELOAD_PREVIOUS_MATCHES_KEY, false)
            }
        }

        /**
         * Get the next upcoming match
         */
        viewModel.upcomingMatch.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> {
                    Timber.tag("upcomingMatch").d("Loading")
                    binding.loadingProgress.visibility = View.VISIBLE
                    binding.addAMatchTV.visibility = View.GONE
                    setUpcomingViewsVisibiltiy(View.GONE)
                }
                is NetworkState.Success -> {
                    setupUpComingMatchCard(it.data, navController)
                    binding.loadingProgress.visibility = View.GONE
                    binding.addAMatchTV.visibility = View.GONE
                    setUpcomingViewsVisibiltiy(View.VISIBLE)
                    hideOrShowUpcoming(hide = false)
                    hideEmptyUpcomingEventsMessageCard(false) // Hide No Events message card
                }
                is NetworkState.Failed -> {
                    Timber.tag("upcomingMatch").d("${it.exception}, ${it.message}, Hide everything")
                    binding.loadingProgress.visibility = View.GONE
                    if (it.exception is IndexOutOfBoundsException) {
                        if (viewModel.isUserLoggedIn()) {
                            binding.addAMatchTV.visibility = View.VISIBLE
                            binding.upcomingMatchCard.setOnClickListener {
                                Timber.d("Clicked Upcoming match card")
                                navController.navigate(HomeFragmentDirections.actionHomeToAddUpcomingMatchFragment())
                            }
                            setUpcomingViewsVisibiltiy(View.GONE)
                            hideOrShowUpcoming(hide = false)
                        } else {
                            hideOrShowUpcoming(true)
                        }
                    } else {
                        hideOrShowUpcoming(true)
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    // Show this if it fails to get the data. EmptyEvents Card is supposed to be shown when both upcoming and previous matches are empty
                    hideEmptyUpcomingEventsMessageCard(true)
                }
            }
        }

        /**
         * Get the game stats
         */
        viewModel.gameStats.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.tag("gameStats").d("Loading")
                is NetworkState.Success -> {
                    binding.gamesTV.text = it.data.games!!.toString()
                    binding.winsTV.text = it.data.wins!!.toString()
                    binding.drawsTV.text = it.data.draws!!.toString()
                    binding.lossTV.text = it.data.losses!!.toString()
                }
                is NetworkState.Failed -> {
                    Timber.tag("gameStats").d("${it.exception}")
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }


        viewModel.previousMatch.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.tag("previousCompleted").d("Loading")
                is NetworkState.Success -> {
                    // Hide the emptyEvents card if success. EmptyEvents Card is supposed to be shown when both upcoming and previous matches are empty
                    hideEmptyUpcomingEventsMessageCard(false)

                    binding.previousMatchTitle.visibility = View.VISIBLE
                    binding.previousMatchCard.root.visibility = View.VISIBLE
                    binding.morePreviousTV.visibility = View.VISIBLE

                    val item = it.data
                    val cardView = binding.previousMatchCard.card
                    setupPreviousMatchCard(item, cardView)

                    cardView.setOnClickListener {
                        navController.navigate(HomeFragmentDirections.actionHomeToCompletedMatchFragment(item))
                    }
                }
                is NetworkState.Failed -> {
                    binding.previousMatchTitle.visibility = View.GONE
                    binding.morePreviousTV.visibility = View.GONE
                    binding.previousMatchCard.card.visibility = View.GONE
                    Timber.tag("previousCompleted").d("${it.exception}")
                    if (it.exception !is NoSuchElementException) Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun hideOrShowUpcoming(hide: Boolean) {
        binding.upcomingMatchCard.visibility = if (hide) View.GONE else View.VISIBLE
        binding.upcomingTV.visibility = if (hide) View.GONE else View.VISIBLE
        binding.moreUpcomingTV.visibility = if (hide) View.GONE else View.VISIBLE
    }

    private fun hideEmptyUpcomingEventsMessageCard(visible: Boolean) {
        binding.emptyEventsCard.visibility = if (visible) {
            Timber.d("Show No events card")
            View.VISIBLE
        } else {
            Timber.d("Hide No events card")
            View.GONE
        }
    }

    /**
     * Setup the upcoming match card and set the clickListener on the card to navigate to detailsFragment.
     */
    private fun setupUpComingMatchCard(matchInfo: Match, navController: NavController) {
        binding.team1Logo.setTeamLogo(matchInfo.team1Logo!!, matchInfo.team1Name!!)
        binding.team2Logo.setTeamLogo(matchInfo.team2Logo!!, matchInfo.team2Name!!)

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

    private fun setupPreviousMatchCard(item: Match, cardView: MaterialCardView) {
        binding.previousMatchCard.team1.text = item.team1Name
        binding.previousMatchCard.team2.text = item.team2Name

        if (item.team1Logo!!.isNotEmpty()) {
            binding.previousMatchCard.team1Logo.load(item.team1Logo)
        } else {
            if (item.team1Name == requireContext().getString(R.string.guardian_angels))
                binding.previousMatchCard.team1Logo.load(R.drawable.gaurdian_angels)
            else
                binding.previousMatchCard.team1Logo.load(R.drawable.ic_football)
        }

        if (item.team2Logo!!.isNotEmpty()) {
            binding.previousMatchCard.team2Logo.load(item.team2Logo)
        } else {
            if (item.team2Name == requireContext().getString(R.string.guardian_angels))
                binding.previousMatchCard.team2Logo.load(R.drawable.gaurdian_angels)
            else
                binding.previousMatchCard.team2Logo.load(R.drawable.ic_football)
        }

        binding.previousMatchCard.team1Score.text = item.team1Score!!.toString()
        binding.previousMatchCard.team2Score.text = item.team2Score!!.toString()

        when (item.gameResult) {
            GameResults.WIN -> cardView.setCardStroke(requireContext(), R.color.mainStatWon)
            GameResults.LOSS -> cardView.setCardStroke(requireContext(), R.color.mainStatLost)
            GameResults.DRAW -> cardView.setCardStroke(requireContext(), R.color.mainStatDraw)
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