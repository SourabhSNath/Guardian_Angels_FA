package com.guardianangels.football.ui.match.addmatch

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.guardianangels.football.R
import com.guardianangels.football.data.Match
import com.guardianangels.football.data.Player
import com.guardianangels.football.databinding.AddUpcomingMatchFragmentBinding
import com.guardianangels.football.network.NetworkState
import com.guardianangels.football.util.Constants.BUNDLE_MATCH_UPLOAD_COMPLETE
import com.guardianangels.football.util.Constants.MATCH_UPDATED_RESULT_KEY
import com.guardianangels.football.util.Constants.PLAYER_SELECTED_KEY
import com.guardianangels.football.util.Constants.RELOAD_NEXT_UPCOMING_KEY
import com.guardianangels.football.util.Constants.REQUEST_MATCH_UPLOAD_COMPLETE_KEY
import com.guardianangels.football.util.getString
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
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
    private val args: AddUpcomingMatchFragmentArgs by navArgs()

    private var adapter = SelectedPlayerListAdapter()
    private var _binding: AddUpcomingMatchFragmentBinding? = null
    private val binding get() = _binding!!

    private var team: List<Player> = emptyList()

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

        Timber.d("OnViewCreated")
        _binding = AddUpcomingMatchFragmentBinding.bind(view)

        val matchData = args.matchData
        var editMode = false
        if (matchData != null) {
            editMode = true
            setUpFields(matchData)
        }


        val dateEditText = binding.dateET
        val timeEditText = binding.timeET
        val team2Logo = binding.team2Image
        val team1 = binding.team1NameET

        dateEditText.toDatePicker(parentFragmentManager)
        timeEditText.toTimePicker(parentFragmentManager)

        val recyclerView = binding.selectedPlayerRecyclerView
        /*val adapter = SelectedPlayerListAdapter()*/
        recyclerView.adapter = adapter

        val navController = findNavController()

        /**
         * Get the players selected to be in the match.
         * This can be null if the user doesn't select anyone.
         * The result is passed back from MatchPlayerListFragment.
         */
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
            val team1Name = team1.getString()
            val team2Name = binding.team2NameET.getString()
            val date = dateEditText.text.toString()
            val time = timeEditText.text.toString()
            val tournamentName = binding.tournamentNameET.getString()
            val locationName = binding.locationET.getString()
            Timber.d("Done Button clicked")
            if (team1Name.isNotEmpty()
                && team2Name.isNotEmpty()
                && team2Logo.drawable != null
                && date.isNotEmpty() && time.isNotEmpty()
            ) {
                if (editMode) {
                    Timber.d("Done Button clicked: Edit Mode")
                    matchData!!.apply {
                        this.team1Name = team1Name
                        this.team2Name = team2Name
                        if (tournamentName.isNotEmpty()) this.tournamentName = tournamentName
                        if (locationName.isNotEmpty()) this.locationName = locationName
                    }
                    viewModel.updateMatchData(matchData, team)
                } else {
                    Timber.d("Done Button clicked: Add Mode")
                    viewModel.addMatchData(team1Name, team2Name, tournamentName, locationName, team)
                    observeAddedResultLiveData()
                }
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
         * Disable team selection if it isn't guardian angels
         */
        val selectTeamButton = binding.selectTeamButton
        disableButtonIfTeam1NotGA(team1, selectTeamButton)

        /**
         *  Navigate to player fragment to select players playing for the match
         */
        selectTeamButton.setOnClickListener {
            /* If players were selected previously, pass them to the fragment */
            Timber.d("${team.size} ${team.map { it.playerName }}")
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

        observeViewModel()
    }

    private fun observeAddedResultLiveData() {
        viewModel.matchUploadResult.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> {
                    Timber.tag(TAG).d("Add result Loading")
                    setProgressVisibility(View.VISIBLE)
                }
                is NetworkState.Success -> {
                    setUpFields()
                    setProgressVisibility(View.GONE)
                    team = emptyList() // Drop previous team
                    adapter.submitList(team)

                    /* Send a message to matchListFragment that a player has been added, so that it can refresh the list */
                    setFragmentResult(REQUEST_MATCH_UPLOAD_COMPLETE_KEY, bundleOf(BUNDLE_MATCH_UPLOAD_COMPLETE to true))
                    findNavController().getBackStackEntry(R.id.home).savedStateHandle.set(RELOAD_NEXT_UPCOMING_KEY, true)
                    showToast("Complete")
                }
                is NetworkState.Failed -> {
                    setProgressVisibility(View.GONE)
                    Timber.d(it.message)
                    showToast(it.message)
                }
            }
        }
    }


    private fun observeViewModel() {
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

        viewModel.matchUpdateResult.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> {
                    Timber.tag(TAG).d("Add result Loading")
                    setProgressVisibility(View.VISIBLE)
                }
                is NetworkState.Success -> {
                    setProgressVisibility(View.GONE)
                    /* Send a message to matchListFragment, homeFragment that a player has been added, so that it can refresh the list */
                    findNavController().apply {
                        previousBackStackEntry?.savedStateHandle?.set(MATCH_UPDATED_RESULT_KEY, it.data)
                        getBackStackEntry(R.id.home).savedStateHandle.set(RELOAD_NEXT_UPCOMING_KEY, true)
                        popBackStack()
                    }
                    showToast("Complete")
                }
                is NetworkState.Failed -> {
                    setProgressVisibility(View.GONE)
                    Timber.d(it.message)
                    showToast(it.message)
                }
            }
        }
    }

    private fun setProgressVisibility(visibility: Int) {
        binding.progressBar.visibility = visibility
    }

    /**
     * Set up fields.
     * Resets the views when it the fragment is done adding the data to firebase.
     * Sets up the views when the fragment is editing an existing match data.
     */
    private fun setUpFields(match: Match? = null) {

        if (match?.team1Logo.isNullOrEmpty())
            binding.team1Image.load(R.drawable.gaurdian_angels)
        else
            binding.team1Image.load(match?.team1Logo)

        if (match?.team2Logo.isNullOrEmpty())
            binding.team2Image.setImageDrawable(null)
        else
            binding.team2Image.load(match?.team2Logo)

        val guardianAngels = getString(R.string.guardian_angels)

        val team1Name = match?.team1Name
        if (team1Name.isNullOrEmpty()) {
            binding.team1NameET.setText(guardianAngels)
            selectButtonState(binding.selectTeamButton, true)
        } else {
            binding.team1NameET.setText(match.team1Name)
            if (team1Name == getString(R.string.guardian_angels)) {
                selectButtonState(binding.selectTeamButton, true)
            } else {
                selectButtonState(binding.selectTeamButton, false)
            }
        }

        binding.team2NameET.setText(match?.team2Name)

        if (match != null) {
            val zonedDateTime = viewModel.getZonedDateTime(match.dateAndTime!!, ZoneId.systemDefault())
            val localDate = zonedDateTime.toLocalDate()
            val localTime = zonedDateTime.toLocalTime()

            binding.dateET.setDate(localDate)
            binding.timeET.setTime(localTime)
        } else {
            binding.dateET.setText("")
            binding.timeET.setText("")
        }

        binding.locationET.setText(match?.locationName)
        binding.tournamentNameET.setText(match?.tournamentName)


        val team1Ids = match?.team1TeamIds
        Timber.d("${team.size} ${team.map { it.playerName }}")
        if (!team1Ids.isNullOrEmpty() && team.isEmpty()) {
            Timber.d("Inside for getTeam()")
            binding.selectedPlayersTitle.visibility = View.VISIBLE
            binding.count.apply {
                visibility = View.VISIBLE
                text = team1Ids.size.toString()
            }
            viewModel.getTeamListFromIds(team1Ids)
            observeTeamLiveData()
        }
    }

    private fun observeTeamLiveData() {
        viewModel.team.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Loading -> Timber.d("team Loading")
                is NetworkState.Success -> {
                    Timber.d("Setting team to ${it.data.map { p -> p.playerName }}")
                    team = it.data
                    adapter.submitList(it.data)
                }
                is NetworkState.Failed -> {
                    Timber.d(it.message)
                    showToast(it.message)
                }
            }
        }
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
                    setDate(localDate)
                    viewModel.setDate(localDate)
                }
            }.show(parentFragmentManager, "Material Date Picker")
        }
    }

    private fun TextInputEditText.setDate(localDate: LocalDate) {
        setText(localDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")))
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

                setTime(time12)
                viewModel.setTime(time12)
            }

            picker.show(parentFragmentManager, "Material Time Picker")
        }
    }

    private fun TextInputEditText.setTime(time12: LocalTime) {
        val timeString = time12.format(DateTimeFormatter.ofPattern("hh:mm a"))
        setText(timeString)
    }

    private fun disableButtonIfTeam1NotGA(team1: TextInputEditText, selectTeamButton: MaterialButton) {
        team1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isGuardianAngels = s.toString().trim() == getString(R.string.guardian_angels)
                selectButtonState(selectTeamButton, isGuardianAngels)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun selectButtonState(selectTeamButton: MaterialButton, enabled: Boolean) {
        selectTeamButton.isEnabled = enabled
        selectTeamButton.strike = !enabled
    }


    private inline var MaterialButton.strike: Boolean
        set(visible) {
            paintFlags = if (visible) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        get() = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG == Paint.STRIKE_THRU_TEXT_FLAG

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        binding.selectedPlayerRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}