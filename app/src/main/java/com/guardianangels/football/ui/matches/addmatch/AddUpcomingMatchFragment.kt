package com.guardianangels.football.ui.matches.addmatch

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.guardianangels.football.R
import com.guardianangels.football.data.Player
import com.guardianangels.football.databinding.AddUpcomingMatchFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.ui.matches.AddUpcomingMatchFragmentDirections
import com.guardianangels.football.ui.matches.SelectedPlayerListAdapter
import com.guardianangels.football.util.Constants.PLAYER_SELECTED_KEY
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddUpcomingMatchFragment : Fragment(R.layout.add_upcoming_match_fragment) {
    companion object {
        @Suppress("SpellCheckingInspection", "unused")
        private const val TAG = "AddUpcomingMatchFgmt"
    }

    private val viewModel: AddUpcomingViewModel by viewModels()

    /**
     * Select images for team logos.
     */
    private val pickImagesForTeam1 = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { viewModel.team1Image(it) }
    }

    private val pickImagesForTeam2 = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { viewModel.team2Image(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = AddUpcomingMatchFragmentBinding.bind(view)

        val dateEditText = binding.dateET
        val timeEditText = binding.timeET
        val team2Logo = binding.team2Image

        dateEditText.toDatePicker(parentFragmentManager)
        timeEditText.toTimePicker(parentFragmentManager)

        val recyclerView = binding.selectedPlayerRecyclerView
        val adapter = SelectedPlayerListAdapter()
        recyclerView.adapter = adapter

        val navController = findNavController()

        /**
         * Get the players selected to be in the match.
         * This can be null if the user doesn't select anyone.
         * The result is passed back from MatchPlayerListFragment.
         */
        var team: List<Player> = emptyList()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<List<Player>>(PLAYER_SELECTED_KEY)
            ?.observe(viewLifecycleOwner) { result ->
                if (result.isNotEmpty()) {
                    binding.selectedPlayersTitle.visibility = View.VISIBLE
                    binding.count.apply {
                        visibility = View.VISIBLE
                        text = result.size.toString()
                    }
                }
                adapter.submitList(result)
                team = result
            }


        binding.doneButton.setOnClickListener {
            val team1Name = binding.team1NameET.text.toString().trim()
            val team2Name = binding.team2NameET.text.toString().trim()
            val date = dateEditText.text.toString()
            val time = timeEditText.text.toString()
            if (team1Name.isNotEmpty()
                && team2Name.isNotEmpty()
                && team2Logo.drawable != null
                && date.isNotEmpty() && time.isNotEmpty()
            ) {
                viewModel.addMatchData(team1Name, binding.team2NameET.text.toString(), team)
                resetViews(binding)
                team = emptyList() // Drop previous team
                adapter.submitList(team)
            } else {
                when {
                    team1Name.isEmpty() || team2Name.isEmpty() -> showToast("Please enter the Team Name")
                    team2Logo.drawable == null -> showToast("Please select the Team Logo")
                    date.isEmpty() -> showToast("Please select the Date")
                    time.isEmpty() -> showToast("Please select the Time")
                }
            }
        }

        /**
         *  Navigate to player fragment to select players playing for the match
         */
        binding.selectTeamButton.setOnClickListener {
            /* If players were selected previously, pass them to the fragment */
            navController.navigate(AddUpcomingMatchFragmentDirections.actionAddUpcomingMatchFragmentToMatchPlayerListFragment(team.toTypedArray()))
        }

        /**
         * Get images from the storage
         */
        binding.team1Image.setOnClickListener {
            pickImagesForTeam1.launch("image/*")
        }

        team2Logo.setOnClickListener {
            pickImagesForTeam2.launch("image/*")
        }

        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }

        observeViewModel(binding)
    }

    private fun observeViewModel(binding: AddUpcomingMatchFragmentBinding) {
        viewModel.team1ImageUri.observe(viewLifecycleOwner) {
            it?.let {
                binding.team1Image.load(it)
            }
        }

        viewModel.team2ImageUri.observe(viewLifecycleOwner) {
            it?.let {
                binding.team2Image.load(it)
            }
        }

        viewModel.matchUploadResult.observe(viewLifecycleOwner) {
            when(it) {
                is NetworkState.Loading -> Timber.tag(TAG).d("Loading")
                is NetworkState.Success -> showToast("Complete")
                is NetworkState.Failed -> {
                    Timber.d(it.message)
                    showToast(it.message)
                }
            }
        }
    }

    private fun resetViews(binding: AddUpcomingMatchFragmentBinding) {
        binding.team1NameET.setText(getString(R.string.guardian_angels))
        binding.team2NameET.setText("")
        binding.dateET.setText("")
        binding.timeET.setText("")
        binding.team1Image.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.gaurdian_angels))
        binding.team2Image.setImageDrawable(null)
    }

    /**
     * Select the date
     */
    private fun TextInputEditText.toDatePicker(parentFragmentManager: FragmentManager) {

        // Without this you'd have to double tap to show the dialog
        setOnFocusChangeListener { _, hasFocus -> if (hasFocus) callOnClick() }

        setOnClickListener {
            MaterialDatePicker.Builder.datePicker().apply {
                setTitleText("Select Match Date")
            }.build().also {
                it.addOnPositiveButtonClickListener { selected ->
                    val localDate = Instant.ofEpochMilli(selected).atZone(ZoneId.systemDefault()).toLocalDate()

                    setText(localDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")))
                    viewModel.setDate(localDate)
                }
            }.show(parentFragmentManager, "Material Date Picker")
        }
    }

    /**
     * Select the time
     */
    @SuppressLint("SetTextI18n")
    private fun TextInputEditText.toTimePicker(parentFragmentManager: FragmentManager) {

        setOnFocusChangeListener { _, hasFocus -> if (hasFocus) callOnClick() }

        setOnClickListener {
            val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .build()

            picker.addOnPositiveButtonClickListener {
                val time24 = String.format("%02d:%02d", picker.hour, picker.minute)
                val time12 = LocalTime.parse(time24, DateTimeFormatter.ofPattern("HH:mm"))
                val timeString = time12.format(DateTimeFormatter.ofPattern("hh:mm a"))

                setText(timeString)
                viewModel.setTime(time12)
            }

            picker.show(parentFragmentManager, "Material Time Picker")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}