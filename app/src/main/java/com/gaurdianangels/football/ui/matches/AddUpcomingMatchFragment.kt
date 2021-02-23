package com.gaurdianangels.football.ui.matches

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.gaurdianangels.football.R
import com.gaurdianangels.football.data.Player
import com.gaurdianangels.football.databinding.AddUpcomingMatchFragmentBinding
import com.gaurdianangels.football.util.Constants.PLAYER_SELECTED_KEY
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddUpcomingMatchFragment : Fragment(R.layout.add_upcoming_match_fragment) {
    companion object {
        const val TAG = "AddUpcomingMatch"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = AddUpcomingMatchFragmentBinding.bind(view)

        val dateEditText = binding.dateET
        val timeEditText = binding.timeET

        dateEditText.toDatePicker(parentFragmentManager)
        timeEditText.toTimePicker(parentFragmentManager)

        val recyclerView = binding.selectedPlayerRecyclerView
        val adapter = SelectedPlayerListAdapter()
        recyclerView.adapter = adapter

        var players: List<Player>? = null
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<List<Player>>(PLAYER_SELECTED_KEY)
            ?.observe(viewLifecycleOwner) { result ->
                if (result.isNotEmpty()) {
                    binding.selectedPlayersTitle.visibility = View.VISIBLE
                    binding.count.apply {
                        visibility = View.VISIBLE
                        text = result.size.toString()
                    }
                }
                adapter.submitList(result)
                players = result
            }

        val navController = findNavController()
        binding.selectTeamButton.setOnClickListener {
            navController.navigate(AddUpcomingMatchFragmentDirections.actionAddUpcomingMatchFragmentToMatchPlayerListFragment(players?.toTypedArray()))
        }

        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }
    }

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
                }
            }.show(parentFragmentManager, "Material Date Picker")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun TextInputEditText.toTimePicker(parentFragmentManager: FragmentManager) {

        setOnFocusChangeListener { _, hasFocus -> if (hasFocus) callOnClick() }

        setOnClickListener {
            val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H)
                .build()

            picker.addOnPositiveButtonClickListener {
                val time24 = String.format("%02d:%02d", picker.hour, picker.minute)
                val time12 = LocalTime.parse(time24, DateTimeFormatter.ofPattern("HH:mm"))
                    .format(DateTimeFormatter.ofPattern("hh:mm a"))
                setText(time12)
            }

            picker.show(parentFragmentManager, "Material Time Picker")
        }
    }
}